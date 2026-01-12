package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class BillingAddressPage extends BasePage {

    private final By firstName = By.id("BillingNewAddress_FirstName");
    private final By lastName = By.id("BillingNewAddress_LastName");
    private final By email = By.id("BillingNewAddress_Email");
    private final By country = By.id("BillingNewAddress_CountryId");
    private final By city = By.id("BillingNewAddress_City");
    private final By address1 = By.id("BillingNewAddress_Address1");
    private final By zip = By.id("BillingNewAddress_ZipPostalCode");
    private final By phone = By.id("BillingNewAddress_PhoneNumber");
    private final By state = By.id("BillingNewAddress_StateProvinceId");


    private final By continueButton = By.cssSelector("button.new-address-next-step-button");

    public BillingAddressPage(WebDriver driver) {
        super(driver);
    }

    public void fillBillingAddress(
            String fn, String ln, String mail,
            String countryVisibleText,
            String stateVisibleText,
            String cityValue, String addressValue,
            String zipValue, String phoneValue
    ) {
        type(firstName, fn);
        type(lastName, ln);
        type(email, mail);

        Select countrySelect = new Select(waitVisible(country));
        countrySelect.selectByVisibleText(countryVisibleText);

        Select stateSelect = new Select(waitVisible(state));
        if (stateSelect.getOptions().size() > 1) {
            stateSelect.selectByIndex(1);
        }

        type(city, cityValue);
        type(address1, addressValue);
        type(zip, zipValue);
        type(phone, phoneValue);
    }


    public ShippingMethodPage continueNext() {
        click(continueButton);
        return new ShippingMethodPage(driver);
    }

}
