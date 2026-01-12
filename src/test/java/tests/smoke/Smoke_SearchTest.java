package tests.smoke;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.HomePage;
import pages.ProductDetailsPage;
import pages.SearchResultsPage;

public class Smoke_SearchTest extends BaseTest {


    @Test
    public void search_shouldWork() {

        HomePage homePage = new HomePage(driver);
        SearchResultsPage searchResultsPage = new SearchResultsPage(driver);

        homePage.searchFor("laptop");

        int count = searchResultsPage.getResultsCount();
        Assert.assertTrue(count > 0, "No search results found. Count=" + count);

        searchResultsPage.openFirstResult();

        ProductDetailsPage productDetailsPage = new ProductDetailsPage(driver);

        Assert.assertTrue(productDetailsPage.isAddToCartVisible(), "PDP not opened");

        productDetailsPage.addToCart();

        CartPage cartPage = productDetailsPage.openCartFromSuccessBar();
        Assert.assertTrue(cartPage.isLoaded(), "Cart page not loaded");
        Assert.assertTrue(cartPage.getItemCount() > 0, "Cart is empty after adding product");


    }

}
