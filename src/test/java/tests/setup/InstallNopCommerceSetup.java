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

    private static final String SQL_SERVER = "sqlserver,1433";
    private static final String SQL_DB     = "nopCommerce";
    private static final String SQL_USER   = "sa";
    private static final String SQL_PASS   = "yourStrong(!)Password";


    @Test
    public void install_ifNeeded() {
        WebDriver driver = DriverFactory.createDriver();
        WebDriverWait wait = new WebDriverWait(driver, LONG);

        try {
            driver.get(BASE_URL + "/install");

            if (!driver.getCurrentUrl().toLowerCase().contains("/install")) {
                return;
            }

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#install-button, button#install-button, button[type='submit'], input[type='submit']")
            ));

            // --- Admin Email ---
            typeIfPresent(driver, By.cssSelector(
                    "#AdminEmail, input[name='AdminEmail'], input[name*='AdminEmail'], input[id*='AdminEmail']"
            ), ADMIN_EMAIL);

            setPasswordField(driver, "adminpassword", ADMIN_PASS);
            setPasswordField(driver, "confirmpassword", ADMIN_PASS);

            selectByContainsIfPresent(driver,
                    By.cssSelector("#DataProvider, select[name='DataProvider'], select[name*='DataProvider'], select[id*='DataProvider']"),
                    "sql"
            );

            boolean connSet = false;


            boolean serverOk =
                    typeByIdOrNameContains(driver, "servername", SQL_SERVER) ||
                            typeByIdOrNameContains(driver, "server", SQL_SERVER) ||
                            typeByIdOrNameContains(driver, "datasource", SQL_SERVER);

            boolean dbOk =
                    typeByIdOrNameContains(driver, "databasename", SQL_DB) ||
                            typeByIdOrNameContains(driver, "database", SQL_DB) ||
                            typeByIdOrNameContains(driver, "initialcatalog", SQL_DB);

            boolean userOk =
                    typeByIdOrNameContains(driver, "sqlusername", SQL_USER) ||
                            typeByIdOrNameContains(driver, "username", SQL_USER) ||
                            typeByIdOrNameContains(driver, "userid", SQL_USER) ||
                            typeByIdOrNameContains(driver, "user", SQL_USER);

            boolean sqlPassSet =
                    typePasswordByIdOrNameContains(driver, "sqlpassword", SQL_PASS) ||
                            typePasswordByIdOrNameContains(driver, "dbpassword", SQL_PASS) ||
                            typePasswordByIdOrNameContains(driver, "mssqlpassword", SQL_PASS);

            if (!sqlPassSet) {
                fillRemainingDbPassword(driver, SQL_PASS);
                sqlPassSet = true;
            }

            if (!connSet && !(serverOk && dbOk && userOk && sqlPassSet)) {
                throw new IllegalStateException(
                        "DB fields not filled. connSet=" + connSet +
                                ", serverOk=" + serverOk +
                                ", dbOk=" + dbOk +
                                ", userOk=" + userOk +
                                ", sqlPassSet=" + sqlPassSet
                );
            }

            clickIfPresent(driver, By.cssSelector("#InstallSampleData, input[name='InstallSampleData'], input[id*='InstallSampleData']"));

            dumpPasswordInputs(driver);

            WebElement installBtn = firstOrNull(driver,
                    By.cssSelector("#install-button, button#install-button, button[type='submit'], input[type='submit']")
            );
            if (installBtn == null) {
                throw new IllegalStateException("Install button not found on /install page");
            }
            safeClick(driver, installBtn);

            try {
                wait.until(d -> {
                    String url = d.getCurrentUrl().toLowerCase();
                    if (!url.contains("/install")) return true;

                    String err = getInstallErrorText(d);
                    return err != null && !err.isBlank();
                });
            } catch (TimeoutException e) {
                String err = getInstallErrorText(driver);
                throw new IllegalStateException(
                        "Install timed out and stayed on /install. Visible errors: " + (err == null ? "(none)" : err),
                        e
                );
            }

            if (driver.getCurrentUrl().toLowerCase().contains("/install")) {
                String err = getInstallErrorText(driver);
                throw new IllegalStateException(
                        "nopCommerce install failed and stayed on /install. Visible errors: " + (err == null ? "(none)" : err)
                );
            }

            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("small-searchterms")));

        } finally {
            driver.quit();
        }
    }

    private static void setPasswordField(WebDriver driver, String keyLower, String value) {
        List<WebElement> inputs = driver.findElements(By.cssSelector("input[type='password']"));

        for (WebElement input : inputs) {
            String id = safeLower(input.getAttribute("id"));
            String name = safeLower(input.getAttribute("name"));

            if (id.contains(keyLower) || name.contains(keyLower)) {
                if (!input.isDisplayed() || !input.isEnabled()) {
                    throw new IllegalStateException("Password field found but not interactable: " + keyLower);
                }
                setInputValue(driver, input, value);
                return;
            }
        }

        StringBuilder dbg = new StringBuilder();
        for (int i = 0; i < inputs.size(); i++) {
            dbg.append("PW[").append(i).append("] id=").append(inputs.get(i).getAttribute("id"))
                    .append(" name=").append(inputs.get(i).getAttribute("name")).append(" ; ");
        }
        throw new IllegalStateException("Could not find password field matching: " + keyLower + ". Found: " + dbg);
    }

    private static boolean typePasswordByIdOrNameContains(WebDriver driver, String containsLower, String value) {
        String key = containsLower.toLowerCase();
        List<WebElement> inputs = driver.findElements(By.cssSelector("input[type='password']"));

        for (WebElement in : inputs) {
            String id = safeLower(in.getAttribute("id"));
            String name = safeLower(in.getAttribute("name"));

            if (id.contains(key) || name.contains(key)) {
                if (!in.isDisplayed() || !in.isEnabled()) return false;
                setInputValue(driver, in, value);
                return true;
            }
        }
        return false;
    }

    private static void fillRemainingDbPassword(WebDriver driver, String value) {
        List<WebElement> pw = driver.findElements(By.cssSelector("input[type='password']"));

        WebElement candidate = null;

        for (WebElement in : pw) {
            String id = safeLower(in.getAttribute("id"));
            String name = safeLower(in.getAttribute("name"));

            boolean isAdmin = id.contains("adminpassword") || name.contains("adminpassword");
            boolean isConfirm = id.contains("confirmpassword") || name.contains("confirmpassword");

            if (isAdmin || isConfirm) continue;

            candidate = in;
            break;
        }

        if (candidate == null) {
            throw new IllegalStateException("Could not find a non-admin password field to treat as SQL password.");
        }

        setInputValue(driver, candidate, value);
    }

    private static boolean typeByIdOrNameContains(WebDriver driver, String containsLower, String value) {
        String key = containsLower.toLowerCase();
        List<WebElement> inputs = driver.findElements(By.cssSelector("input"));

        for (WebElement in : inputs) {
            String id = safeLower(in.getAttribute("id"));
            String name = safeLower(in.getAttribute("name"));
            String type = safeLower(in.getAttribute("type"));

            boolean skip = type.equals("hidden") || type.equals("checkbox") || type.equals("radio")
                    || type.equals("submit") || type.equals("button");
            if (skip) continue;


            if (id.contains(key) || name.contains(key)) {
                if (!in.isDisplayed() || !in.isEnabled()) return false;
                setInputValue(driver, in, value);
                return true;
            }
        }
        return false;
    }

    private static boolean typeIfPresent(WebDriver driver, By locator, String text) {
        WebElement el = firstOrNull(driver, locator);
        if (el == null) return false;

        try {
            if (!el.isDisplayed() || !el.isEnabled()) return false;

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
            waitClickable(driver, el).click();

            el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            el.sendKeys(Keys.DELETE);
            el.sendKeys(text);
            return true;

        } catch (ElementNotInteractableException e) {
            return false;
        }
    }

    private static void setInputValue(WebDriver driver, WebElement el, String value) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        waitClickable(driver, el).click();

        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        el.sendKeys(value);
    }

    private static void selectByContainsIfPresent(WebDriver driver, By locator, String containsText) {
        WebElement el = firstOrNull(driver, locator);
        if (el == null) return;

        try {
            Select select = new Select(el);
            String needle = containsText.toLowerCase();

            for (WebElement opt : select.getOptions()) {
                String txt = opt.getText() == null ? "" : opt.getText().toLowerCase();
                if (txt.contains(needle)) {
                    select.selectByVisibleText(opt.getText());
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void clickIfPresent(WebDriver driver, By locator) {
        WebElement el = firstOrNull(driver, locator);
        if (el == null) return;

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
            if (!el.isSelected()) {
                waitClickable(driver, el).click();
            }
        } catch (Exception ignored) {
        }
    }

    private static void safeClick(WebDriver driver, WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        waitClickable(driver, el).click();
    }

    private static WebElement waitClickable(WebDriver driver, WebElement el) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(el));
    }

    private static WebElement firstOrNull(WebDriver driver, By locator) {
        List<WebElement> els = driver.findElements(locator);
        return els.isEmpty() ? null : els.get(0);
    }

    private static String getInstallErrorText(WebDriver driver) {
        By errorBlocks = By.cssSelector(
                ".validation-summary-errors, .field-validation-error, .message-error, .alert-danger, #error-list"
        );

        List<WebElement> errors = driver.findElements(errorBlocks);
        if (errors.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (WebElement e : errors) {
            String t = e.getText();
            if (t != null) {
                t = t.trim();
                if (!t.isEmpty()) {
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append(t.replace("\n", " "));
                }
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private static void dumpPasswordInputs(WebDriver driver) {
        List<WebElement> pw = driver.findElements(By.cssSelector("input[type='password']"));
        System.out.println("=== PASSWORD INPUTS DUMP (" + pw.size() + ") ===");
        for (int i = 0; i < pw.size(); i++) {
            WebElement e = pw.get(i);
            String id = e.getAttribute("id");
            String name = e.getAttribute("name");
            String val = e.getAttribute("value");
            int len = (val == null) ? 0 : val.length();
            System.out.println("PW[" + i + "] id=" + id + " name=" + name + " valueLen=" + len);
        }
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}
