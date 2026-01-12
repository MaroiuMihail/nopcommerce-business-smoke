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
    private static final Duration LONG = Duration.ofSeconds(180);

    @Test
    public void install_ifNeeded() {
        WebDriver driver = DriverFactory.createDriver();
        WebDriverWait wait = new WebDriverWait(driver, LONG);

        try {
            driver.get(BASE_URL + "/install");

            if (!driver.getCurrentUrl().toLowerCase().contains("/install")) {
                return;
            }

            typeIfPresent(driver, By.cssSelector("#AdminEmail, input[name='AdminEmail']"), "admin@test.com");
            typeIfPresent(driver, By.cssSelector("#AdminPassword, input[name='AdminPassword']"), "Admin123!");
            typeIfPresent(driver, By.cssSelector("#ConfirmPassword, input[name='ConfirmPassword']"), "Admin123!");

            String conn =
                    "Data Source=sqlserver;Initial Catalog=nopcommerce;" +
                            "User ID=sa;Password=yourStrong(!)Password;" +
                            "TrustServerCertificate=True;Encrypt=False";

            boolean connStringSet = typeIfPresent(driver,
                    By.cssSelector("#ConnectionString, input[name='ConnectionString']"),
                    conn);

            if (!connStringSet) {

                typeIfPresent(driver, By.cssSelector("#ServerName, input[name='ServerName']"), "sqlserver");
                typeIfPresent(driver, By.cssSelector("#DatabaseName, input[name='DatabaseName']"), "nopcommerce");
                typeIfPresent(driver, By.cssSelector("#SqlUsername, input[name='SqlUsername']"), "sa");
                typeIfPresent(driver, By.cssSelector("#SqlPassword, input[name='SqlPassword']"), "yourStrong(!)Password");
            }

            selectByTextIfPresent(driver, By.cssSelector("#DataProvider, select[name='DataProvider']"), "SQL Server");

            clickIfPresent(driver, By.cssSelector("#InstallSampleData, input[name='InstallSampleData']"));

            WebElement installBtn = findFirst(driver,
                    By.cssSelector("#install-button, button[type='submit'], input[type='submit']"));
            if (installBtn == null) {
                throw new IllegalStateException("Install button not found on /install page");
            }
            installBtn.click();

            wait.until(d -> {
                try {
                    d.get(BASE_URL);
                    List<WebElement> els = d.findElements(By.id("small-searchterms"));
                    return !els.isEmpty() && els.get(0).isDisplayed();
                } catch (Exception e) {
                    return false;
                }
            });

        } finally {
            driver.quit();
        }
    }

    private static boolean typeIfPresent(WebDriver driver, By locator, String text) {
        WebElement el = findFirst(driver, locator);
        if (el == null) return false;
        el.clear();
        el.sendKeys(text);
        return true;
    }

    private static void selectByTextIfPresent(WebDriver driver, By locator, String visibleText) {
        WebElement el = findFirst(driver, locator);
        if (el == null) return;
        try {
            new Select(el).selectByVisibleText(visibleText);
        } catch (Exception ignored) {

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
}
