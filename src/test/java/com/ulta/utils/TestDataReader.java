package com.ulta.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class TestDataReader {
    public record TestData(String criterion, String searchTerm, Set<String> keywords,
                           int minPercentage) { }

    private static final List<TestData> DATA = load();
    private TestDataReader() { }

    private static List<TestData> load() {
        String resource = "testdata/ulta-test-data.csv";
        try (InputStream input = TestDataReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) throw new IllegalStateException("Missing classpath resource " + resource);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return reader.lines().skip(1).filter(line -> !line.isBlank()).map(line -> {
                    String[] cols = line.split(",", -1);
                    if (cols.length != 4) throw new IllegalArgumentException("Expected 4 CSV fields: " + line);
                    Set<String> keywords = Arrays.stream(cols[2].split(";"))
                            .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toUnmodifiableSet());
                    return new TestData(cols[0].trim(), cols[1].trim(), keywords,
                            Integer.parseInt(cols[3].trim()));
                }).toList();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + resource, e);
        }
    }

    public static TestData forCriterion(String criterion) {
        return DATA.stream().filter(row -> row.criterion().equalsIgnoreCase(criterion))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "No test data for criterion " + criterion));
    }
}
