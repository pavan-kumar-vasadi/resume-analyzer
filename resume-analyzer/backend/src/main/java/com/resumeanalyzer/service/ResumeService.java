package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.ApiException;
import com.resumeanalyzer.repository.*;
import com.resumeanalyzer.util.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {
    private final ResumeRepository resumes;
    private final UserRepository users;
    private final SkillExtractor extractor;
    private final ResumeAnalysisService analysis;
    private final Path root;

    public ResumeService(ResumeRepository resumes, UserRepository users, SkillExtractor extractor,
                         ResumeAnalysisService analysis, @Value("${app.storage.dir}") String dir) {
        this.resumes = resumes; this.users = users; this.extractor = extractor; this.analysis = analysis;
        this.root = Path.of(dir).toAbsolutePath().normalize(); // outside any static/public folder
    }

    @Transactional
    public ResumeDto upload(MultipartFile file, String email) {
        User u = user(email);
        byte[] data;
        try { data = file.getBytes(); } catch (IOException e) { throw new ApiException(HttpStatus.BAD_REQUEST, "Could not read the uploaded file"); }
        String ext = FileValidator.validate(file.getOriginalFilename(), data);
        String text;
        try { text = TextExtractor.extract(ext, data); }
        catch (Exception e) { throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "This file could not be read. It may be corrupted or password-protected."); }

        String stored = UUID.randomUUID() + "." + ext; // server-generated name: no path traversal possible
        try { Files.createDirectories(root); Files.write(path(stored), data); }
        catch (IOException e) { throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the file"); }

        Resume r = new Resume();
        r.setUser(u);
        r.setFileName(cleanName(file.getOriginalFilename()));
        r.setStoredFileName(stored);
        if (text.isBlank()) {
            r.setStatus(ResumeStatus.NO_TEXT);
        } else {
            r.setExtractedText(text);
            r.getSkills().addAll(extractor.detect(text));
            r.setStatus(ResumeStatus.ANALYZED);
        }
        return toDto(resumes.save(r));
    }

    @Transactional(readOnly = true)
    public List<ResumeDto> mine(String email) {
        return resumes.findByUserIdOrderByUploadedAtDesc(user(email).getId()).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ResumeDto get(Long id, String email) { return toDto(owned(id, email)); }

    @Transactional(readOnly = true)
    public List<SkillDto> skills(Long id, String email) { return skillDtos(owned(id, email)); }

    @Transactional(readOnly = true)
    public ResumeAnalysisDto analysis(Long id, String email) {
        Resume r = owned(id, email);
        return analysis.analyze(r.getId(), r.getFileName(), r.getStatus().name(), r.getExtractedText(), skillDtos(r));
    }

    @Transactional
    public void delete(Long id, String email) {
        Resume r = owned(id, email);
        String stored = r.getStoredFileName();
        resumes.delete(r);
        try { Files.deleteIfExists(path(stored)); } catch (IOException ignored) { }
    }

    private Resume owned(Long id, String email) {
        // 404 (not 403) for other people's resumes, so existence is not revealed
        return resumes.findByIdAndUserId(id, user(email).getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resume not found"));
    }

    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    private Path path(String stored) {
        Path p = root.resolve(stored).normalize();
        if (!p.startsWith(root)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid file path");
        return p;
    }

    private static String cleanName(String n) {
        String s = (n == null ? "resume" : n).replaceAll("[\\\\/\\p{Cntrl}]+", "_");
        return s.length() > 200 ? s.substring(s.length() - 200) : s;
    }

    private List<SkillDto> skillDtos(Resume r) {
        return r.getSkills().stream().map(s -> new SkillDto(s.getId(), s.getName(), s.getCategory())).toList();
    }

    private ResumeDto toDto(Resume r) {
        String msg = r.getStatus() == ResumeStatus.NO_TEXT ? ResumeAnalysisService.NO_TEXT_MSG : null;
        return new ResumeDto(r.getId(), r.getFileName(), r.getStatus().name(), r.getUploadedAt(), r.getSkills().size(), msg);
    }
}
