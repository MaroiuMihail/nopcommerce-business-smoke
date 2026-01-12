package core;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;


public class BaseTest {

    protected WebDriver driver;
    protected static final String BASE_URL = "http://localhost:5000";

    @BeforeMethod
    public void setupBrowser(){
        driver = DriverFactory.createDriver();
        driver.get(BASE_URL);
    }

    @AfterMethod
   public void teardownBrowser() {
       if (driver != null) {
           driver.quit();
     }
    }
}

