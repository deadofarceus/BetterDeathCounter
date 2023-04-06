package betterdeathcounter.service.predict;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import betterdeathcounter.Main;
import betterdeathcounter.service.TimeService;

public class GetPercenttest {

    private static final String LOADING = "loading"; 
    private static final String FIGHTING = "fighting";
    private static final String NOTHING = "nothing"; 
    private final ScheduledExecutorService saveScheduler = Executors.newSingleThreadScheduledExecutor();
    private String STATE = NOTHING;
    private int currentPercentage = Integer.MAX_VALUE;

    public static void main(String[] args) {
        GetPercenttest gp = new GetPercenttest();
        gp.percentTest();
    }

    public void percentTest() {
        List<BufferedImage> bosslist = new ArrayList<>();
        List<BufferedImage> loadlist = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            try {
                BufferedImage image = null;
                File file = new File(Main.class.getResource("images/load"+i+".jpg").toURI());
                image = ImageIO.read(file);
                loadlist.add(image);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }

        for (int i = 1; i < 5; i++) {
            try {
                BufferedImage image = null;
                File file = new File(Main.class.getResource("images/boss"+i+".jpg").toURI());
                image = ImageIO.read(file);
                bosslist.add(image);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }

        for (BufferedImage bufferedImage : loadlist) {
            int x = 1412; // the x coordinate of the pixel to observe
            int y = 1027; // the y coordinate of the pixel to observe
            int color = bufferedImage.getRGB(x, y);
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;
            System.out.println("Pixel color at (" + x + "," + y + "): RGB(" + red + "," + green + "," + blue + ")");
            System.out.println(getLoading(bufferedImage));
        }

        System.out.println();

        saveScheduler.scheduleAtFixedRate(() -> {
            Robot robot;
            try {
                robot = new Robot();
                BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                // TimeService.print("Screenshot taken");
                changeState(screenShot);
                executeState(screenShot);
                // File outputFile = new File("graphs/Kutcher/testscreen.png");

                // try {
                //     ImageIO.write(screenShot, "png", outputFile);
                // } catch (IOException e) { e.printStackTrace(); }
            } catch (AWTException e) { e.printStackTrace(); }
        }, 1, 1, TimeUnit.SECONDS);

    }

    private void executeState(BufferedImage screenShot) {
        switch (this.STATE) {
            case LOADING:
                currentPercentage = Integer.MAX_VALUE;
                this.STATE = NOTHING;
                TimeService.print("Changed State to " + NOTHING);
                break;
            case FIGHTING:
                int screenPercentage = getCurrentPercentage(screenShot);
                if (currentPercentage == Integer.MAX_VALUE || 
                    (screenPercentage < currentPercentage && Math.abs(screenPercentage - currentPercentage) < 10)) {
                    currentPercentage = screenPercentage;
                    TimeService.print("New Percentage detected: " + currentPercentage + "%");
                }
                break;
            default:
                break;
        }
    }

    private void changeState(BufferedImage screenShot) {
        switch (this.STATE) {
            case FIGHTING:
                if (getLoading(screenShot) > 850) {
                    this.STATE = LOADING;
                    TimeService.print("Changed State to " + LOADING);
                }
                break;
            default:
                if (getCurrentPercentage(screenShot) > 90) {
                    this.STATE = FIGHTING;
                    TimeService.print("Changed State to " + FIGHTING);
                }
                break;
        }
    }

    private int getCurrentPercentage(BufferedImage bufferedImage) {
        int percentage = 0;
        for (int i = 465; i < 1460; i++) {
            if (isPixelRed(bufferedImage, i, 870)) {
                percentage++;
            }
        }

        return (int) Math.round(percentage*20.0/199 + 0.5);
    }

    private boolean isPixelRed(BufferedImage bufferedImage, int i, int j) {
        int x = i;
        int y = j;
        int color = bufferedImage.getRGB(x, y);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return red+20 > green + blue && red > 25 && green < 80 && blue < 60;
    }

    private int getLoading(BufferedImage bufferedImage) {
        int loading = 0;
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 1920; i++) {
                if (isPixelBlack(bufferedImage, i, j)) {
                    loading += 1;
                }
            }
        }

        return loading;
    }

    private boolean isPixelBlack(BufferedImage bufferedImage, int i, int j) {
        int x = i;
        int y = j;
        int color = bufferedImage.getRGB(x, y);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return red < 3 && green < 3 && blue < 3;
    }
}
