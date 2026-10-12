package com.ulta.hooks;

import com.ulta.driver.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;

public class Hooks {
    @Before
    public void startBrowser() { DriverFactory.startDriver(); }

    @After
    public void stopBrowser(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                try {
                    var driver = DriverFactory.getDriver();
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "failure-screenshot");
                    scenario.attach(driver.getPageSource(), "text/html", "failure-page-source");
                } catch (WebDriverException evidenceError) {
                    scenario.log("Could not capture browser failure evidence: " + evidenceError.getMessage());
                }
            }
        } finally {
            DriverFactory.quitDriver();
        }
    }
}
