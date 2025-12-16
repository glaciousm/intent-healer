@herokuapp @healing
Feature: Herokuapp Login with Self-Healing
  As a QA engineer using Intent Healer
  I want to test login functionality on the-internet.herokuapp.com
  So that I can verify the self-healing feature works with real websites

  Background:
    Given I am on the Herokuapp login page

  @smoke @healing-test
  Scenario: Successful login triggers healing for wrong locator
    When I enter username "tomsmith"
    And I enter password "SuperSecretPassword!"
    And I click the login button
    Then I should see the secure area page
    And I should see a success message containing "You logged into a secure area!"

  @smoke
  Scenario: Logout after successful login
    When I login with username "tomsmith" and password "SuperSecretPassword!"
    Then I should see the secure area page
    When I click the logout button
    Then I should be back on the login page

  @negative
  Scenario: Login with invalid credentials shows error
    When I login with username "invalid" and password "wrongpassword"
    Then I should see an error message containing "Your username is invalid!"
    And I should still be on the login page

  @negative
  Scenario: Login with empty credentials shows error
    When I click the login button
    Then I should see an error message containing "Your username is invalid!"
