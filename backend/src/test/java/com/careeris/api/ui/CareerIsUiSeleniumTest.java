package com.careeris.api.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * EC4206 Section 3.6 - UI test automation with Selenium WebDriver (Chrome).
 * Black-box system tests of the React frontend: they need the frontend (npm run dev) and backend running.
 *
 * Run:  mvn test -Dgroups=ui -DexcludedTestGroups=none -Dui.baseUrl=http://localhost:5173
 *       add -Dui.headless=false to watch the browser.
 *
 * Owner: EG/2023/5708 - Liyanarachchi L.A.E.E
 */
@Tag("ui")
@DisplayName("UI: Selenium smoke tests")
class CareerIsUiSeleniumTest {
    private static final String BASE_URL = System.getProperty("ui.baseUrl", "http://localhost:5173");
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (!"false".equals(System.getProperty("ui.headless"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1366,900", "--no-sandbox");
        driver = new ChromeDriver(options); // Selenium Manager downloads the matching driver automatically
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    private WebElement field(String name) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.name(name)));
    }

    private String statusMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".auth-form .form-message"))).getText();
    }

    private void registerStudent(String name, String email, String password, String confirm) {
        driver.get(BASE_URL + "/#register/student");
        field("fullName").sendKeys(name);
        field("email").sendKeys(email);
        field("password").sendKeys(password);
        field("confirmPassword").sendKeys(confirm);
        driver.findElement(By.cssSelector("button.auth-submit")).click();
    }

    @Test
    @DisplayName("UI-01: home page loads and 'Join CareerIS' opens the registration form")
    void homePageNavigation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("hero-title")));
        driver.findElement(By.cssSelector(".nav-actions .nav-join")).click();
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("auth-title")));
        assertThat(title.getText()).isEqualTo("Create your account");
        assertThat(driver.getCurrentUrl()).endsWith("#register/student");
    }

    @Test
    @DisplayName("UI-02: student registers through the form and sees a success message")
    void studentRegistration() {
        String email = "ui-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        registerStudent("Maya Perera", email, "Password123", "Password123");
        assertThat(statusMessage()).isEqualTo("Account created for " + email + ". You can sign in now.");
    }

    @Test
    @DisplayName("UI-03: mismatched confirm password shows an error and nothing is sent")
    void passwordMismatch() {
        registerStudent("Maya Perera", "mismatch@example.com", "Password123", "Password999");
        assertThat(statusMessage()).isEqualTo("The passwords do not match. Please check them and try again.");
    }

    @Test
    @DisplayName("UI-04: wrong password on sign-in shows a generic error")
    void wrongPassword() {
        String email = "ui-wrong-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        registerStudent("Maya Perera", email, "Password123", "Password123");
        statusMessage();
        driver.get(BASE_URL + "/#sign-in/student");
        driver.navigate().refresh();
        field("email").sendKeys(email);
        field("password").sendKeys("WrongPassword1");
        driver.findElement(By.cssSelector("button.auth-submit")).click();
        assertThat(statusMessage()).isEqualTo("Email or password is incorrect.");
    }

    @Test
    @DisplayName("UI-05: valid sign-in opens the student dashboard")
    void signInOpensDashboard() {
        String email = "ui-ok-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        registerStudent("Maya Perera", email, "Password123", "Password123");
        statusMessage();
        driver.get(BASE_URL + "/#sign-in/student");
        driver.navigate().refresh();
        field("email").sendKeys(email);
        field("password").sendKeys("Password123");
        driver.findElement(By.cssSelector("button.auth-submit")).click();
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dashboard-heading h1")));
        assertThat(heading.getText()).isEqualTo("Welcome, Maya.");
        assertThat(driver.getCurrentUrl()).endsWith("#dashboard/student");
    }

    @Test
    @DisplayName("UI-06: e-mail without a top-level domain is rejected on the registration form (BUG-05 fix)")
    void emailWithoutDomainRejected() {
        registerStudent("Maya Perera", "maya@localhost", "Password123", "Password123");
        assertThat(statusMessage()).contains("must be a valid e-mail address with a domain");
    }
}
