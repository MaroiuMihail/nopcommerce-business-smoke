package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ConfirmOrderPage extends BasePage {

    private final By confirmOrderButton = By.cssSelector("button.confirm-order-next-step-button");

    public ConfirmOrderPage(WebDriver driver) {
        super(driver);
    }

    public boolean isConfirmButtonVisible() {
        return isDisplayed(confirmOrderButton);
    }
}
