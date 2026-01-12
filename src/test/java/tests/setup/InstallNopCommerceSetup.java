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
    private static final Duration SHORT = Duration.ofSeconds(6);

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String ADMIN_PASS  = "Admin123!";

    private static final String SQL_SERVER = "sqlserver";
    private static final String SQL_DB     = "nopcommerce";
    private static final String SQL_USER   = "sa";
    private static final String SQL_PASS   = "StrongPassw0rd!2026";

    private static final String CONN =
            "Data Source=" + SQL_SERVER + ";Initial Catalog=" + SQL_DB + ";" +
                    "User ID=" + SQL_USER + ";Password=" + SQL_PASS + ";" +
                    "Encrypt=False;TrustServerCertificate=True;";

    private static final By INSTALL_BUTTON = By.cssSelector("#install-button, button#install-button, button[type='submit'], input[type='submit']");

    private static final By ADMIN_EMAIL_INPUT   = By.cssSelector("#AdminEmail, input[name='AdminEmail']");
    private static final By ADMIN_PASS_INPUT    = By.cssSelector("#AdminPassword, input[name='AdminPassword']");
    private static final By CONFIRM_PASS_INPUT  = By.cssSelector("#ConfirmPassword, input[name='ConfirmPassword']");

    private static final By SERVER_NAME_INPUT = By.cssSelector("#ServerName, input[name='ServerName']");
    private static final By DB_NAME_INPUT     = By.cssSelector("#DatabaseName, input[name='DatabaseName']");
    private static final By SQL_USER_INPUT    = By.cssSelector("#SqlUsername, input[name='SqlUsername']");
    private static final By SQL_PASS_INPUT    = By.cssSelector("#SqlPassword, input[name='SqlPassword']");

    private static final By CONNECTION_STRING_INPUT = By.cssSelector("#ConnectionString, input[name='ConnectionString']");

    private static final By DATA_PROVIDER_SELECT = By.cssSelector("#DataProvider, select[name='DataProvider']");
    private static final By INSTALL_SAMPLE_DATA  = By.cssSelector("#InstallSampleData, input[name='InstallSampleData']");

    private static final By ERRORS = By.cssSelector(".validation-summary-errors, .field-validation-error, .message-error, .alert-danger");

    @Test
    public void install_ifNeeded() {
        WebDriver driver = DriverFactory.createDriver();
        WebDriverWait longWait = new WebDriverWait(driver, LONG);

        try {
            driver.get(BASE_URL);

            if (!driver.getCurrentUrl().toLowerCase().contains("/install")) {
                return;
            }

            longWait.until(ExpectedConditions.presenceOfElementLocated(INSTALL_BUTTON));

            typeVisible(driver, ADMIN_EMAIL_INPUT, ADMIN_EMAIL, LONG);
            typeVisible(driver, ADMIN_PASS_INPUT, ADMIN_PASS, LONG);
            typeVisible(driver, CONFIRM_PASS_INPUT, ADMIN_PASS, LONG);

            selectIfPresent(driver, DATA_PROVIDER_SELECT, "SQL Server");

            boolean usedDbFields = tryFillDbFields(driver);
            boolean usedConnString = false;

            if (!usedDbFields) {
                usedConnString = tryFillConnectionString(driver);
            }

            if (!usedDbFields && !usedConnString) {
                dumpInputs(driver);
                throw new IllegalStateException(
                        "Could not find DB fields (SqlUsername etc.) AND could not use ConnectionString field. " +
                                "Installer UI variant is different or fields are hidden/disabled."
                );
            }

            clickIfPresent(driver, INSTALL_SAMPLE_DATA);

            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.elementToBeClickable(INSTALL_BUTTON));
            btn.click();

            try {
                longWait.until(d -> {
                    String url = d.getCurrentUrl().toLowerCase();
                    if (!url.contains("/install")) return true;
                    return !d.findElements(ERRORS).isEmpty();
                });
            } catch (TimeoutException e) {
                String err = readErrors(driver);
                throw new IllegalStateException("Install timed out and stayed on /install. Errors: " + err, e);
            }

            if (driver.getCurrentUrl().toLowerCase().contains("/install")) {
                String err = readErrors(driver);
                throw new IllegalStateException("Install failed and stayed on /install. Errors: " + err);
            }

            longWait.until(ExpectedConditions.presenceOfElementLocated(By.id("small-searchterms")));

        } finally {
            driver.quit();
        }
    }

    /**
     * Returns true if SqlUsername exists (meaning DB fields variant is present) and we filled it.
     * If SqlUsername is missing, we return false immediately (NO 180s wait).
     */
    private static boolean tryFillDbFields(WebDriver driver) {
        if (!exists(driver, SQL_USER_INPUT)) {
            return false; // UI variant without SqlUsername
        }

        typeVisible(driver, SERVER_NAME_INPUT, SQL_SERVER, SHORT);
        typeVisible(driver, DB_NAME_INPUT, SQL_DB, SHORT);
        typeVisible(driver, SQL_USER_INPUT, SQL_USER, SHORT);
        typeVisible(driver, SQL_PASS_INPUT, SQL_PASS, SHORT);
        return true;
    }

    /**
     * Returns true if ConnectionString exists AND is interactable.
     * We avoid ElementNotInteractable by checking displayed+enabled.
     */
    private static boolean tryFillConnectionString(WebDriver driver) {
        List<WebElement> els = driver.findElements(CONNECTION_STRING_INPUT);
        if (els.isEmpty()) return false;

        WebElement el = els.get(0);
        if (!el.isDisplayed() || !el.isEnabled()) return false;

        safeSetValue(driver, el, CONN);
        return true;
    }

    private static void typeVisible(WebDriver driver, By locator, String value, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(ExpectedConditions.elementToBeClickable(el));
        safeSetValue(driver, el, value);
    }

    private static void safeSetValue(WebDriver driver, WebElement el, String value) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        el.sendKeys(value);
    }

    private static boolean exists(WebDriver driver, By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private static void clickIfPresent(WebDriver driver, By locator) {
        List<WebElement> els = driver.findElements(locator);
        if (els.isEmpty()) return;
        WebElement el = els.get(0);
        try {
            if (!el.isSelected()) el.click();
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

    private static void dumpInputs(WebDriver driver) {
        System.out.println("=== INPUT DUMP ===");
        List<WebElement> inputs = driver.findElements(By.cssSelector("input, select, textarea"));
        for (WebElement in : inputs) {
            try {
                String tag = in.getTagName();
                String id = in.getAttribute("id");
                String name = in.getAttribute("name");
                String type = in.getAttribute("type");
                boolean disp = in.isDisplayed();
                boolean en = in.isEnabled();
                System.out.println(tag + " id=" + id + " name=" + name + " type=" + type + " displayed=" + disp + " enabled=" + en);
            } catch (Exception ignored) {}
        }
    }
}
