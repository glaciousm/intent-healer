@login @healing
Feature: User Login with Intent-Based Healing
  As a user
  I want to log into the application
  So that I can access my account

  Background:
    Given I am on the login page

  @happy-path
  Scenario: Successful login with valid credentials
    When I enter username "testuser@example.com"
    And I enter password "SecurePass123"
    And I click the login button
    Then I should be logged in successfully

  @negative
  Scenario: Login fails with invalid password
    When I enter username "testuser@example.com"
    And I enter password "wrongpassword"
    And I click the login button
    Then I should see an error message "Invalid credentials"
    And I should still be on the login page

  @negative
  Scenario: Login fails with empty credentials
    When I enter username ""
    And I enter password ""
    And I click the login button
    Then I should see an error message "required"
    And I should still be on the login page

  @locator-drift @healing
  Scenario: Login succeeds even after UI changes
    # This scenario demonstrates healing - even if button ID changes
    # from "login-btn" to "submit-auth", the healer will find it
    When I enter username "testuser@example.com"
    And I enter password "SecurePass123"
    And I click the login button
    Then I should be logged in successfully
