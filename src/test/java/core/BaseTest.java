package core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;


public class BaseTest {

    protected WebDriver driver;
    protected static final String BASE_URL = "https://demo.nopcommerce.com/";

    @BeforeMethod
    public void setupBrowser(){
        driver = new ChromeDriver();
        driver.get(BASE_URL);
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void teardownBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}

