package com.ulta.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
    private static final Properties PROPERTIES = load();

    private ConfigReader() { }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config/config.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                        "Missing classpath resource config/config.properties. " +
                        "Check that it is under src/test/resources/config.");
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing configuration property: " + key);
        }
        return value.trim();
    }

    public static String baseUrl() { return get("base.url"); }
    public static String browser() { return get("browser"); }

    public static int explicitWaitSeconds() {
        return Integer.parseInt(get("explicit.wait"));
    }
    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", get("headless")));
    }
}
