package com.ulta.utils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RecommendationValidator {
    private RecommendationValidator() { }

    public static int relevancePercentage(List<String> titles, Set<String> keywords) {
        List<String> usable = titles == null ? List.of() : titles.stream()
                .filter(title -> title != null && !title.isBlank()).toList();
        if (usable.isEmpty() || keywords == null || keywords.isEmpty()) return 0;
        long relevant = usable.stream().filter(title -> isRelevant(title, keywords)).count();
        return (int) (relevant * 100 / usable.size());
    }

    /** Compares the unrounded ratio so rounding cannot turn a below-threshold score into a pass. */
    public static boolean meetsMinimumPercentage(List<String> titles, Set<String> keywords, int minimum) {
        if (minimum < 0 || minimum > 100) {
            throw new IllegalArgumentException("Minimum percentage must be between 0 and 100.");
        }
        List<String> usable = titles == null ? List.of() : titles.stream()
                .filter(title -> title != null && !title.isBlank()).toList();
        if (usable.isEmpty() || keywords == null || keywords.isEmpty()) return false;
        long relevant = usable.stream().filter(title -> isRelevant(title, keywords)).count();
        return relevant * 100 >= (long) usable.size() * minimum;
    }

    private static boolean isRelevant(String title, Set<String> keywords) {
        String normalizedTitle = normalize(title);
        return keywords.stream().filter(keyword -> keyword != null && !keyword.isBlank())
                .map(RecommendationValidator::normalize)
                .anyMatch(normalizedTitle::contains);
    }

    private static String normalize(String text) {
        return " " + text.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ").trim() + " ";
    }
}
