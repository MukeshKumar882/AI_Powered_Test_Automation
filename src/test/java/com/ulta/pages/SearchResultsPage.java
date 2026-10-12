package com.ulta.pages;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchResultsPage extends BasePage {
    private final By productLinks = By.cssSelector(
            "a[href*='/p/'], a[href*='/product/'], " +
            "[data-testid*='product-card' i] a[href], [data-testid*='product' i] a[href]");
    private final WebDriverWait resultsWait;

    public SearchResultsPage(WebDriver driver) {
        super(driver);
        // Ulta's client-rendered search results can take longer than ordinary page controls.
        this.resultsWait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public boolean hasAtLeastOneProduct() {
        return resultsWait.until(currentDriver -> firstVisibleProduct(currentDriver) != null);
    }

    public void openFirstProduct() {
        String sourceHandle = driver.getWindowHandle();
        Set<String> originalHandles = new HashSet<>(driver.getWindowHandles());
        String searchUrl = driver.getCurrentUrl();
        try {
            WebElement firstProduct = resultsWait.until(this::firstVisibleProduct);
            firstProduct.click();
            resultsWait.until(currentDriver -> {
                Set<String> currentHandles = currentDriver.getWindowHandles();
                if (currentHandles.size() > originalHandles.size()) {
                    currentHandles.stream()
                            .filter(handle -> !originalHandles.contains(handle))
                            .findFirst()
                            .ifPresent(handle -> currentDriver.switchTo().window(handle));
                }
                return isProductUrl(currentDriver.getCurrentUrl());
            });

            if (!driver.getWindowHandle().equals(sourceHandle)) {
                String productHandle = driver.getWindowHandle();
                driver.switchTo().window(sourceHandle).close();
                driver.switchTo().window(productHandle);
            }
        } catch (TimeoutException timeout) {
            throw new TimeoutException(
                    "Could not open a product page from search URL " + searchUrl + ". Current URL: " +
                            driver.getCurrentUrl() +
                            "; page title: '" + driver.getTitle() + "'; visible body text: '" +
                            visibleBodyText() + "'; open windows: " + driver.getWindowHandles() +
                            "; product links found: " + describeProductLinks(), timeout);
        }
    }

    private boolean isProductUrl(String url) {
        String normalizedUrl = url.toLowerCase(java.util.Locale.ROOT);
        return normalizedUrl.contains("/p/") || normalizedUrl.contains("/product/");
    }

    private WebElement firstVisibleProduct(WebDriver currentDriver) {
        return currentDriver.findElements(productLinks).stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .findFirst()
                .orElse(null);
    }

    private String visibleBodyText() {
        List<WebElement> bodies = driver.findElements(By.tagName("body"));
        if (bodies.isEmpty()) return "<body missing>";
        String text = bodies.get(0).getText().replaceAll("\\s+", " ").trim();
        return text.substring(0, Math.min(text.length(), 700));
    }

    private String describeProductLinks() {
        return driver.findElements(productLinks).stream()
                .limit(8)
                .map(link -> "href='" + link.getAttribute("href") + "', visible=" + link.isDisplayed() +
                        ", text='" + link.getText().replaceAll("\\s+", " ").trim() + "'")
                .toList().toString();
    }
}
