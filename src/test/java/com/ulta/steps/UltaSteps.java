package com.ulta.steps;

import com.ulta.config.ConfigReader;
import com.ulta.driver.DriverFactory;
import com.ulta.pages.BagPage;
import com.ulta.pages.HomePage;
import com.ulta.pages.ProductPage;
import com.ulta.pages.SearchResultsPage;
import com.ulta.utils.RecommendationValidator;
import com.ulta.utils.TestDataReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

public class UltaSteps {
    private HomePage home;
    private SearchResultsPage results;
    private ProductPage product;
    private BagPage bag;
    private final List<String> addedProductTitles = new ArrayList<>();
    private final List<String> addedProductUrls = new ArrayList<>();
    private final List<String> firstProductPageRecommendations = new ArrayList<>();
    private final List<String> productPageRecommendations = new ArrayList<>();
    private SoftAssert recommendationAssertions = new SoftAssert();
    private int expectedHeaderBagCount;

    private void initializePages() {
        var driver = DriverFactory.getDriver();
        home = new HomePage(driver);
        results = new SearchResultsPage(driver);
        product = new ProductPage(driver);
        bag = new BagPage(driver);
    }

    @Given("the user launches the Ulta website")
    public void launchUlta() {
        addedProductTitles.clear();
        addedProductUrls.clear();
        firstProductPageRecommendations.clear();
        productPageRecommendations.clear();
        recommendationAssertions = new SoftAssert();
        expectedHeaderBagCount = 0;
        initializePages();
        home.open(ConfigReader.baseUrl());
    }

    @Then("the search bar should be visible and enabled")
    public void searchBarReady() {
        Assert.assertTrue(home.isSearchVisibleAndEnabled(), "Search input is not visible and enabled");
    }

    @And("the account sign-in control should be accessible")
    public void accountControlAccessible() {
        Assert.assertTrue(home.isAccountControlAccessible(), "Account/sign-in control was not found");
    }

    @When("the user searches for {string}")
    public void searchFor(String term) {
        home.open(ConfigReader.baseUrl());
        home.search(term);
        results = new SearchResultsPage(DriverFactory.getDriver());
    }

    @Then("at least one search result should be displayed")
    public void searchResultDisplayed() {
        Assert.assertTrue(results.hasAtLeastOneProduct(), "No product results were found");
    }

    @Then("the bag should contain at least one product")
    public void bagContainsOneProduct() {
        assertBagContainsAtLeast(1);
    }

    @Then("the bag should contain at least {int} products")
    public void bagContainsAtLeastProducts(int minimum) {
        assertBagContainsAtLeast(minimum);
    }

    @Then("the cart badge count should be at least {int}")
    public void cartIconShowsAtLeast(int minimum) {
        Assert.assertTrue(expectedHeaderBagCount >= minimum,
                "The expected cart badge count after the last add was " + expectedHeaderBagCount +
                        ", below the required " + minimum);
        bag.openBag();
        bag.waitForHeaderBagCountAtLeast(minimum);
        int actual = bag.headerBagCountOrZero();
        Assert.assertTrue(actual >= minimum,
                "Expected the cart icon to show at least " + minimum + " item(s), but it showed " + actual);
    }

    private void assertBagContainsAtLeast(int minimum) {
        bag.openBag();
        int actualCount = bag.getBagItemCount();
        Assert.assertTrue(actualCount >= minimum,
                "Expected at least " + minimum + " product(s) in the bag; found " + actualCount);
    }

    @When("the shopper adds {string} to the bag")
    public void shopperAddsProduct(String term) {
        expectedHeaderBagCount = bag.headerBagCountOrZero() + 1;
        addProduct(term);
    }

    private void addProduct(String term) {
        // Reuse the scenario's current WebDriver; this navigates the existing tab only.
        home.open(ConfigReader.baseUrl());
        home.search(term);
        results = new SearchResultsPage(DriverFactory.getDriver());
        results.openFirstProduct();
        product = new ProductPage(DriverFactory.getDriver());
        product.selectFirstAvailableOptionIfRequired();
        addedProductTitles.add(product.getProductTitle());
        addedProductUrls.add(DriverFactory.getDriver().getCurrentUrl());
        product.addToBag();
    }

    @When("the shopper collects product page recommendations for the added products")
    public void collectProductRecommendationsForAddedProducts() {
        var driver = DriverFactory.getDriver();
        for (int i = 0; i < addedProductUrls.size(); i++) {
            driver.get(addedProductUrls.get(i));
            product = new ProductPage(driver);
            List<String> titles = product.getRecommendationTitles();
            productPageRecommendations.addAll(titles);
            if (i == 0) firstProductPageRecommendations.addAll(titles);
        }
    }

    @Then("the bag should contain all products added during this scenario")
    public void bagContainsAddedProducts() {
        List<String> actual = bag.getBagItemNames();
        Assert.assertTrue(actual.size() >= 3, "Expected at least three bag lines, found " + actual.size());
        for (String expectedTitle : addedProductTitles) {
            String expected = normalize(expectedTitle);
            Assert.assertTrue(actual.stream().map(UltaSteps::normalize)
                    .anyMatch(actualTitle -> actualTitle.contains(expected) || expected.contains(actualTitle)),
                    "Added product was not found in bag: " + expectedTitle + "; bag contains " + actual);
        }
    }

    @Then("product page recommendations should meet the AC5 threshold")
    public void pdpRecommendationsMeetThreshold() {
        assertRelevance("AC5", firstProductPageRecommendations);
    }

    @When("the shopper opens the bag")
    public void shopperOpensBag() { bag.openBag(); }

    @Then("bag recommendations should meet the AC6 threshold")
    public void bagRecommendationsMeetThreshold() {
        assertRelevance("AC6", bag.getRecommendationTitles());
    }

    @Then("combined recommendations should meet the AC7 threshold")
    public void combinedRecommendationsMeetThreshold() {
        List<String> combined = new ArrayList<>(productPageRecommendations);
        combined.addAll(bag.getRecommendationTitles());
        assertRelevance("AC7", combined);
        recommendationAssertions.assertAll();
    }

    private void assertRelevance(String criterion, List<String> titles) {
        var data = TestDataReader.forCriterion(criterion);
        int percent = RecommendationValidator.relevancePercentage(titles, data.keywords());
        recommendationAssertions.assertTrue(RecommendationValidator.meetsMinimumPercentage(
                        titles, data.keywords(), data.minPercentage()),
                criterion + " recommendation relevance was " + percent + "%; expected at least " +
                        data.minPercentage() + "% from " + titles.size() + " visible titles: " + titles);
    }

    private static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }
}
