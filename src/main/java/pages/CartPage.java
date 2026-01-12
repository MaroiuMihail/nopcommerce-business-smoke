package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;


import java.util.List;

public class CartPage extends BasePage {

    private org.openqa.selenium.support.ui.WebDriverWait fastWait() {
        return new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(3));
    }


    private final By cartTable = By.cssSelector("table.cart");
    private final By cartRows = By.cssSelector("table.cart tbody tr");
    private final By productLinksInCart = By.cssSelector("table.cart .product a");

    private final By removeCheckboxes = By.cssSelector("input[name='removefromcart']");
    private final By updateCartButton = By.cssSelector("[name='updatecart']");
    private final By orderSummaryContent = By.cssSelector("div.order-summary-content");

    private final By checkoutButton = By.cssSelector("button#checkout");
    private final By termsOfServiceCheckbox = By.id("termsofservice");



    public CartPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(cartTable) || isEmpty();
    }

    public int getItemCount() {
        if (isEmpty()) return 0;

        if (!isDisplayed(cartTable)) {
            return 0;
        }

        return driver.findElements(cartRows).size();
    }

    public boolean isEmpty() {
        String txt = driver.findElements(orderSummaryContent).isEmpty()
                ? ""
                : driver.findElements(orderSummaryContent).get(0).getText().toLowerCase();

        return txt.contains("your shopping cart is empty");
    }

    public boolean hasProductNamed(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name must not be null/blank");
        }

        if (getItemCount() == 0) return false;

        String expected = productName.trim().toLowerCase();

        return driver.findElements(productLinksInCart).stream()
                .map(e -> e.getText() == null ? "" : e.getText().trim().toLowerCase())
                .anyMatch(name -> name.contains(expected));
    }

    private void fastClick(By locator) {
        WebElement el = driver.findElement(locator);
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        el.click();
    }


    public void removeAllItems() {
        if (isEmpty() || driver.findElements(cartRows).isEmpty()) {
            return;
        }

        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(removeCheckboxes, 0));

        List<WebElement> checks = driver.findElements(removeCheckboxes);
        for (WebElement cb : checks) {
            safeClick(cb);
        }

        safeClick(driver.findElement(updateCartButton));



        new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(3))
                .until(d -> {
                    boolean noRows = d.findElements(cartRows).isEmpty();

                    String txt = d.findElements(orderSummaryContent).isEmpty()
                            ? ""
                            : d.findElements(orderSummaryContent).get(0).getText().toLowerCase();

                    boolean emptyMessage = txt.contains("your shopping cart is empty");

                    return noRows || emptyMessage;
                });

    }

    private void safeClick(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
            fastWait().until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    public void acceptTermsOfService() {
        WebElement cb = driver.findElement(termsOfServiceCheckbox);
        if (!cb.isSelected()) {
            safeClick(cb);
        }
    }

    public CheckoutPage clickCheckout() {
        acceptTermsOfService();
        safeClick(driver.findElement(checkoutButton));
        return new CheckoutPage(driver);
    }

}
