@showcase
Feature: Intent Healer Self-Healing Showcase
  This feature demonstrates Intent Healer's ability to automatically fix broken locators.
  Each scenario uses an INTENTIONALLY WRONG locator that would normally cause test failure.
  Intent Healer detects the failure and finds the correct element using AI-powered analysis.

  Background:
    Given Intent Healer is enabled with mock LLM provider

  # =============================================================================
  # TEST 1: Wrong ID - Element exists but with different ID
  # =============================================================================
  @test-1 @wrong-id
  Scenario: Test 1 - Login button with wrong ID
    Given I am on the Herokuapp login page
    When I enter username "tomsmith"
    And I enter password "SuperSecretPassword!"
    And I click the login button using wrong ID "login-btn"
    Then I should see the secure area
    # WRONG: By.id("login-btn")
    # REAL:  <button class="radius" type="submit">
    # HEALED: By.cssSelector or By.xpath with button attributes

  # =============================================================================
  # TEST 2: Wrong Class Name - Looking for non-existent class
  # =============================================================================
  @test-2 @wrong-class
  Scenario: Test 2 - Checkbox with wrong class name
    Given I am on the Herokuapp checkboxes page
    When I click the checkbox using wrong class "checkbox-input"
    Then the checkbox should be checked
    # WRONG: By.className("checkbox-input")
    # REAL:  <input type="checkbox"> (no class)
    # HEALED: By.cssSelector("input[type='checkbox']")

  # =============================================================================
  # TEST 3: Wrong Tag Type - Looking for link instead of button
  # =============================================================================
  @test-3 @wrong-tag
  Scenario: Test 3 - Add button mistaken for link
    Given I am on the Herokuapp add remove elements page
    When I click add element using link text "Add Element"
    Then a delete button should appear
    # WRONG: By.linkText("Add Element")
    # REAL:  <button onclick="addElement()">Add Element</button>
    # HEALED: By.xpath("//button[contains(text(),'Add Element')]")

  # =============================================================================
  # TEST 4: Wrong CSS Selector - Overly specific selector that doesn't exist
  # =============================================================================
  @test-4 @wrong-css
  Scenario: Test 4 - Dropdown with wrong CSS selector
    Given I am on the Herokuapp dropdown page
    When I select "Option 1" from dropdown using wrong CSS "select.dropdown-menu"
    Then the dropdown should show "Option 1"
    # WRONG: By.cssSelector("select.dropdown-menu")
    # REAL:  <select id="dropdown">
    # HEALED: By.id("dropdown")

  # =============================================================================
  # TEST 5: Wrong XPath - Looking for non-existent form structure
  # =============================================================================
  @test-5 @wrong-xpath
  Scenario: Test 5 - Login with wrong XPath for button
    Given I am on the Herokuapp login page
    When I enter username "tomsmith"
    And I enter password "SuperSecretPassword!"
    And I click login using wrong XPath "//form[@id='login-form']/button[@type='submit']"
    Then I should see the secure area
    # WRONG: By.xpath("//form[@id='login-form']/button[@type='submit']")
    # REAL:  <button class="radius" type="submit"> (different structure)
    # HEALED: By.cssSelector("button.radius") or similar

  # =============================================================================
  # TEST 6: Another Login Test - Wrong CSS selector for login button
  # =============================================================================
  @test-6 @wrong-css-submit
  Scenario: Test 6 - Login with wrong CSS for submit button
    Given I am on the Herokuapp login page
    When I enter username "tomsmith"
    And I enter password "SuperSecretPassword!"
    And I click login using wrong CSS "input.submit-btn"
    Then I should see the secure area
    # WRONG: By.cssSelector("input.submit-btn")
    # REAL:  <button class="radius" type="submit">
    # HEALED: By.cssSelector("button.radius") or type="submit"

  # =============================================================================
  # TEST 7: Dynamically Generated ID - UUID-like ID that changes
  # =============================================================================
  @test-7 @dynamic-id
  Scenario: Test 7 - Challenging DOM with dynamic ID
    Given I am on the Herokuapp challenging DOM page
    When I click the first action button using wrong ID "btn-action-12345"
    Then the page should remain functional
    # WRONG: By.id("btn-action-12345")
    # REAL:  <a class="button" href="#edit">edit</a>
    # HEALED: By.cssSelector("a.button") or By.linkText("edit")

  # =============================================================================
  # TEST 8: Wrong Element Type - Submit input vs button
  # =============================================================================
  @test-8 @wrong-type
  Scenario: Test 8 - Dynamic loading start button with wrong type
    Given I am on the Herokuapp dynamic loading page
    When I click start using wrong element type "input[type='submit']"
    And I wait for the loading to complete
    Then the finish message should be visible
    # WRONG: By.cssSelector("input[type='submit']")
    # REAL:  <button>Start</button>
    # HEALED: By.xpath("//button[contains(text(),'Start')]")

  # =============================================================================
  # TEST 9: Another Add/Remove - Wrong ID for Add button
  # =============================================================================
  @test-9 @wrong-id-button
  Scenario: Test 9 - Add element button with wrong ID
    Given I am on the Herokuapp add remove elements page
    When I click add using wrong ID "add-button"
    Then a delete button should appear
    # WRONG: By.id("add-button")
    # REAL:  <button onclick="addElement()">Add Element</button> (no ID)
    # HEALED: By.xpath with text content or tag name

  # =============================================================================
  # TEST 10: Complex Nested Element - Deep DOM traversal failure
  # =============================================================================
  @test-10 @nested-element
  Scenario: Test 10 - Table cell with wrong nested selector
    Given I am on the Herokuapp tables page
    When I click edit for "Smith" using wrong selector "table#users tr[data-id='1'] a.edit-btn"
    Then the URL should contain "#edit"
    # WRONG: By.cssSelector("table#users tr[data-id='1'] a.edit-btn")
    # REAL:  <a href="#edit">edit</a> in a different table structure
    # HEALED: By.xpath with text matching or simpler CSS
