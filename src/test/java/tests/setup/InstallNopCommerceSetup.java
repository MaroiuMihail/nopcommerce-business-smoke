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

    private static final String SQL_USER = "sa";
    private static final String SQL_PASS = "yourStrong(!)Password";
    private static final String SQL_SERVER = "sqlserver";
    private static final String SQL_DB = "nopcommerce";

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
                    By.cssSelector("#install-button, button[type='submit'], input[type='submit']")
            ));


            typeIfPresent(driver, By.cssSelector(
                    "#AdminEmail, input[name='AdminEmail'], input[name*='AdminEmail']"
            ), ADMIN_EMAIL);


            setPasswordField(driver, "adminpassword", ADMIN_PASS);
            setPasswordField(driver, "confirmpassword", ADMIN_PASS);


            selectByContainsIfPresent(driver,
                    By.cssSelector("#DataProvider, select[name='DataProvider'], select[name*='DataProvider']"),
                    "sql"
            );


            String SQL_SERVER = "sqlserver";
            String SQL_DB = "nopcommerce";
            String SQL_USER = "sa";
            String SQL_PASS = "yourStrong(!)Password";


            clickByIdOrNameContainsIfPresent(driver, "sqlauth");


            boolean connSet = typeIfPresent(driver, By.cssSelector(
                            "#ConnectionString, input[name='ConnectionString'], input[id*='ConnectionString'], input[name*='ConnectionString']"
                    ),
                    "Data Source=sqlserver;Initial Catalog=nopcommerce;User ID=sa;Password=yourStrong(!)Password;Encrypt=False;TrustServerCertificate=True"
            );


            boolean serverOk = typeByIdOrNameContains(driver, "server", SQL_SERVER);
            boolean dbOk = typeByIdOrNameContains(driver, "database", SQL_DB);
            boolean userOk = typeByIdOrNameContains(driver, "user", SQL_USER);
            boolean passOk =
                    typePasswordByIdOrNameContains(driver, "sqlpassword", SQL_PASS)
                            || typePasswordByIdOrNameContains(driver, "dbpassword", SQL_PASS)
                            || typePasswordByIdOrNameContains(driver, "password", SQL_PASS);


            if (!connSet && !(serverOk && userOk && passOk)) {
                throw new IllegalStateException(
                        "DB fields not filled: connSet=" + connSet +
                                ", serverOk=" + serverOk +
                                ", dbOk=" + dbOk +
                                ", userOk=" + userOk +
                                ", passOk=" + passOk
                );
            }



            clickIfPresent(driver, By.cssSelector("#InstallSampleData, input[name='InstallSampleData']"));


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

    private static void setInputValue(WebDriver driver, WebElement el, String value) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        waitClickable(driver, el).click();

        el.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        el.sendKeys(Keys.DELETE);
        el.sendKeys(value);
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

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private static boolean typeByIdOrNameContains(WebDriver driver, String containsLower, String value) {
        String key = containsLower.toLowerCase();
        List<WebElement> inputs = driver.findElements(By.cssSelector("input"));

        for (WebElement in : inputs) {
            String id = safeLower(in.getAttribute("id"));
            String name = safeLower(in.getAttribute("name"));
            String type = safeLower(in.getAttribute("type"));

            boolean textLike = type.isEmpty() || type.equals("text") || type.equals("tel") || type.equals("email") || type.equals("search");
            if (!textLike) continue;

            if (id.contains(key) || name.contains(key)) {
                if (!in.isDisplayed() || !in.isEnabled()) return false;
                setInputValue(driver, in, value);
                return true;
            }
        }
        return false;
    }

    private static void clickByIdOrNameContainsIfPresent(WebDriver driver, String containsLower) {
        String key = containsLower.toLowerCase();
        List<WebElement> els = driver.findElements(By.cssSelector("input[type='checkbox'], input[type='radio']"));

        for (WebElement e : els) {
            String id = safeLower(e.getAttribute("id"));
            String name = safeLower(e.getAttribute("name"));

            if (id.contains(key) || name.contains(key)) {
                if (!e.isDisplayed() || !e.isEnabled()) return;
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", e);
                if (!e.isSelected()) e.click();
                return;
            }
        }
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



}
