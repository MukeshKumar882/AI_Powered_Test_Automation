package com.ulta.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage {
    // Verify these candidate selectors against the current permitted Ulta page.
    private final By searchInput = By.cssSelector(
            "input[placeholder*='Search'], input[aria-label*='Search'], input[type='search']");
    private final By accountControl = By.cssSelector(
            "a[aria-label*='Account'], button[aria-label*='Account'], a[href*='account']");

    public HomePage(WebDriver driver) { super(driver); }

    public void open(String baseUrl) { driver.get(baseUrl); }

    public boolean isSearchVisibleAndEnabled() {
        WebElement element = visible(searchInput);
        return element.isDisplayed() && element.isEnabled();
    }

    public boolean isAccountControlAccessible() {
        return visible(accountControl).isDisplayed();
    }

    public void search(String product) {
        type(searchInput, product);
        visible(searchInput).sendKeys(Keys.ENTER);
    }
}
