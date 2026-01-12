package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class PaymentInfoPage extends BasePage {

    private final By creditCardSelect = By.id("CreditCardType");
    private final By cardholderName = By.id("CardholderName");
    private final By cardNumber = By.id("CardNumber");
    private final By expireMonth = By.id("ExpireMonth");
    private final By expireYear = By.id("ExpireYear");
    private final By cardCode = By.id("CardCode");

    private final By continueButton = By.cssSelector("button.payment-info-next-step-button");

    public PaymentInfoPage(WebDriver driver) {
        super(driver);
    }

    public void fillCreditCard(String typeVisibleText,
                               String holder,
                               String number,
                               String monthValue,
                               String yearValue,
                               String code) {

        new Select(waitVisible(creditCardSelect)).selectByVisibleText(typeVisibleText);

        type(cardholderName, holder);
        type(cardNumber, number);

        new Select(waitVisible(expireMonth)).selectByValue(monthValue);
        new Select(waitVisible(expireYear)).selectByValue(yearValue);

        type(cardCode, code);
    }

    public ConfirmOrderPage continueNext() {
        click(continueButton);
        return new ConfirmOrderPage(driver);
    }
}
