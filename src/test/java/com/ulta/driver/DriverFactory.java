package com.ulta.driver;

import com.ulta.config.ConfigReader;
import java.io.File;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

public final class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() { }

    public static void startDriver() {
        if (!"chrome".equalsIgnoreCase(ConfigReader.browser())) {
            throw new IllegalArgumentException(
                    "Unsupported browser: " + ConfigReader.browser() + ". This starter supports Chrome.");
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        if (ConfigReader.headless()) {
            options.addArguments("--headless=new", "--window-size=1440,1000");
        } else {
            options.addArguments("--start-maximized");
        }
        ChromeDriverService service = new ChromeDriverService.Builder()
                .withLogFile(new File("target/chromedriver.log"))
                .withVerbose(true)
                .build();
        DRIVER.set(new ChromeDriver(service, options));
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is not started for this scenario.");
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        try {
            if (driver != null) driver.quit();
        } finally {
            DRIVER.remove();
        }
    }
}
