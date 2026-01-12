package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SearchResultsPage extends BasePage {

    private static final By SEARCH_RESULTS = By.cssSelector(".product-title a");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public int getResultsCount() {
        waitVisible(SEARCH_RESULTS);
        List<WebElement> results = driver.findElements(SEARCH_RESULTS);
        return results.size();
    }

    public void openFirstResult() {
        waitVisible(SEARCH_RESULTS);
        List<WebElement> results = driver.findElements(SEARCH_RESULTS);

        if (results.isEmpty()) {
            throw new IllegalStateException("No search results found to open");
        }

        results.get(0).click();
    }
}
