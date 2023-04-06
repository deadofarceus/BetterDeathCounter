package betterdeathcounter.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.jfoenix.controls.JFXSlider;

import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Death;
import betterdeathcounter.model.Player;
import betterdeathcounter.model.Settings;
import betterdeathcounter.service.APIService;
import betterdeathcounter.service.AutomationService;
import betterdeathcounter.service.CalculateService;
import betterdeathcounter.service.TimeService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class DeathController implements Controller {

    private final Player player;
    private final Settings settings;
    private final Boss boss;
    private final AutomationService automationService = new AutomationService();
    private final CalculateService calculateService = new CalculateService();
    private final APIService apiService = new APIService();
    private long startTime = System.currentTimeMillis();
    private long elapsedTime = System.currentTimeMillis() - startTime;
    private long elapsedSeconds = elapsedTime / 1000;
    private long secondsDisplay = elapsedSeconds % 60;
    private long elapsedMinutes = elapsedSeconds / 60;
    private Thread timerThread;
    private boolean shutdown = false;
    private final ScheduledExecutorService saveScheduler = Executors.newSingleThreadScheduledExecutor();

    public DeathController(Boss boss, Player player) {
        this.boss = boss;
        this.player = player;
        this.settings = player.getSettings();
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
        final HBox timerBox = (HBox) parent.lookup("#timerBox");
        final Text autoPercentage = new Text();

        if (settings.getAutomatic()) {
            autoPercentage.setFont(new Font(20));
            autoPercentage.setText("Detected Percentage:\n100%");
            autoPercentage.setTextAlignment(TextAlignment.CENTER);
            timerBox.getChildren().add(autoPercentage);
        }
        /*
         * Timer display
         */
        final Text timerText = (Text) parent.lookup("#timerText");
        final Text totalTime = (Text) parent.lookup("#totalTime");

        if(settings.getShowTimer() && !boss.getName().equals("Other Monsters or Heights")) { 
            timerText.setText(String.format("%02d:%02d", elapsedMinutes, secondsDisplay));
            timerThread = new Thread(() -> {
                while (!shutdown) {
                    elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                    long minutes = (elapsedTime % 3600) / 60;
                    long seconds = elapsedTime % 60;
                    Platform.runLater(() -> timerText.setText(String.format("%02d:%02d", minutes, seconds)));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            timerThread.start();

            totalTime.setText(totalTime(player.getCurrentBoss().getDeaths()));
        }

        /*
         * Button actions
         */
        final Button newDeath = (Button) parent.lookup("#newDeath");
        final Button secondPhase = (Button) parent.lookup("#secondPhase");
        final JFXSlider percentageSlider = (JFXSlider) parent.lookup("#percentageSlider");

        if (settings.getAutomatic()) {
            saveScheduler.scheduleAtFixedRate(() -> {
                int result = automationService.checkState();
                if (result > -1 && result < 1000) {
                    Platform.runLater(() -> handleNewDeath(percentageSlider, totalTime, result));
                } else if (result < -1) {
                    startTime = System.currentTimeMillis();
                } else if (result > 1000) {
                    Platform.runLater(() -> autoPercentage.setText("Detected Percentage:\n" + (result-1000) + "%"));
                }
            }, 1, 1, TimeUnit.SECONDS);
        }

        secondPhase.setVisible(false);
        if (boss.getSecondPhase()) {
            percentageSlider.setMax(200);
        }

        newDeath.setOnAction(e -> handleNewDeath(percentageSlider, totalTime, -1));
    
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
            secondPhase.setVisible(newValue.intValue() == 0 && !boss.getSecondPhase());
        });

        return parent;
    }

    private void handleNewDeath(JFXSlider percentageSlider, Text totalTime, int result) {
    
            // try {
            //     BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/NiKi/Desktop/Coding/deaths.txt"));
            //     StringBuilder sb = new StringBuilder();
            //     for (Death i : boss.getDeaths()) {
            //         sb.append(i.getPercentage()).append(", ");
            //     }
            //     sb.setLength(sb.length() - 2);  // remove trailing comma and space
            //     writer.write(sb.toString());
            //     writer.close();
            // } catch (IOException e1) {
            //     e1.printStackTrace();
            // }

            
            int percentage = percentageSlider.valueProperty().intValue();
            if (result > 0) {
                percentage = result;
            }
            Death d = new Death().setPercentage(percentage);

            if(elapsedTime > 30) d.setTime((int)elapsedTime);
            else {
                Death nearest = null;
                for (Death death : player.getCurrentBoss().getDeaths()) {
                    double diff = Math.abs(death.getPercentage() - percentage);
                    if(diff < 5 && (nearest == null || diff < Math.abs(nearest.getPercentage()-percentage))) {
                        nearest = death;
                    }
                }

                if (nearest != null) {
                    d.setTime(nearest.getTime());
                }
            }

            if(settings.getShowTimer()) {
                startTime = System.currentTimeMillis();
            }

            if(boss.getName().equals("Other Monsters or Heights")) {
                boss.withDeaths(new Death());
            } else {
                if(!calculateService.bossDead(boss)) {
                    boss.withDeaths(d);

                    new Thread(() -> {
                        try {
                            if (player.getCurrentGame().getSpreadsheetId() != null && settings.getAPIUsername() != null) {
                                apiService.sendData(player.getCurrentGame(), boss);
                            } else {
                                TimeService.print("No connection to google service!");
                                TimeService.print("Please enter API Username for the player and ");
                                TimeService.print("the spreadsheetId for the game under the Connect menu!");
                                System.out.println();
                            }
                        } catch (GeneralSecurityException | IOException e1) {
                            e1.printStackTrace();
                        }
                    }).start();
                    
                    TimeService.print("New Death: " + d.getPercentage());
                }
            }
            
            totalTime.setText(totalTime(player.getCurrentBoss().getDeaths()));

    }

    private String totalTime(List<Death> deaths) {
        int totalSeconds = 0;

        for (Death death : deaths) {
            totalSeconds += death.getTime();
        }

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String time = String.format("%03d:%02d:%02d", hours, minutes, seconds);
        return "Total Time:" + "\n" + time;
    }

    @Override
    public void destroy() {
        saveScheduler.shutdown();
        shutdown = true;
    }
    
}
