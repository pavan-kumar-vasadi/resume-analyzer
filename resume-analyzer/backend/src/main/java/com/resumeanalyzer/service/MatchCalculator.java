package com.resumeanalyzer.service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Skill-overlap scoring (a transparent estimate, NOT a probability of being hired).
 *   required coverage  = matched required / total required x 100
 *   preferred coverage = matched preferred / total preferred x 100
 *   score = w x required + (1 - w) x preferred   (w configurable, default 0.8)
 * Edge cases (no division by zero): no preferred -> required coverage only; no required -> preferred only; neither -> 0.
 */
public final class MatchCalculator {
    private MatchCalculator() {}

    public record Result(double requiredPct, double preferredPct, double score, List<String> matchedRequired,
                         List<String> missingRequired, List<String> matchedPreferred, List<String> missingPreferred) {}

    public static String norm(String s) { return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase(); }

    public static Result compute(Collection<String> resumeSkills, List<String> required, List<String> preferred, double requiredWeight) {
        Set<String> have = resumeSkills.stream().map(MatchCalculator::norm).collect(Collectors.toSet());
        List<String> mr = required.stream().filter(s -> have.contains(norm(s))).toList();
        List<String> xr = required.stream().filter(s -> !have.contains(norm(s))).toList();
        List<String> mp = preferred.stream().filter(s -> have.contains(norm(s))).toList();
        List<String> xp = preferred.stream().filter(s -> !have.contains(norm(s))).toList();
        double reqPct = required.isEmpty() ? 0 : 100.0 * mr.size() / required.size();
        double prefPct = preferred.isEmpty() ? 0 : 100.0 * mp.size() / preferred.size();
        double w = Math.max(0, Math.min(1, requiredWeight));
        double score;
        if (!required.isEmpty() && !preferred.isEmpty()) score = w * reqPct + (1 - w) * prefPct;
        else if (!required.isEmpty()) score = reqPct;
        else if (!preferred.isEmpty()) score = prefPct;
        else score = 0;
        return new Result(round(reqPct), round(prefPct), round(score), mr, xr, mp, xp);
    }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
