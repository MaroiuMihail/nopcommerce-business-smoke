package tests.smoke;

import core.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import java.time.Duration;
import java.util.List;

public class Smoke_SearchTest extends BaseTest {

    private static final By SEARCH_RESULTS =
            By.cssSelector(".product-title a");

    private static final By ADD_TO_CART_BUTTON =
            By.cssSelector("button[id^='add-to-cart-button']");

    @Test
    public void search_shouldWork() {

        HomePage homePage = new HomePage(driver);
        homePage.searchFor("laptop");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(SEARCH_RESULTS));


        List<WebElement> results = driver.findElements(SEARCH_RESULTS);
        Assert.assertTrue(results.size() > 0,
                "No search results found");

        wait.until(ExpectedConditions.elementToBeClickable(SEARCH_RESULTS));
        driver.findElements(SEARCH_RESULTS).get(0).click();


        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(ADD_TO_CART_BUTTON));
        Assert.assertTrue(addToCart.isDisplayed(), "PDP not opened");

    }

}
