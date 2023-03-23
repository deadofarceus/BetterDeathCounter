package betterdeathcounter.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import com.jfoenix.controls.JFXSlider;

import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Death;
import betterdeathcounter.model.Player;
import betterdeathcounter.service.APIService;
import betterdeathcounter.service.CalculateService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class DeathController implements Controller {

    private final Boss boss;
    private final CalculateService calculateService = new CalculateService();
    private final APIService apiService = new APIService();
    private final Player player;
    private long startTime = System.currentTimeMillis();
    private long elapsedTime = System.currentTimeMillis() - startTime;
    private long elapsedSeconds = elapsedTime / 1000;
    private long secondsDisplay = elapsedSeconds % 60;
    private long elapsedMinutes = elapsedSeconds / 60;
    private Runnable timerThread;
    private boolean shutdown = false;

    public DeathController(Boss boss, Player player) {
        this.boss = boss;
        this.player = player;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void init() {
    }

    @Override
    public Parent render() throws IOException {
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/Death.fxml"));
        
        /*
         * Timer display
         */
        final Text timerText = (Text) parent.lookup("#timerText");
        final Text totalTime = (Text) parent.lookup("#totalTime");

        if(player.getShowTimer()) {
            timerText.setText(elapsedMinutes + ":" + secondsDisplay);
            timerThread = new Runnable() {
                public void run() {
                    while (!shutdown) {
                        elapsedTime = System.currentTimeMillis() - startTime;
                        elapsedSeconds = elapsedTime / 1000;
                        secondsDisplay = elapsedSeconds % 60;
                        elapsedMinutes = elapsedSeconds / 60;

                        String displaySeconds = secondsDisplay+"";
                        String displayMinutes = elapsedMinutes+"";
                        if(secondsDisplay<10) displaySeconds = "0" + displaySeconds;
                        if(elapsedMinutes<10) displayMinutes = "0" + displayMinutes;

                        timerText.setText(displayMinutes + ":" + displaySeconds);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {}
                    }
                }
            };
            new Thread(timerThread).start();

            totalTime.setText(totalTime(player.getCurrentBoss().getDeaths()));
        }


        /*
         * Button actions
         */
        final Button newDeath = (Button) parent.lookup("#newDeath");
        final Button secondPhase = (Button) parent.lookup("#secondPhase");
        final JFXSlider percentageSlider = (JFXSlider) parent.lookup("#percentageSlider");
        
        secondPhase.setVisible(boss.getSecondPhase());

        newDeath.setOnAction(e -> {
            int percentage = percentageSlider.valueProperty().intValue();
            Death d = new Death().setPercentage(percentage);

            if(elapsedSeconds > 30) d.setTime((int)elapsedSeconds);
            else {
                Death nearest = null;
                for (Death death : player.getCurrentBoss().getDeaths()) {
                    if(death.getPercentage() - 5 < percentage && percentage < death.getPercentage() +5) {
                        if(nearest == null || nearest.getPercentage()-percentage > death.getPercentage()-percentage) {
                            nearest = death;
                        }
                    }
                }

                if (nearest != null) {
                    d.setTime(nearest.getTime());
                }
            }

            if(player.getShowTimer()) {
                startTime = System.currentTimeMillis();
            }

            if(boss.getName().equals("Other Monsters or Heights")) {
                boss.withDeaths(new Death());
            } else {
                if(!calculateService.bossDead(boss)) {
                    boss.withDeaths(d);

                    new Thread() {
                        public void run() {
                            try {
                                if (player.getCurrentGame().getSpreadsheetId() != null
                                    && player.getAPIUsername() != null) {
                                    apiService.sendData(player.getCurrentGame(), boss);
                                } else {
                                    System.out.println("No connection to google service!");
                                    System.out.println("Please enter API Username for the player and ");
                                    System.out.println("the spreadsheetId for the game under the Connect menu!");
                                    System.out.println();
                                }
                            } catch (GeneralSecurityException | IOException e1) { e1.printStackTrace(); }
                        }
                    }.start();
                    
                    System.out.println("New Death: " + d.getPercentage());
                }
            }

            totalTime.setText(totalTime(player.getCurrentBoss().getDeaths()));
        });
    
        secondPhase.setOnAction(e -> {
            boss.setSecondPhase(true);

            percentageSlider.setMax(200);
            for (Death death : boss.getDeaths()) {
                death.setPercentage(death.getPercentage()+100);
            }
            secondPhase.setVisible(false);
                
        });

        percentageSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            newDeath.setText("NEW DEATH: " + newValue.intValue() + "%");
            if(newValue.intValue() == 0) {
                if (!boss.getSecondPhase()) {
                    secondPhase.setVisible(true);
                }
            } else {
                secondPhase.setVisible(false);
            }
        });

        return parent;
    }

    private String totalTime(List<Death> deaths) {
        int totalSeconds = 0;

        for (Death death : deaths) {
            totalSeconds += death.getTime();
        }

        int totalSecondsDisplay = totalSeconds % 60;
        int totalElapsedMinutes = totalSeconds / 60;
        int totalElapsedHours = totalElapsedMinutes / 60;

        String displaySeconds = totalSecondsDisplay+"";
        String displayMinutes = totalElapsedMinutes % 60 +"";
        String displayHours = totalElapsedMinutes+"";
        if(totalSecondsDisplay<10) displaySeconds = "0" + displaySeconds;
        if(totalElapsedMinutes<10) displayMinutes = "0" + displayMinutes;
        if(totalElapsedHours<10) displayHours = "0" + displayHours;

        return "Total Time:" + "\n" + displayHours + ":" + displayMinutes + ":" + displaySeconds;
    }

    @Override
    public void destroy() {
        shutdown = true;
    }
    
}
