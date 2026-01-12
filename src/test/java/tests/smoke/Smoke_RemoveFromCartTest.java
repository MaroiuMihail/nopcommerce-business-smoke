package tests.smoke;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.HomePage;
import pages.ProductDetailsPage;
import pages.SearchResultsPage;

public class Smoke_RemoveFromCartTest extends BaseTest {

    @Test
    public void cart_removeItem_shouldBecomeEmpty() {

        HomePage home = new HomePage(driver);
        SearchResultsPage results = new SearchResultsPage(driver);

        home.searchFor("laptop");
        Assert.assertTrue(results.getResultsCount() > 0, "No search results found");

        results.openFirstResult();

        ProductDetailsPage pdp = new ProductDetailsPage(driver);
        Assert.assertTrue(pdp.isAddToCartVisible(), "PDP not opened");

        pdp.addToCart();
        CartPage cart = pdp.openCartFromSuccessBar();

        Assert.assertTrue(cart.getItemCount() > 0, "Cart is empty after adding product");

        cart.removeAllItems();

        Assert.assertTrue(cart.isEmpty(), "Cart should be empty after removing all items");
    }
}
