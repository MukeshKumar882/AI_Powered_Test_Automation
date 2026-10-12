Feature: Ulta shopping and recommendation relevance
  Background:
    Given the user launches the Ulta website

  @e2e @AC1 @AC2 @AC3 @AC4 @AC5 @AC6 @AC7
  Scenario: Complete all seven acceptance criteria in one browser session
    Then the search bar should be visible and enabled
    And the account sign-in control should be accessible

    When the user searches for "moisturizer"
    Then at least one search result should be displayed
    When the user searches for "mascara"
    Then at least one search result should be displayed
    When the user searches for "vitamin c serum"
    Then at least one search result should be displayed

    When the shopper adds "moisturizer" to the bag
    Then the cart badge count should be at least 1
    Then the bag should contain at least one product

    When the shopper adds "mascara" to the bag
    Then the cart badge count should be at least 2
    Then the bag should contain at least 2 products

    When the shopper adds "vitamin c serum" to the bag
    Then the cart badge count should be at least 3
    Then the bag should contain at least 3 products
    And the bag should contain all products added during this scenario

    When the shopper collects product page recommendations for the added products
    Then product page recommendations should meet the AC5 threshold
    When the shopper opens the bag
    Then bag recommendations should meet the AC6 threshold
    And combined recommendations should meet the AC7 threshold
