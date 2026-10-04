package com.resumeanalyzer;

import static org.junit.jupiter.api.Assertions.*;

import com.resumeanalyzer.service.SkillExtractor;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkillExtractorTest {
    static final List<String> NAMES = List.of("Java", "JavaScript", "C", "C++", "SQL", "MySQL", "Express", "Node.js", "REST API", "Spring Boot", "React");
    List<String> d(String t) { return SkillExtractor.detectNames(t, NAMES); }

    @Test void javaIsNotJavaScript() { assertEquals(List.of("Java"), d("Java developer")); }
    @Test void mysqlIsNotSqlAndJavaScriptIsNotJava() { assertEquals(List.of("JavaScript", "MySQL"), d("JavaScript, MySQL")); }
    @Test void aliasesWork() { assertEquals(List.of("JavaScript", "Node.js"), d("Worked with js and nodejs")); }
    @Test void cppIsNotC() { assertEquals(List.of("C++"), d("C++ programming")); }
    @Test void cAlone() { assertEquals(List.of("Java", "C"), d("Languages: C, Java")); }
    @Test void noDuplicates() { assertEquals(List.of("Java"), d("Java java JAVA")); }
    @Test void genericExpressWordIgnored() {
        assertTrue(d("I express my interest").isEmpty());
        assertEquals(List.of("Express"), d("Built APIs with ExpressJS"));
    }
}
