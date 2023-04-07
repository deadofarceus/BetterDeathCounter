package betterdeathcounter.service.predict;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import betterdeathcounter.Main;

public class ReadFromBrowser {
    public static void main(String[] args) {
        String username = "kutcherlol";
        System.setProperty("webdriver.chrome.driver", "bin/main/betterdeathcounter/web/chromedriver.exe"); // replace with the path to the chromedriver executable on your system
        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://www.twitch.tv/"+username); // navigate to the Twitch website
            WebElement fullScreenButton = driver.findElement(By.xpath("//button[@data-a-target='player-fullscreen-button']"));
            fullScreenButton.click(); // make the video player full screen
            Thread.sleep(3000); // wait for the video player to go full screen
            URL imageUrl = new URL(driver.getCurrentUrl()); // retrieve the current URL of the video player as an image
            BufferedImage image = ImageIO.read(imageUrl);
            int pixel = image.getRGB(100, 100); // replace with the coordinates of the pixel you want to read
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;
            System.out.println("Pixel value at (100, 100) on the Twitch screen is: ");
            System.out.println("Red: " + red);
            System.out.println("Green: " + green);
            System.out.println("Blue: " + blue);
            
            File outputFile = new File("graphs/Kutcher/TESTScreenvonTwitch.png");

            try {
                ImageIO.write(image, "png", outputFile);
            } catch (IOException e) { e.printStackTrace(); }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            driver.quit(); // close the web browser
        }
    }
}
