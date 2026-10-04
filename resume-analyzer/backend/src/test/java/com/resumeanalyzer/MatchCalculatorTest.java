package com.resumeanalyzer;

import static org.junit.jupiter.api.Assertions.*;

import com.resumeanalyzer.service.MatchCalculator;
import com.resumeanalyzer.service.MatchCalculator.Result;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchCalculatorTest {
    List<String> resume = List.of("Java", "Spring Boot", "MySQL", "HTML", "CSS");

    @Test void specExample_60PercentAndMissingSkills() {
        Result r = MatchCalculator.compute(resume, List.of("Java", "Spring Boot", "React", "MySQL", "REST API"), List.of(), 0.8);
        assertEquals(60.0, r.requiredPct());
        assertEquals(60.0, r.score()); // no preferred skills: required coverage only
        assertEquals(List.of("Java", "Spring Boot", "MySQL"), r.matchedRequired());
        assertEquals(List.of("React", "REST API"), r.missingRequired());
    }
    @Test void combinedScoreUses80_20() {
        Result r = MatchCalculator.compute(resume, List.of("Java", "Spring Boot", "React", "MySQL", "REST API"), List.of("HTML", "Docker"), 0.8);
        assertEquals(58.0, r.score()); // 0.8*60 + 0.2*50
        assertEquals(List.of("HTML"), r.matchedPreferred());
        assertEquals(List.of("Docker"), r.missingPreferred());
    }
    @Test void noRequiredSkillsUsesPreferredOnly() {
        assertEquals(50.0, MatchCalculator.compute(resume, List.of(), List.of("Java", "Docker"), 0.8).score());
    }
    @Test void noSkillsAtAllGivesZeroWithoutDivisionByZero() {
        assertEquals(0.0, MatchCalculator.compute(resume, List.of(), List.of(), 0.8).score());
    }
    @Test void caseAndWhitespaceAreNormalized() {
        Result r = MatchCalculator.compute(List.of("  java ", "SPRING   BOOT"), List.of("Java", "Spring Boot"), List.of(), 0.8);
        assertEquals(100.0, r.score());
    }
    @Test void emptyResumeScoresZero() {
        assertEquals(0.0, MatchCalculator.compute(List.of(), List.of("Java"), List.of("Git"), 0.8).score());
    }
}
