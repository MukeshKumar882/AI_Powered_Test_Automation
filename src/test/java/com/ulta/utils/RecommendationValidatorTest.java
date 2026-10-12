package com.ulta.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RecommendationValidatorTest {
    private static final Set<String> KEYWORDS = Set.of("skin");

    @Test
    public void scoresBelowThreshold() {
        Assert.assertEquals(RecommendationValidator.relevancePercentage(titlesWithRelevantCount(29), KEYWORDS), 29);
    }

    @Test
    public void scoresAtThreshold() {
        Assert.assertEquals(RecommendationValidator.relevancePercentage(titlesWithRelevantCount(30), KEYWORDS), 30);
    }

    @Test
    public void scoresAboveThreshold() {
        Assert.assertEquals(RecommendationValidator.relevancePercentage(titlesWithRelevantCount(31), KEYWORDS), 31);
    }

    @Test
    public void emptyRecommendationsScoreZero() {
        Assert.assertEquals(RecommendationValidator.relevancePercentage(List.of(), KEYWORDS), 0);
    }

    private List<String> titlesWithRelevantCount(int relevant) {
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < relevant; i++) titles.add("Skin care product " + i);
        for (int i = relevant; i < 100; i++) titles.add("Unrelated product " + i);
        return titles;
    }
}
