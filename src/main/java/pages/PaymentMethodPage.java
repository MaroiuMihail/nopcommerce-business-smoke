package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class PaymentMethodPage extends BasePage {

    private final By continueButton = By.cssSelector("button.payment-method-next-step-button");

    public PaymentMethodPage(WebDriver driver) {
        super(driver);
    }

    public PaymentInfoPage continueNext() {
        click(continueButton);
        return new PaymentInfoPage(driver);
    }
}
