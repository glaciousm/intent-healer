package com.intenthealer.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page object for the login page.
 * Demonstrates how locators might change and need healing.
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators - these might become stale after UI changes
    private static final By USERNAME_INPUT = By.id("username");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-btn");
    private static final By ERROR_MESSAGE = By.cssSelector(".error-message");
    private static final By REMEMBER_ME_CHECKBOX = By.id("remember-me");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Navigate to the login page.
     */
    public LoginPage navigate(String baseUrl) {
        driver.get(baseUrl + "/login");
        return this;
    }

    /**
     * Enter username.
     */
    public LoginPage enterUsername(String username) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        input.clear();
        input.sendKeys(username);
        return this;
    }

    /**
     * Enter password.
     */
    public LoginPage enterPassword(String password) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        input.clear();
        input.sendKeys(password);
        return this;
    }

    /**
     * Click the login button.
     */
    public void clickLogin() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));
        button.click();
    }

    /**
     * Check the remember me checkbox.
     */
    public LoginPage checkRememberMe() {
        WebElement checkbox = wait.until(ExpectedConditions.elementToBeClickable(REMEMBER_ME_CHECKBOX));
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
        return this;
    }

    /**
     * Get the error message if displayed.
     */
    public String getErrorMessage() {
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
            return error.getText();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if error message is displayed.
     */
    public boolean isErrorDisplayed() {
        try {
            return driver.findElement(ERROR_MESSAGE).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Perform complete login.
     */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }
}
