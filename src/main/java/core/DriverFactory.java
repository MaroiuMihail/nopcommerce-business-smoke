package core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverFactory {

    public static WebDriver createDriver() {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");

        boolean headlessProp = "true".equalsIgnoreCase(System.getProperty("headless"));
        boolean ciEnv = "true".equalsIgnoreCase(System.getenv("CI"));
        if (headlessProp || ciEnv) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
        }

        return new ChromeDriver(options);
    }

}
