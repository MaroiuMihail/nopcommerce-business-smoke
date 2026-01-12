package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckoutPage extends BasePage {

    private final By checkoutAsGuestButton = By.cssSelector("button.checkout-as-guest-button");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public BillingAddressPage checkoutAsGuest() {
        click(checkoutAsGuestButton);
        return new BillingAddressPage(driver);
    }
}


