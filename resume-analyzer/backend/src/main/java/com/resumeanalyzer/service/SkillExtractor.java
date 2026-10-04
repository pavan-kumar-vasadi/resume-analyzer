package com.resumeanalyzer.service;

import static java.util.Map.entry;

import com.resumeanalyzer.entity.Skill;
import com.resumeanalyzer.repository.SkillRepository;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Rule-based dictionary matching (not machine learning). Skills come from the `skills` table; aliases below. */
@Service
public class SkillExtractor {
    private static final Map<String, List<String>> ALIASES = Map.ofEntries(
        entry("JavaScript", List.of("js", "ecmascript")), entry("Node.js", List.of("nodejs", "node js")),
        entry("React", List.of("reactjs", "react.js")), entry("Angular", List.of("angularjs")),
        entry("Spring Boot", List.of("springboot")), entry("Spring MVC", List.of("springmvc")),
        entry("REST API", List.of("rest apis", "restful api", "restful apis", "restful", "rest services")),
        entry("Express", List.of("express.js", "expressjs")), entry("PostgreSQL", List.of("postgres")),
        entry("MongoDB", List.of("mongo db", "mongo")), entry("Machine Learning", List.of("ml")),
        entry("NLP", List.of("natural language processing")), entry("CI/CD", List.of("cicd", "ci cd")),
        entry("OOP", List.of("oops", "object oriented", "object-oriented")),
        entry("DBMS", List.of("database management")), entry("Operating Systems", List.of("operating system")),
        entry("Computer Networks", List.of("computer network", "computer networking")),
        entry("Data Structures", List.of("data structure", "dsa")), entry("Algorithms", List.of("algorithm")),
        entry("Manual Testing", List.of("manual test")), entry("Data Analysis", List.of("data analytics")),
        entry("C++", List.of("cpp")), entry("HTML", List.of("html5")), entry("CSS", List.of("css3")));
    /** Names too generic to match on their own; only their aliases count. */
    private static final Set<String> NO_BARE_NAME = Set.of("Express");

    private final SkillRepository skills;
    public SkillExtractor(SkillRepository skills) { this.skills = skills; }

    public List<Skill> detect(String text) {
        List<Skill> all = skills.findAll();
        Set<String> found = new HashSet<>(detectNames(text, all.stream().map(Skill::getName).toList()));
        return all.stream().filter(s -> found.contains(s.getName())).toList();
    }

    public static List<String> detectNames(String text, Collection<String> names) {
        String t = text == null ? "" : text.replaceAll("\\s+", " ");
        List<String> out = new ArrayList<>();
        for (String n : names) {
            List<String> terms = new ArrayList<>();
            if (!NO_BARE_NAME.contains(n)) terms.add(n);
            terms.addAll(ALIASES.getOrDefault(n, List.of()));
            for (String term : terms) if (matches(t, term)) { out.add(n); break; } // each skill at most once
        }
        return out;
    }

    // Whole-term match: not preceded/followed by letters, digits, +, # (so SQL != MySQL, Java != JavaScript, C != C++).
    static boolean matches(String text, String term) {
        int flags = term.equals("C") ? 0 : Pattern.CASE_INSENSITIVE; // single-letter "C" is case-sensitive
        return Pattern.compile("(?<![A-Za-z0-9+#.])" + Pattern.quote(term) + "(?![A-Za-z0-9+#])", flags).matcher(text).find();
    }
}
