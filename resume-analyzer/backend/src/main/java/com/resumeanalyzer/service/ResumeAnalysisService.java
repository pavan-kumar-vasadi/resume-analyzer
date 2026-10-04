package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.*;
import java.util.*;
import java.util.regex.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Transparent heuristic checks on the extracted text. Measures completeness, not employability or any ATS score. */
@Service
public class ResumeAnalysisService {
    public static final String NO_TEXT_MSG = "No readable text was found. This looks like a scanned or image-based file. OCR is not configured, so nothing was extracted.";
    static final String NOTE = "This score measures document completeness using the configured rules. It is not a guarantee of employability or an employer's ATS score.";
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile("\\+?\\d[\\d\\s()-]{8,16}\\d");
    private static final Pattern HEADING = Pattern.compile("^(?:(?:technical|key|core|academic|professional|work|educational|personal|career) )?(skills|projects?|education|qualifications?|experience|internships?|certifications?|certificates?|summary|objective|profile|about me)(?: (?:summary|details|qualifications?))?$");
    private static final Pattern INLINE = Pattern.compile("^(?:technical |key |core )?(skills|projects?|education|experience|internships?|certifications?|summary|objective)\\s*[:\\-]");
    private static final List<String> EDU = List.of("b.tech", "btech", "m.tech", "mtech", "bachelor", "master", "b.sc", "m.sc", "bca", "mca", "mba", "diploma", "degree", "university", "college", "institute", "cgpa", "gpa", "intermediate");
    private static final List<String> EXP = List.of("intern", "internship", "experience", "developer", "engineer", "worked", "developed", "built", "implemented", "designed", "trainee", "freelance");

    private final Map<String, Integer> w = new LinkedHashMap<>();

    public ResumeAnalysisService(@Value("${app.scoring.contact:15}") int contact, @Value("${app.scoring.summary:10}") int summary,
            @Value("${app.scoring.education:15}") int education, @Value("${app.scoring.skills:20}") int skills,
            @Value("${app.scoring.projects:15}") int projects, @Value("${app.scoring.experience:10}") int experience,
            @Value("${app.scoring.certifications:5}") int certs, @Value("${app.scoring.readability:10}") int readability) {
        w.put("CONTACT", contact); w.put("SUMMARY", summary); w.put("EDUCATION", education); w.put("SKILLS", skills);
        w.put("PROJECTS", projects); w.put("EXPERIENCE", experience); w.put("CERT", certs); w.put("READ", readability);
    }

    public ResumeAnalysisDto analyze(Long id, String fileName, String status, String text, List<SkillDto> skills) {
        if (text == null || text.isBlank())
            return new ResumeAnalysisDto(id, fileName, status, NO_TEXT_MSG, null, null, null, List.of(), List.of(), List.of(),
                0, List.of(), List.of(), List.of("Upload a text-based PDF or DOCX file."), "", "", NOTE);
        String email = first(EMAIL, text);
        String phone = phone(text);
        Set<String> sec = sections(text);
        List<String> edu = found(EDU, text), exp = found(EXP, text);

        double contact = (email != null ? 0.6 : 0) + (phone != null ? 0.4 : 0);
        double eduF = sec.contains("EDUCATION") ? 1 : (edu.size() >= 2 ? 0.5 : 0);
        double skillsF = (sec.contains("SKILLS") ? 0.4 : 0) + Math.min(1, skills.size() / 5.0) * 0.6;
        List<SectionScoreDto> rows = List.of(
            row("Contact information", "CONTACT", contact), row("Professional summary", "SUMMARY", sec.contains("SUMMARY") ? 1 : 0),
            row("Education", "EDUCATION", eduF), row("Skills", "SKILLS", skillsF),
            row("Projects", "PROJECTS", sec.contains("PROJECTS") ? 1 : 0),
            row("Experience / internship", "EXPERIENCE", sec.contains("EXPERIENCE") ? 1 : 0),
            row("Certifications", "CERT", sec.contains("CERT") ? 1 : 0),
            row("Readability (enough text)", "READ", Math.min(1, text.length() / 1500.0)));
        int total = Math.min(100, rows.stream().mapToInt(SectionScoreDto::score).sum());
        List<String> missing = rows.stream().filter(r -> !r.present()).map(SectionScoreDto::name).toList();

        List<String> tips = new ArrayList<>();
        if (email == null) tips.add("Add a professional email address.");
        if (phone == null) tips.add("Add a phone number so recruiters can reach you.");
        if (!sec.contains("SUMMARY")) tips.add("Add a short professional summary.");
        if (eduF < 1) tips.add("Add a clearly labeled Education section with degree, college and year.");
        if (skills.size() < 5) tips.add("Include relevant technical skills in a dedicated Skills section.");
        if (!sec.contains("PROJECTS")) tips.add("Add a Projects section describing what you built and the technologies used.");
        if (!sec.contains("EXPERIENCE")) tips.add("Include internship or project experience if you have any. Do not invent experience.");
        if (!sec.contains("CERT")) tips.add("List certifications or courses you have actually completed.");
        if (!Pattern.compile("\\d+\\s?%|\\d+\\+").matcher(text).find()) tips.add("Add measurable achievements (numbers, percentages) to project descriptions.");
        if (text.length() < 1500) tips.add("Add more detail: the resume has little readable text.");
        tips.add("Keep formatting readable and consistent, and remove unnecessary personal information.");

        String flat = text.replaceAll("\\s+", " ").trim();
        return new ResumeAnalysisDto(id, fileName, status, null, name(text), email, phone, skills, edu, exp, total, rows, missing,
            tips, flat.length() > 600 ? flat.substring(0, 600) + "…" : flat, text, NOTE);
    }

    private SectionScoreDto row(String label, String key, double frac) {
        int max = w.get(key);
        return new SectionScoreDto(label, (int) Math.round(max * frac), max, frac >= 0.5);
    }

    private static Set<String> sections(String text) {
        Set<String> out = new HashSet<>();
        for (String line : text.split("\\R")) {
            String raw = line.trim().toLowerCase();
            if (raw.isEmpty()) continue;
            String norm = raw.replaceAll("[^a-z ]", " ").replaceAll("\\s+", " ").trim();
            Matcher m = norm.length() <= 40 ? HEADING.matcher(norm) : null;
            if (m != null && m.find()) { out.add(key(m.group(1))); continue; }
            Matcher in = INLINE.matcher(raw);
            if (in.find()) out.add(key(in.group(1)));
        }
        return out;
    }

    private static String key(String k) {
        if (k.startsWith("skill")) return "SKILLS";
        if (k.startsWith("project")) return "PROJECTS";
        if (k.startsWith("education") || k.startsWith("qualification")) return "EDUCATION";
        if (k.startsWith("experience") || k.startsWith("internship")) return "EXPERIENCE";
        if (k.startsWith("certif")) return "CERT";
        return "SUMMARY";
    }

    private static String first(Pattern p, String t) { Matcher m = p.matcher(t); return m.find() ? m.group() : null; }

    private static String phone(String t) {
        Matcher m = PHONE.matcher(t);
        while (m.find()) {
            int digits = m.group().replaceAll("\\D", "").length();
            if (digits >= 10 && digits <= 13) return m.group().trim();
        }
        return null;
    }

    private static String name(String text) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < Math.min(lines.length, 5); i++) {
            String l = lines[i].trim();
            if (l.isEmpty()) continue;
            int words = l.split("\\s+").length;
            if (l.length() <= 40 && words >= 2 && words <= 4 && l.matches("[A-Za-z. ]+") && !l.toLowerCase().matches(".*(resume|curriculum|vitae).*")) return l;
        }
        return null; // not guessed
    }

    private static List<String> found(List<String> terms, String text) {
        List<String> out = new ArrayList<>();
        for (String t : terms)
            if (Pattern.compile("(?<![A-Za-z0-9])" + Pattern.quote(t) + "(?![A-Za-z0-9])", Pattern.CASE_INSENSITIVE).matcher(text).find()) out.add(t);
        return out;
    }
}
