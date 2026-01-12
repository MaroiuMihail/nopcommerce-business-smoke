package tests.smoke;

import core.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;

public class Smoke_GuestCheckoutToConfirmTest extends BaseTest {

    @Test
    public void guestCheckout_shouldReachConfirmOrder() {

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

        CheckoutPage checkout = cart.clickCheckout();
        BillingAddressPage billing = checkout.
                checkoutAsGuest();

        String email = "qa" + System.currentTimeMillis() + "@test.com";

        billing.fillBillingAddress(
                "Test", "User", email,
                "Romania",
                "București Sector 2",
                "Bucharest", "Street 1", "010101", "0712345678"
        );

        ShippingMethodPage shipping = billing.continueNext();
        PaymentMethodPage payment = shipping.continueNext();
        PaymentInfoPage paymentInfo = payment.continueNext();

        paymentInfo.fillCreditCard(
                "Visa",
                "Test User",
                "4111111111111111",
                "1",
                "2026",
                "123"
        );

        ConfirmOrderPage confirm = paymentInfo.continueNext();

        Assert.assertTrue(confirm.isConfirmButtonVisible(),
                "Did not reach Confirm Order step");

        Assert.assertTrue(confirm.isConfirmButtonVisible(),
                "Did not reach Confirm Order step");
    }
}
