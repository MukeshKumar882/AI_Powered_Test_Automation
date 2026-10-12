package com.ulta.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProductPage extends BasePage {
    private final By productTitle = By.cssSelector("h1");
    // These selectors are examples. Confirm their meaning on the current product page.
    private final By optionControls = By.cssSelector(
            "button[aria-label*='size'], button[data-testid*='size'], [role='radio'][aria-label]");
    // AC5 calls the product carousel "You may also like" / "Similar products".
    // Include the live site's common "Similar items for you" wording as well.
    private final By recommendationHeadings = By.cssSelector(
            ".RecommendationProductRail h2, .RecommendationProductRail [role='heading']");
    private final By recommendationCards = By.cssSelector(
            ".RecommendationProductRail a[href*='/p/']");
    private final By recommendationLoaders = By.cssSelector(
            ".RecommendationProductRail .ProductRailHorizontal__Loader--show");
    // The current Ulta PDP CTA is labelled "Add for ship". Keep "Add to bag"
    // as a fallback for product/page variants that still use that wording.
    private final By primaryAddForShipButton = By.cssSelector(
            "button[class*='AddToBagButton__AddToBag']");
    private final By addForShipButton = By.xpath(
            "//button[contains(translate(normalize-space(.)," +
            "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add for ship')]");
    private final By addToBagButton = By.xpath(
            "//button[contains(translate(normalize-space(.)," +
            "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add to bag')]");
    private final By addedToBagConfirmation = By.cssSelector(
            "button[aria-label*='Added to your bag' i]");

    public ProductPage(WebDriver driver) { super(driver); }

    public String getProductTitle() { return visible(productTitle).getText().trim(); }

    public void selectFirstAvailableOptionIfRequired() {
        List<WebElement> options = driver.findElements(optionControls);
        if (!options.isEmpty()) {
            WebElement option = options.stream().filter(WebElement::isDisplayed)
                    .filter(WebElement::isEnabled).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Product options are present but unavailable."));
            option.click();
        }
    }

    public void addToBag() {
        List<WebElement> primaryButtons = driver.findElements(primaryAddForShipButton).stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .toList();
        if (!primaryButtons.isEmpty()) {
            primaryButtons.get(0).click();
        } else {
            List<WebElement> shipButtons = driver.findElements(addForShipButton).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(WebElement::isEnabled)
                    .toList();
            if (!shipButtons.isEmpty()) {
                shipButtons.get(0).click();
            } else {
                click(addToBagButton);
            }
        }
        try {
            wait.until(currentDriver -> currentDriver.findElements(addedToBagConfirmation).stream()
                    .anyMatch(WebElement::isDisplayed));
        } catch (org.openqa.selenium.TimeoutException timeout) {
            throw new org.openqa.selenium.TimeoutException(
                    "The add-to-bag control was clicked but Ulta did not confirm the product was added. " +
                            "Current URL: " + driver.getCurrentUrl() + "; page title: '" + driver.getTitle() + "'.",
                    timeout);
        }
    }

    public List<String> getRecommendationTitles() {
        if (driver.findElements(recommendationHeadings).stream().noneMatch(WebElement::isDisplayed)) {
            return List.of();
        }
        scrollTo(recommendationHeadings);
        wait.until(currentDriver -> {
            try {
                boolean hasCards = currentDriver.findElements(recommendationCards).stream()
                        .anyMatch(WebElement::isDisplayed);
                boolean loading = currentDriver.findElements(recommendationLoaders).stream()
                        .anyMatch(WebElement::isDisplayed);
                return hasCards || !loading;
            } catch (org.openqa.selenium.StaleElementReferenceException refreshedRail) {
                return false;
            }
        });
        return driver.findElements(recommendationCards).stream()
                .filter(WebElement::isDisplayed)
                .map(this::getLinkTitle)
                .filter(title -> !title.isEmpty())
                .distinct()
                .limit(12)
                .toList();
    }

    private String getLinkTitle(WebElement link) {
        String title = link.getText().trim();
        if (!title.isEmpty()) return title;
        title = link.getAttribute("aria-label");
        if (title != null && !title.isBlank()) return title.trim();
        List<WebElement> images = link.findElements(By.tagName("img"));
        if (images.isEmpty()) return "";
        String alt = images.get(0).getAttribute("alt");
        return alt == null ? "" : alt.trim();
    }
}
