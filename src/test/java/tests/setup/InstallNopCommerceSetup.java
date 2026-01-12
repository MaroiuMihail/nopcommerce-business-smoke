package tests.setup;

import core.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class InstallNopCommerceSetup {

    private static final String BASE_URL = "http://localhost:5000";
    private static final Duration LONG = Duration.ofSeconds(180);

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String ADMIN_PASS  = "Admin123!";

    private static final String SQL_SERVER = "sqlserver";
    private static final String SQL_DB     = "nopcommerce";
    private static final String SQL_USER   = "sa";
    private static final String SQL_PASS   = "StrongPassw0rd!2026";

    private static final By INSTALL_BUTTON = By.cssSelector("#install-button, button#install-button, button[type='submit'], input[type='submit']");
    private static final By ADMIN_EMAIL_INPUT = By.cssSelector("#AdminEmail, input[name='AdminEmail']");
    private static final By ADMIN_PASS_INPUT = By.cssSelector("#AdminPassword, input[name='AdminPassword']");
    private static final By CONFIRM_PASS_INPUT = By.cssSelector("#ConfirmPassword, input[name='ConfirmPassword']");

    private static final By SERVER_NAME_INPUT = By.cssSelector("#ServerName, input[name='ServerName']");
    private static final By DB_NAME_INPUT = By.cssSelector("#DatabaseName, input[name='DatabaseName']");
    private static final By SQL_USER_INPUT = By.cssSelector("#SqlUsername, input[name='SqlUsername']");
    private static final By SQL_PASS_INPUT = By.cssSelector("#SqlPassword, input[name='SqlPassword']");

    private static final By DATA_PROVIDER_SELECT = By.cssSelector("#DataProvider, select[name='DataProvider']");
    private static final By INSTALL_SAMPLE_DATA = By.cssSelector("#InstallSampleData, input[name='InstallSampleData']");
    private static final By ERRORS = By.cssSelector(".validation-summary-errors, .field-validation-error, .message-error, .alert-danger");

    @Test
    public void install_ifNeeded() {
        WebDriver driver = DriverFactory.createDriver();
        WebDriverWait wait = new WebDriverWait(driver, LONG);

        try {
            driver.get(BASE_URL);

            if (!driver.getCurrentUrl().toLowerCase().contains("/install")) {
                return;
            }

            wait.until(ExpectedConditions.presenceOfElementLocated(INSTALL_BUTTON));

            type(wait, driver, ADMIN_EMAIL_INPUT, ADMIN_EMAIL);
            type(wait, driver, ADMIN_PASS_INPUT, ADMIN_PASS);
            type(wait, driver, CONFIRM_PASS_INPUT, ADMIN_PASS);

            selectIfPresent(driver, DATA_PROVIDER_SELECT, "SQL Server");

            type(wait, driver, SERVER_NAME_INPUT, SQL_SERVER);
            type(wait, driver, DB_NAME_INPUT, SQL_DB);
            type(wait, driver, SQL_USER_INPUT, SQL_USER);
            type(wait, driver, SQL_PASS_INPUT, SQL_PASS);

            clickIfPresent(wait, driver, INSTALL_SAMPLE_DATA);

            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(INSTALL_BUTTON));
            btn.click();

            wait.until(d -> {
                String url = d.getCurrentUrl().toLowerCase();
                if (!url.contains("/install")) return true;
                return !d.findElements(ERRORS).isEmpty();
            });

            if (driver.getCurrentUrl().toLowerCase().contains("/install")) {
                String err = readErrors(driver);
                throw new IllegalStateException("Install failed and stayed on /install. Errors: " + err);
            }

            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("small-searchterms")));

        } finally {
            driver.quit();
        }
    }

    private static void type(WebDriverWait wait, WebDriver driver, By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(ExpectedConditions.elementToBeClickable(el));
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        el.sendKeys(value);
    }

    private static void clickIfPresent(WebDriverWait wait, WebDriver driver, By locator) {
        List<WebElement> els = driver.findElements(locator);
        if (els.isEmpty()) return;
        WebElement el = els.get(0);
        try {
            if (!el.isSelected()) {
                wait.until(ExpectedConditions.elementToBeClickable(el)).click();
            }
        } catch (Exception ignored) {}
    }

    private static void selectIfPresent(WebDriver driver, By locator, String visibleText) {
        List<WebElement> els = driver.findElements(locator);
        if (els.isEmpty()) return;
        try {
            new Select(els.get(0)).selectByVisibleText(visibleText);
        } catch (Exception ignored) {}
    }

    private static String readErrors(WebDriver driver) {
        List<WebElement> errs = driver.findElements(ERRORS);
        if (errs.isEmpty()) return "(no visible errors)";
        StringBuilder sb = new StringBuilder();
        for (WebElement e : errs) {
            String t = e.getText();
            if (t != null) {
                t = t.trim();
                if (!t.isEmpty()) {
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append(t.replace("\n", " "));
                }
            }
        }
        return sb.length() == 0 ? "(no visible errors)" : sb.toString();
    }
}
