package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ShippingMethodPage extends BasePage {

    private final By continueButton = By.cssSelector("button.shipping-method-next-step-button");

    public ShippingMethodPage(WebDriver driver) {
        super(driver);
    }

    public PaymentMethodPage continueNext() {
        click(continueButton);
        return new PaymentMethodPage(driver);
    }
}
