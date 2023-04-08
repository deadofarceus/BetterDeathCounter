package betterdeathcounter.service;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

public class AutomationService {
    private static final String LOADING = "Loading"; 
    private static final String FIGHTING = "Fighting";
    private static final String NOTHING = "Nothing";
    private String STATE = NOTHING;
    private int currentPercentage = Integer.MAX_VALUE;

    public int checkState() {
        Robot robot;
        try {
            robot = new Robot();
            BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            changeState(screenShot);
            return executeState(screenShot);
        } catch (AWTException e) { e.printStackTrace(); }
        return -4;
    }

    private int executeState(BufferedImage screenShot) {
        switch (this.STATE) {
            case LOADING:
                this.STATE = NOTHING;
                TimeService.print("Changed State to " + NOTHING);
                return currentPercentage;
            case FIGHTING:
                int screenPercentage = getCurrentPercentage(screenShot);
                if (currentPercentage == Integer.MAX_VALUE || 
                    (screenPercentage < currentPercentage && Math.abs(screenPercentage - currentPercentage) < 10)) {
                    currentPercentage = screenPercentage;
                    TimeService.print("New Percentage detected: " + currentPercentage + "%");
                    return currentPercentage+1000;
                }
                return -1;
            default:
                return -3;
        }
    }

    private void changeState(BufferedImage screenShot) {
        switch (this.STATE) {
            case FIGHTING:
                if (getLoading(screenShot)) {
                    this.STATE = LOADING;
                    TimeService.print("Changed State to " + LOADING);
                }
                break;
            default:
                if (getCurrentPercentage(screenShot) > 5) {
                    currentPercentage = Integer.MAX_VALUE;
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
        if (percentage > 60) {
            percentage += 15;
        }
        
        return (int) Math.round(percentage*20.0/199 + 0.5);
    }

    private boolean isPixelRed(BufferedImage bufferedImage, int x, int y) {
        int color = bufferedImage.getRGB(x, y);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return 30 > green + blue && red > 60 && red < 100;
    }

    private boolean getLoading(BufferedImage bufferedImage) {
        int loading = 0;
        for (int j = 0; j < 50; j++) {
            for (int i = 0; i < 1920; i++) {
                if (isPixelBlack(bufferedImage, i, j)) {
                    loading += 1;
                }
            }
        }

        return loading > 15000;
    }

    private boolean isPixelBlack(BufferedImage bufferedImage, int x, int y) {
        int color = bufferedImage.getRGB(x, y);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return red < 1 && green < 1 && blue < 1;
    }
}
