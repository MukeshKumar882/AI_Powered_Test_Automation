package com.ulta.pages;

import com.ulta.config.ConfigReader;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver, Duration.ofSeconds(ConfigReader.explicitWaitSeconds()));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected void scrollTo(By locator) {
        JavascriptExecutor javascript = (JavascriptExecutor) driver;
        for (int step = 0; step < 12; step++) {
            for (WebElement candidate : driver.findElements(locator)) {
                if (candidate.isDisplayed()) {
                    javascript.executeScript(
                            "arguments[0].scrollIntoView({block:'center'});", candidate);
                    return;
                }
            }
            javascript.executeScript(
                    "window.scrollBy(0, Math.max(400, Math.floor(window.innerHeight * 0.8))); ");
        }
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        javascript.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }
}
