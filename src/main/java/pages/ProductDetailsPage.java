package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;


public class ProductDetailsPage extends BasePage {

    private final By addToCartButton = By.cssSelector("button[id^='add-to-cart-button']");
    private final By successBar = By.cssSelector("div.bar-notification.success");
    private final By successBarCartLink = By.cssSelector("div.bar-notification.success a[href*='cart']");

    public ProductDetailsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAddToCartVisible() {
        return isDisplayed(addToCartButton);
    }

    public void addToCart() {
        click(addToCartButton);
    }

    public CartPage openCartFromSuccessBar() {
        waitVisible(successBar);

        wait.until(ExpectedConditions.textToBePresentInElementLocated(successBar, "added to your shopping cart"));


        click(successBarCartLink);

        return new CartPage(driver);
    }

}
