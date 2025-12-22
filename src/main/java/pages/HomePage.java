package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage {

    protected WebDriver driver;

    private By searchInput = By.id("small-searchterms");
    private By searchButton = By.cssSelector("button.search-box-button");


    public HomePage(WebDriver driver){
        this.driver = driver;
    }

    public void searchFor(String term){

        driver.findElement(searchInput).clear();
        driver.findElement(searchInput).sendKeys(term);
        driver.findElement(searchButton).click();
    }
}
