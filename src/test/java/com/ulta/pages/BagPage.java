package com.ulta.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BagPage extends BasePage {
    // Ulta's app header can render outside a semantic <header>, so these selectors
    // intentionally search the whole DOM and then exclude product "Add to bag" actions.
    private final By bagLink = By.cssSelector(
            "a[href*='/bag' i], a[href*='/basket' i], a[href*='/cart' i], " +
            "button[aria-label*='bag' i], button[aria-label*='cart' i], " +
            "[role='button'][aria-label*='bag' i], [role='button'][aria-label*='cart' i], " +
            "[title*='bag' i], [title*='cart' i], " +
            "[data-testid*='bag-icon' i], [data-testid*='cart-icon' i]");
    private final By bagItems = By.cssSelector(
            "[data-testid*='cart-item'], [data-testid*='bag-item'], " +
            "div.BagProductCardSplit[role='group'][aria-label='product']");
    private final By headerBagControls = By.cssSelector(
            "a[href*='/bag' i], a[href*='/basket' i], a[href*='/cart' i], " +
            "button[aria-label*='bag' i], button[aria-label*='cart' i], " +
            "[role='button'][aria-label*='bag' i], [role='button'][aria-label*='cart' i], " +
            "[title*='bag' i], [title*='cart' i], " +
            "[data-testid*='bag' i], [data-testid*='cart' i], " +
            "a[id*='bag' i], button[id*='bag' i], [role='button'][id*='bag' i], " +
            "a[class*='bag' i], button[class*='bag' i], [role='button'][class*='bag' i], " +
            "a[id*='cart' i], button[id*='cart' i], [role='button'][id*='cart' i], " +
            "a[class*='cart' i], button[class*='cart' i], [role='button'][class*='cart' i]");
    private final By recommendationHeadings = By.cssSelector(
            ".RecommendationProductRail h2, .RecommendationProductRail [role='heading']");
    private final By recommendationCards = By.cssSelector(
            ".RecommendationProductRail a[href*='/p/']");
    private final By recommendationLoaders = By.cssSelector(
            ".RecommendationProductRail .ProductRailHorizontal__Loader--show");
    public BagPage(WebDriver driver) { super(driver); }

    public void openBag() {
        List<WebElement> controls = driver.findElements(bagLink).stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .filter(BagPage::isNotAddToBagAction)
                .toList();
        String bagUrl = controls.stream()
                .filter(control -> "a".equalsIgnoreCase(control.getTagName()))
                .map(control -> control.getAttribute("href"))
                .filter(href -> href != null && !href.isBlank())
                .findFirst()
                .orElse(null);
        if (bagUrl != null) {
            driver.navigate().to(bagUrl);
            return;
        }
        if (!controls.isEmpty()) {
            WebElement control = controls.get(0);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", control);
            control.click();
            return;
        }
        // The Ulta bag route is /bag. Use it only when the icon has no usable
        // accessible link in the current page markup.
        driver.navigate().to("https://www.ulta.com/bag");
    }

    /** Returns the current visible badge count, or zero while the badge is absent/empty. */
    public int headerBagCountOrZero() {
        try {
            return driver.findElements(headerBagControls).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(BagPage::isNotAddToBagAction)
                    .mapToInt(this::countFromControl)
                    .max()
                    .orElse(0);
        } catch (StaleElementReferenceException ignored) {
            return 0;
        }
    }

    public void waitForHeaderBagCountAtLeast(int minimum) {
        try {
            wait.until(currentDriver -> {
                try {
                    // Re-query on every poll; Ulta replaces the header DOM while
                    // the post-add bag panel opens, invalidating prior elements.
                    return currentDriver.findElements(headerBagControls).stream()
                            .filter(WebElement::isDisplayed)
                            .filter(BagPage::isNotAddToBagAction)
                            .anyMatch(control -> countFromControl(control) >= minimum);
                } catch (StaleElementReferenceException ignored) {
                    return false;
                }
            });
        } catch (org.openqa.selenium.TimeoutException timeout) {
            throw new org.openqa.selenium.TimeoutException(
                    "Timed out waiting for header bag count >= " + minimum + ". " +
                            "Current header bag controls: " + describeHeaderBagControls(), timeout);
        }
    }

    private String describeHeaderBagControls() {
        try {
            return driver.findElements(headerBagControls).stream()
                    .filter(WebElement::isDisplayed)
                    .filter(BagPage::isNotAddToBagAction)
                    .limit(20)
                    .map(control -> "tag=" + control.getTagName() + ", text='" + control.getText().trim() + "', href='" +
                            control.getAttribute("href") + "', data-testid='" + control.getAttribute("data-testid") +
                            "', aria-label='" +
                            control.getAttribute("aria-label") + "', title='" +
                            control.getAttribute("title") + "', id='" + control.getAttribute("id") + "', class='" +
                            control.getAttribute("class") + "'")
                    .toList().toString();
        } catch (StaleElementReferenceException ignored) {
            return "<header refreshed while collecting diagnostics>";
        }
    }

    private static boolean isNotAddToBagAction(WebElement element) {
        try {
            String label = String.join(" ",
                    element.getText(),
                    String.valueOf(element.getAttribute("aria-label")),
                    String.valueOf(element.getAttribute("title")),
                    String.valueOf(element.getAttribute("data-testid")))
                    .toLowerCase(java.util.Locale.ROOT);
            return !label.matches("(?s).*\\badd\\s+to\\s+(the\\s+)?bag\\b.*")
                    && !label.matches("(?s).*\\badded\\s+to\\s+(the\\s+|your\\s+)?bag\\b.*")
                    && !label.matches("(?s).*\\b(add|adding)\\s+bag\\b.*")
                    && !label.matches("(?s).*\\badd\\s+for\\s+(ship|pickup)\\b.*");
        } catch (StaleElementReferenceException ignored) {
            return false;
        }
    }

    private int countFromControl(WebElement control) {
        String attributes = String.join(" ",
                control.getText(),
                String.valueOf(control.getAttribute("aria-label")),
                String.valueOf(control.getAttribute("title")),
                String.valueOf(control.getAttribute("data-count")),
                String.valueOf(control.getAttribute("data-quantity")),
                String.valueOf(control.getAttribute("data-bag-count")),
                String.valueOf(control.getAttribute("data-cart-count")));
        Object generatedContent = ((JavascriptExecutor) driver).executeScript(
                "const e=arguments[0];" +
                "const pseudo=[getComputedStyle(e,'::before').content,getComputedStyle(e,'::after').content];" +
                "const children=[...e.querySelectorAll('[data-count],[data-quantity],[aria-label],[title]')]" +
                ".flatMap(x=>[x.textContent,x.getAttribute('data-count'),x.getAttribute('data-quantity')," +
                "x.getAttribute('aria-label'),x.getAttribute('title')]);" +
                "return [...pseudo,...children].join(' ');", control);
        Matcher matcher = Pattern.compile("\\b(\\d+)\\b")
                .matcher(attributes + " " + String.valueOf(generatedContent));
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    public List<String> getBagItemNames() {
        List<WebElement> items = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(bagItems));
        return items.stream().map(item -> {
            List<WebElement> headings = item.findElements(By.cssSelector("h2, h3"));
            return headings.isEmpty() ? item.getText().trim() : headings.get(0).getText().trim();
        }).filter(name -> !name.isEmpty()).distinct().toList();
    }

    public int getBagItemCount() { return getBagItemNames().size(); }

    public List<String> getRecommendationTitles() {
        try {
            wait.until(currentDriver -> hasVisibleElement(currentDriver, recommendationHeadings));
        } catch (org.openqa.selenium.TimeoutException noRecommendationRail) {
            return List.of();
        }
        scrollTo(recommendationHeadings);
        try {
            wait.until(currentDriver -> hasVisibleElement(currentDriver, recommendationCards)
                    || !hasVisibleElement(currentDriver, recommendationLoaders));
        } catch (org.openqa.selenium.TimeoutException noRecommendationCards) {
            return List.of();
        }
        return driver.findElements(recommendationCards).stream()
                .filter(WebElement::isDisplayed)
                .map(link -> {
                    String title = link.getText().trim();
                    if (!title.isEmpty()) return title;
                    String label = link.getAttribute("aria-label");
                    if (label != null && !label.isBlank()) return label.trim();
                    List<WebElement> images = link.findElements(By.tagName("img"));
                    if (images.isEmpty()) return "";
                    String alt = images.get(0).getAttribute("alt");
                    return alt == null ? "" : alt.trim();
                })
                .filter(title -> !title.isEmpty())
                .distinct()
                .limit(12)
                .toList();
    }

    private static boolean hasVisibleElement(WebDriver driver, By locator) {
        try {
            return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
        } catch (StaleElementReferenceException refreshedRail) {
            return false;
        }
    }
}
