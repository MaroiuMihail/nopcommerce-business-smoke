package tests.smoke;

import core.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Smoke_SanityOpenHomeTest extends BaseTest {


    @Test
    public void openHome_shouldDisplaySearchBox(){

        WebElement searchBox = driver.findElement(By.id("small-searchterms"));
        boolean isVisible = searchBox.isDisplayed();

        Assert.assertTrue(isVisible,
                "Search box should be visible on Home page");
    }

}
