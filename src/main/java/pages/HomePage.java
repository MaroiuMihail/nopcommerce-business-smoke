package pages;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import core.BasePage;

import java.time.Duration;

public class HomePage extends BasePage {

    private final By searchInput = By.id("small-searchterms");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void searchFor(String text) {
        waitForAppReady();
        type(searchInput, text);
    }

    public boolean isSearchBoxVisible(){
        return isDisplayed(By.id("small-searchterms"));
    }

    private void waitForAppReady() {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(120));

        w.until(d -> {
            String url = d.getCurrentUrl();
            return url != null
                    && !url.isBlank()
                    && !url.startsWith("chrome-error://")
                    && !url.equals("about:blank");
        });


        String url = driver.getCurrentUrl().toLowerCase();
        if (url.contains("/install")) {
            throw new IllegalStateException("App is still on /install (not installed yet). URL=" + driver.getCurrentUrl());
        }


        w.until(ExpectedConditions.visibilityOfElementLocated(By.id("small-searchterms")));
    }
}
