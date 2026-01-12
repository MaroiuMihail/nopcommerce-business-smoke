package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
    super(driver);
    }

    private final By searchInput = By.id("small-searchterms");
    private final By searchButton = By.cssSelector("button.search-box-button");


    public void searchFor(String term) {
        if (term == null || term.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term must not be null/blank");
        }

        type(searchInput, term.trim());
        click(searchButton);
    }


    public boolean isSearchBoxVisible() {
            return isDisplayed(searchInput);
        }




    }
