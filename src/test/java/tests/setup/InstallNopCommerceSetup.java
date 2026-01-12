package tests.setup;

import core.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class InstallNopCommerceSetup {

    private static final String BASE_URL = "http://localhost:5000";
    private static final String ADMIN_PASS = "Admin123!";
    private static final Duration LONG = Duration.ofSeconds(180);

    @Test
    public void install_ifNeeded() {
        WebDriver driver = DriverFactory.createDriver();
        WebDriverWait wait = new WebDriverWait(driver, LONG);

        try {
            driver.get(BASE_URL + "/install");

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#install-button, button[type='submit'], input[type='submit']")
            ));


            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#install-button, button[type='submit'], input[type='submit']")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#AdminEmail, input[name='AdminEmail']"))
            ));


            if (!driver.getCurrentUrl().toLowerCase().contains("/install")) {
                return;
            }

            typeIfPresent(driver, By.cssSelector("#AdminEmail, input[name='AdminEmail']"), "admin@test.com");

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[type='password']")
            ));

            typeIfPresent(driver,
                    By.cssSelector("#AdminPassword, input[name='AdminPassword'], input[type='password'][name*='Admin'], input[type='password'][id*='Admin']"),
                    ADMIN_PASS);

            typeIfPresent(driver,
                    By.cssSelector("#ConfirmPassword, input[name='ConfirmPassword'], input[type='password'][name*='Confirm'], input[type='password'][id*='Confirm']"),
                    ADMIN_PASS);


            String conn =
                    "Data Source=sqlserver;Initial Catalog=nopcommerce;" +
                            "User ID=sa;Password=yourStrong(!)Password;" +
                            "TrustServerCertificate=True;Encrypt=False";

            selectByContainsIfPresent(driver, By.cssSelector("#DataProvider, select[name='DataProvider']"), "sql");



            boolean connStringSet = typeIfPresent(driver,
                    By.cssSelector("#ConnectionString, input[name='ConnectionString']"),
                    conn);

            if (!connStringSet) {

                typeIfPresent(driver, By.cssSelector("#ServerName, input[name='ServerName'], input[name*='Server'], input[name*='DataSource']"), "sqlserver");
                typeIfPresent(driver, By.cssSelector("#DatabaseName, input[name='DatabaseName'], input[name*='Database']"), "nopcommerce");
                typeIfPresent(driver, By.cssSelector("#SqlUsername, input[name='SqlUsername'], #Username, input[name='Username'], input[name*='Username']"), "sa");
                typeIfPresent(driver, By.cssSelector("#SqlPassword, input[name='SqlPassword'], #Password, input[name='Password'], input[name*='Password']"), "yourStrong(!)Password");
            }

            clickIfPresent(driver, By.cssSelector("#InstallSampleData, input[name='InstallSampleData']"));

            WebElement installBtn = findFirst(driver,
                    By.cssSelector("#install-button, button[type='submit'], input[type='submit']"));
            if (installBtn == null) {
                throw new IllegalStateException("Install button not found on /install page");
            }
            installBtn.click();

            try {
                wait.until(d -> {
                    String url = d.getCurrentUrl().toLowerCase();
                    if (!url.contains("/install")) return true;

                    String err = getInstallErrorText(d);
                    return err != null && !err.isBlank();
                });
            } catch (org.openqa.selenium.TimeoutException e) {
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

    private static boolean typeIfPresent(WebDriver driver, By locator, String text) {
        WebElement el = findFirst(driver, locator);
        if (el == null) return false;

        try {
            if (!el.isDisplayed() || !el.isEnabled()) return false;

            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", el);

            el.click();

            el.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
            el.sendKeys(org.openqa.selenium.Keys.DELETE);

            el.sendKeys(text);
            return true;

        } catch (org.openqa.selenium.ElementNotInteractableException e) {
            return false;
        }
    }


    private static void selectByContainsIfPresent(WebDriver driver, By locator, String containsText) {
        WebElement el = findFirst(driver, locator);
        if (el == null) return;

        Select select = new Select(el);
        String needle = containsText.toLowerCase();

        for (WebElement opt : select.getOptions()) {
            String txt = opt.getText() == null ? "" : opt.getText().toLowerCase();
            if (txt.contains(needle)) {
                select.selectByVisibleText(opt.getText());
                return;
            }
        }
    }


    private static void clickIfPresent(WebDriver driver, By locator) {
        WebElement el = findFirst(driver, locator);
        if (el == null) return;
        if (!el.isSelected()) el.click();
    }

    private static WebElement findFirst(WebDriver driver, By locator) {
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

}
