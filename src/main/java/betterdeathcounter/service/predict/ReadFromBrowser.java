package betterdeathcounter.service.predict;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.apache.http.annotation.Experimental;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v111.page.Page;

@Experimental
public class ReadFromBrowser {
    public static void main(String[] args) throws IOException, AWTException, InterruptedException {
        String username = "kutcherlol";
        System.setProperty("webdriver.chrome.driver", "bin/main/betterdeathcounter/web/chromedriver.exe"); // replace with the path to the chromedriver executable on your system
        ChromeOptions options = new ChromeOptions();
        options.addArguments("test-type");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        WebDriver driver = new ChromeDriver(options);
        
        DevTools devTools = ((ChromeDriver) driver).getDevTools();
        devTools.createSession();
        devTools.send(Page.enable());
        driver.get("https://twitch.tv/"+ username);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.findElement(By.tagName("body")).sendKeys("f");
        Thread.sleep(5000);

        for (int i = 0; i < 5; i++) {
            Thread.sleep(2000);
            byte[] scrBytes = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage screenshot = ImageIO.read(new ByteArrayInputStream(scrBytes));
            Color pixelColor = new Color(screenshot.getRGB(100, 100)); // Replace with the coordinates of the pixel you want to observe
            System.out.println("Pixel color: " + pixelColor);
        }
        driver.quit();
    }
}
