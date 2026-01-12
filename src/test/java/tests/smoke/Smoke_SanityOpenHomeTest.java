package tests.smoke;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;

public class Smoke_SanityOpenHomeTest extends BaseTest {


    @Test
    public void openHome_shouldDisplaySearchBox(){

        HomePage homePage = new HomePage(driver);
        boolean isVisible = homePage.isSearchBoxVisible();

        Assert.assertTrue(isVisible,
                "Search box should be visible on Home page");
    }

}
