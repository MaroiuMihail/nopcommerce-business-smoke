package tests.setup;

import core.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class InstallNopCommerceSetup {

    private static final String BASE_URL = "http://127.0.0.1:5000";
    private static final Duration LONG = Duration.ofSeconds(600); // 10 min

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String ADMIN_PASS  = "Admin123!";

    private static final String SQL_SERVER = "sqlserver";           // docker service name
    private static final String SQL_DB     = "nopcommerce";
    private static final String SQL_USER   = "sa";
    private static final String SQL_PASS   = "yourStrong(!)Password";

    private static final By INSTALL_BUTTON = By.cssSelector("#install-button, button#install-button, button[type='submit'], input[type='submit']");
    private static final By ERRORS = By.cssSelector(".validation-summary-errors, .field-validation-error, .message-error, .alert-danger");

    private static final By ADMIN_EMAIL_INPUT  = By.cssSelector("#AdminEmail, input[name='AdminEmail']");
    private static final By ADMIN_PASS_INPUT   = By.cssSelector("#AdminPassword, input[name='AdminPassword']");
    private static final By CONFIRM_PASS_INPUT = By.cssSelector("#ConfirmPassword, input[name='ConfirmPassword']");

    private static final By SERVER_NAME_INPUT = By.cssSelector("#ServerName, input[name='ServerName']");
    private static final By DB_NAME_INPUT     = By.cssSelector("#DatabaseName, input[name='DatabaseName']");
    private static final By USERNAME_INPUT    = By.cssSelector("#Username, input[name='Username']");
    private static final By PASSWORD_INPUT    = By.cssSelector("#Password, input[name='Password']");

    private static final By INTEGRATED_SECURITY = By.cssSelector("#IntegratedSecurity, input[name='IntegratedSecurity']");
    private static final By CREATE_DB_IF_NOT_EXISTS = By.cssSelector("#CreateDatabaseIfNotExists, input[name='CreateDatabaseIfNotExists']");
    private static final By CONNECTION_STRING_RAW = By.cssSelector("#ConnectionStringRaw, input[name='ConnectionStringRaw']");
    private static final By INSTALL_SAMPLE_DATA  = By.cssSelector("#InstallSampleData, input[name='InstallSampleData']");

    private static final By HOME_READY = By.id("small-searchterms");

    @Test
    public void install_ifNeeded() {
        WebDriver driver = DriverFactory.createDriver();
        WebDriverWait wait = new WebDriverWait(driver, LONG);

        try {
            driver.get(BASE_URL + "/install");

            if (!driver.getCurrentUrl().toLowerCase().contains("/install")) return;

            wait.until(ExpectedConditions.presenceOfElementLocated(INSTALL_BUTTON));

            typeVisible(driver, wait, ADMIN_EMAIL_INPUT, ADMIN_EMAIL);
            typeVisible(driver, wait, ADMIN_PASS_INPUT, ADMIN_PASS);
            typeVisible(driver, wait, CONFIRM_PASS_INPUT, ADMIN_PASS);

            setCheckbox(driver, CONNECTION_STRING_RAW, false);

            setCheckbox(driver, INTEGRATED_SECURITY, false);

            setCheckbox(driver, CREATE_DB_IF_NOT_EXISTS, true);

            typeVisible(driver, wait, SERVER_NAME_INPUT, SQL_SERVER);
            typeVisible(driver, wait, DB_NAME_INPUT, SQL_DB);
            typeVisible(driver, wait, USERNAME_INPUT, SQL_USER);
            typeVisible(driver, wait, PASSWORD_INPUT, SQL_PASS);

            clickIfPresent(driver, INSTALL_SAMPLE_DATA);

            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(INSTALL_BUTTON));
            btn.click();

            boolean finished = false;
            long end = System.currentTimeMillis() + LONG.toMillis();

            while (System.currentTimeMillis() < end) {
                String url = safeLower(driver.getCurrentUrl());

                if (!url.contains("/install")) {
                    finished = true;
                    break;
                }

                String err = readErrors(driver);
                if (err != null && !err.isBlank() && !err.equals("(no visible errors)")) {
                    throw new IllegalStateException("Install failed and stayed on /install. Errors: " + err);
                }

                try {
                    Thread.sleep(2000);
                    driver.navigate().refresh();
                } catch (Exception ignored) {}
            }

            if (!finished) {
                throw new IllegalStateException("Install timeout: stayed on /install with no visible errors (likely app restart loop or DB issue).");
            }

            wait.until(ExpectedConditions.presenceOfElementLocated(HOME_READY));

        } finally {
            driver.quit();
        }
    }

    private static void typeVisible(WebDriver driver, WebDriverWait wait, By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        wait.until(ExpectedConditions.elementToBeClickable(el));
        scrollIntoView(driver, el);
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        el.sendKeys(value);
    }

    private static void setCheckbox(WebDriver driver, By locator, boolean shouldBeChecked) {
        List<WebElement> els = driver.findElements(locator);
        if (els.isEmpty()) return;

        WebElement cb = els.get(0);
        scrollIntoView(driver, cb);

        boolean checked = cb.isSelected();
        if (checked != shouldBeChecked) cb.click();
    }

    private static void clickIfPresent(WebDriver driver, By locator) {
        List<WebElement> els = driver.findElements(locator);
        if (els.isEmpty()) return;
        try {
            WebElement el = els.get(0);
            scrollIntoView(driver, el);
            if ("checkbox".equalsIgnoreCase(el.getAttribute("type"))) {
                if (!el.isSelected()) el.click();
            } else {
                el.click();
            }
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

    private static void scrollIntoView(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {}
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}
