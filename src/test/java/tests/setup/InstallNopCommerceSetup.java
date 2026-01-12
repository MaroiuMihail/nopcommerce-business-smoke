package tests.setup;

import core.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class InstallNopCommerceSetup {

    private static final String BASE_URL = "http://localhost:5000";
    private static final Duration LONG = Duration.ofSeconds(180);

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String ADMIN_PASS  = "Admin123!";

    private static final String SQL_SERVER = "sqlserver";
    private static final String SQL_DB     = "nopcommerce";
    private static final String SQL_USER   = "sa";
    private static final String SQL_PASS   = "yourStrong(!)Password";

    private static final By INSTALL_BUTTON = By.cssSelector("#install-button, button#install-button, button[type='submit'], input[type='submit']");

    private static final By ADMIN_EMAIL_INPUT  = By.cssSelector("#AdminEmail, input[name='AdminEmail']");
    private static final By ADMIN_PASS_INPUT   = By.cssSelector("#AdminPassword, input[name='AdminPassword']");
    private static final By CONFIRM_PASS_INPUT = By.cssSelector("#ConfirmPassword, input[name='ConfirmPassword']");

    private static final By SERVER_NAME_INPUT = By.cssSelector("#ServerName, input[name='ServerName']");
    private static final By DB_NAME_INPUT     = By.cssSelector("#DatabaseName, input[name='DatabaseName']");
    private static final By USERNAME_INPUT    = By.cssSelector("#Username, input[name='Username']");
    private static final By PASSWORD_INPUT    = By.cssSelector("#Password, input[name='Password']");

    private static final By INTEGRATED_SECURITY = By.cssSelector("#IntegratedSecurity, input[name='IntegratedSecurity']");
    private static final By CREATE_DB_IF_NOT_EXISTS = By.cssSelector("#CreateDatabaseIfNotExists, input[name='CreateDatabaseIfNotExists']");

    private static final By DATA_PROVIDER_SELECT = By.cssSelector("#DataProvider, select[name='DataProvider']");
    private static final By INSTALL_SAMPLE_DATA  = By.cssSelector("#InstallSampleData, input[name='InstallSampleData']");

    private static final By ERROR_BLOCKS = By.cssSelector(
            ".validation-summary-errors, .field-validation-error, .message-error, .alert-danger, .alert, .error, #error-list"
    );

    @Test
    public void install_ifNeeded() {
        WebDriver driver = DriverFactory.createDriver();
        WebDriverWait wait = new WebDriverWait(driver, LONG);

        try {
            driver.get(BASE_URL + "/install");

            if (!driver.getCurrentUrl().toLowerCase().contains("/install")) {
                return;
            }

            wait.until(ExpectedConditions.presenceOfElementLocated(INSTALL_BUTTON));

            typeVisible(driver, wait, ADMIN_EMAIL_INPUT, ADMIN_EMAIL);
            typeVisible(driver, wait, ADMIN_PASS_INPUT, ADMIN_PASS);
            typeVisible(driver, wait, CONFIRM_PASS_INPUT, ADMIN_PASS);

            selectIfPresent(driver, DATA_PROVIDER_SELECT, "SQL Server");

            setCheckbox(driver, INTEGRATED_SECURITY, false);
            setCheckbox(driver, CREATE_DB_IF_NOT_EXISTS, true);


            typeVisible(driver, wait, SERVER_NAME_INPUT, SQL_SERVER);
            typeVisible(driver, wait, DB_NAME_INPUT, SQL_DB);
            typeVisible(driver, wait, USERNAME_INPUT, SQL_USER);
            typeVisible(driver, wait, PASSWORD_INPUT, SQL_PASS);

            clickIfPresent(driver, INSTALL_SAMPLE_DATA);

            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(INSTALL_BUTTON));
            scrollIntoView(driver, btn);
            btn.click();

            wait.until(d -> homeReady(d) || installCompletedScreen(d) || hasRealErrorText(d));

            if (installCompletedScreen(driver)) {
                clickContinueIfPresent(driver);
            }

            wait.until(d -> homeReady(d));

        } catch (TimeoutException te) {
            dumpState(driver, "TIMEOUT");
            throw te;
        } catch (RuntimeException re) {
            dumpState(driver, "RUNTIME_EXCEPTION");
            throw re;
        } finally {
            driver.quit();
        }
    }


    private static boolean homeReady(WebDriver d) {
        try {
            List<WebElement> els = d.findElements(By.id("small-searchterms"));
            return !els.isEmpty() && els.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasRealErrorText(WebDriver d) {
        String err = readErrors(d);
        return err != null && !err.isBlank() && !err.equals("(no visible errors)");
    }

    private static boolean installCompletedScreen(WebDriver d) {
        try {
            String body = d.findElement(By.tagName("body")).getText().toLowerCase();
            if (body.contains("installation") && (body.contains("completed") || body.contains("success"))) return true;

            return !findContinueCandidates(d).isEmpty();
        } catch (Exception e) {
            return false;
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
        if (checked != shouldBeChecked) {
            cb.click();
        }
    }

    private static void selectIfPresent(WebDriver driver, By locator, String visibleText) {
        List<WebElement> els = driver.findElements(locator);
        if (els.isEmpty()) return;
        try {
            new Select(els.get(0)).selectByVisibleText(visibleText);
        } catch (Exception ignored) {}
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

    private static void clickContinueIfPresent(WebDriver driver) {
        List<WebElement> candidates = findContinueCandidates(driver);
        for (WebElement el : candidates) {
            try {
                scrollIntoView(driver, el);
                el.click();
                return;
            } catch (Exception ignored) {}
        }
    }

    private static List<WebElement> findContinueCandidates(WebDriver d) {
        List<WebElement> found = new ArrayList<>();
        List<WebElement> linksButtons = d.findElements(By.cssSelector("a, button"));
        for (WebElement el : linksButtons) {
            String txt = el.getText() == null ? "" : el.getText().trim().toLowerCase();
            String href = el.getAttribute("href") == null ? "" : el.getAttribute("href").toLowerCase();

            boolean looksLikeContinue =
                    txt.contains("continue") || txt.contains("homepage") || txt.contains("home") || txt.contains("go to");

            boolean looksLikeHomeLink =
                    href.endsWith("/") || href.contains("localhost:5000/") || href.endsWith("/en") || href.contains("/home");

            if (looksLikeContinue || looksLikeHomeLink) {
                try {
                    if (el.isDisplayed() && el.isEnabled()) found.add(el);
                } catch (Exception ignored) {}
            }
        }
        return found;
    }


    private static String readErrors(WebDriver driver) {
        List<WebElement> errs = driver.findElements(ERROR_BLOCKS);
        StringBuilder sb = new StringBuilder();
        for (WebElement e : errs) {
            String t = safeText(e);
            if (!t.isBlank()) append(sb, t);
        }

        String body = "";
        try { body = driver.findElement(By.tagName("body")).getText(); } catch (Exception ignored) {}
        String lower = body.toLowerCase();

        if (lower.contains("setup failed") || lower.contains("login failed") || lower.contains("error occurred")) {

            String snippet = body.replace("\n", " ");
            if (snippet.length() > 240) snippet = snippet.substring(0, 240) + "...";
            append(sb, snippet);
        }

        return sb.length() == 0 ? "(no visible errors)" : sb.toString();
    }

    private static void dumpState(WebDriver driver, String tag) {
        try {
            System.out.println("=== INSTALL DEBUG DUMP [" + tag + "] ===");
            System.out.println("URL: " + driver.getCurrentUrl());
            System.out.println("TITLE: " + driver.getTitle());

            dumpValue(driver, "AdminEmail", ADMIN_EMAIL_INPUT);
            dumpValue(driver, "AdminPassword", ADMIN_PASS_INPUT);
            dumpValue(driver, "ConfirmPassword", CONFIRM_PASS_INPUT);
            dumpValue(driver, "ServerName", SERVER_NAME_INPUT);
            dumpValue(driver, "DatabaseName", DB_NAME_INPUT);
            dumpValue(driver, "Username", USERNAME_INPUT);
            dumpValue(driver, "Password", PASSWORD_INPUT);

            List<WebElement> cont = findContinueCandidates(driver);
            System.out.println("Continue candidates: " + cont.size());
            for (WebElement el : cont) {
                String txt = safeText(el);
                String href = el.getAttribute("href");
                System.out.println(" - [" + txt + "] href=" + href);
            }

            System.out.println("Errors: " + readErrors(driver));

            String src = driver.getPageSource();
            if (src != null) {
                src = src.replace("\n", " ").replace("\r", " ");
                if (src.length() > 1200) src = src.substring(0, 1200) + "...";
                System.out.println("HTML(head): " + src);
            }
            System.out.println("=== END DEBUG DUMP ===");
        } catch (Exception ignored) {}
    }

    private static void dumpValue(WebDriver driver, String label, By locator) {
        try {
            List<WebElement> els = driver.findElements(locator);
            if (els.isEmpty()) {
                System.out.println(label + ": (not found)");
                return;
            }
            WebElement el = els.get(0);
            String v = el.getAttribute("value");
            int len = v == null ? 0 : v.length();
            System.out.println(label + ": valueLen=" + len + " displayed=" + el.isDisplayed());
        } catch (Exception e) {
            System.out.println(label + ": (error reading)");
        }
    }

    private static String safeText(WebElement e) {
        try {
            String t = e.getText();
            return t == null ? "" : t.trim().replace("\n", " ");
        } catch (Exception ex) {
            return "";
        }
    }

    private static void append(StringBuilder sb, String t) {
        if (t == null) return;
        t = t.trim();
        if (t.isEmpty()) return;
        if (sb.length() > 0) sb.append(" | ");
        sb.append(t);
    }

    private static void scrollIntoView(WebDriver driver, WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {}
    }
}
