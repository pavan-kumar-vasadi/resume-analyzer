package com.resumeanalyzer;

import static org.junit.jupiter.api.Assertions.*;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.service.ResumeAnalysisService;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResumeAnalysisServiceTest {
    ResumeAnalysisService svc = new ResumeAnalysisService(15, 10, 15, 20, 15, 10, 5, 10);
    static final String TEXT = "Asha Reddy\nasha@example.com\n+91 98765 43210\n\nSummary\nFinal year student.\n\nEducation\nB.Tech Computer Science, ABC College, CGPA 8.5\n\nSkills\nJava, Spring Boot, MySQL, HTML, CSS\n\nProjects\nBuilt a library system with 95% test coverage.\n\nInternship\nBackend intern at XYZ.\n";
    List<SkillDto> skills = List.of("Java", "Spring Boot", "MySQL", "HTML", "CSS").stream().map(n -> new SkillDto(1L, n, "x")).toList();

    @Test void extractsContactDetails() {
        ResumeAnalysisDto r = svc.analyze(1L, "cv.pdf", "ANALYZED", TEXT, skills);
        assertEquals("Asha Reddy", r.candidateName());
        assertEquals("asha@example.com", r.email());
        assertEquals("+91 98765 43210", r.phone());
    }
    @Test void scoresSectionsAndFlagsMissing() {
        ResumeAnalysisDto r = svc.analyze(1L, "cv.pdf", "ANALYZED", TEXT, skills);
        assertTrue(r.overallScore() >= 80 && r.overallScore() <= 92, "score was " + r.overallScore());
        assertTrue(r.missingSections().contains("Certifications"));
        assertFalse(r.missingSections().contains("Projects"));
    }
    @Test void emptyTextReportsNoOcrAndScoreZero() {
        ResumeAnalysisDto r = svc.analyze(1L, "scan.pdf", "NO_TEXT", "  ", List.of());
        assertEquals(0, r.overallScore());
        assertTrue(r.message().contains("OCR"));
    }
}
