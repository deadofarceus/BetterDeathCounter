package betterdeathcounter.subscenes;

import java.awt.Toolkit;
import java.util.List;

import betterdeathcounter.App;
import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Death;
import betterdeathcounter.model.Player;
import betterdeathcounter.service.CalculateService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BossStatsScene {

    final App app;
    private final CalculateService calculateService = new CalculateService();
    private final double timePerPercentage;
    private final int totalTime;
    private final Player player;

    public BossStatsScene(App app, Player player) {
        this.app = app;
        this.player = player;
        List<Death> deaths = player.getCurrentBoss().getDeaths();
        this.timePerPercentage = getTimePerPercentage(deaths);
        this.totalTime = getTotalSeconds(deaths);
    }
    
    public void showBossStats() {
        app.getStage().getScene().getRoot().setDisable(true);
        app.getStage().getScene().setOnMouseClicked(event -> {
            Toolkit.getDefaultToolkit().beep();
        });

        Boss boss = player.getCurrentBoss();
        List<Death> deaths = boss.getDeaths();
        double[] regressionInfos = calculateService.getRegressionInfos(player);
        double[] pred = boss.getPrediction();

        int lastTry;
        String tillDefeated;
        if (player.getSettings().getUseCostumPrediction()) {
            lastTry = deaths.size() + pred.length;
            tillDefeated = timeTillDefeatedPred(boss.getDeaths(), pred);
        } else {
            lastTry = (int) regressionInfos[5];
            tillDefeated = timeTillDefeated(boss.getDeaths(), regressionInfos);
        }

        List<String> infoLabels = List.of(
            "Number of deaths: \n" + deaths.size(),
            "Expected last try: \n" + lastTry,
            "Total spend time: \n" + totalTime(),
            "Time per percentage: \n" + timePerPercentage(boss.getDeaths()),
            "Time from 100 to 0: \n" + timefrom100to0(boss.getDeaths()),
            "Time till defeated: \n" + tillDefeated
            );
    
        Stage stage = new Stage();
        stage.setTitle("Stats for " + boss.getName());
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
    
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
    
        Label nameLabel = new Label(boss.getName());
        vbox.getChildren().add(nameLabel);
        nameLabel.getStyleClass().add("name-label");
        VBox.setMargin(nameLabel, new Insets(0, 0, 20, 0));
    
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < infoLabels.size(); i++) {
            int row = i / 2;
            int col = i % 2;
            grid.add(getInfoLabel(infoLabels.get(i)), col, row+1);
        }
    
        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> stage.close());
    
        vbox.getChildren().add(grid);
    
        vbox.getChildren().add(closeButton);
    
        Scene scene = new Scene(vbox, 600, 450);
    
        scene.getStylesheets().add(Main.class.getResource("style/AboutStyle.css").toString());
    
        stage.setScene(scene);
        stage.showAndWait();

        app.getStage().getScene().getRoot().setDisable(false);
        app.getStage().getScene().setOnMouseClicked(event ->{});
    }

    private Label getInfoLabel(String info) {
        Label label = new Label(info);
        label.getStyleClass().add("info-label");
        label.setMinWidth(200);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private int getTotalSeconds(List<Death> deaths) {
        int totalSeconds = 0;
        
        for (Death death : deaths) {
            int percentage = 100 - death.getPercentage();
            if (death.getTime() != 0) {
                totalSeconds += death.getTime();
            } else {
                Death nearest = null;
                for (Death d : deaths) {
                    double diff = Math.abs(d.getPercentage() - percentage);
                    if(diff < 5 && (nearest == null || diff < Math.abs(nearest.getPercentage()-percentage))
                        && d.getTime() != 0) {
                        nearest = d;
                    }
                }
                if (nearest != null) {
                    totalSeconds += nearest.getTime();
                } else {
                    totalSeconds += timePerPercentage*percentage;
                }
            }
        }
        return totalSeconds;
    }

    private double getTimePerPercentage(List<Death> deaths) {
        double timePerPercentage = 0.0;
    
        int numUsedDeaths = 0;
        for (Death death : deaths) {
            int percentage = 100 - death.getPercentage();
            if (death.getTime() != 0) {
                timePerPercentage += (double) death.getTime() / percentage;
                numUsedDeaths++;
            } else {
                Death nearest = null;
                for (Death d : deaths) {
                    double diff = Math.abs(d.getPercentage() - percentage);
                    if(diff < 5 && (nearest == null || diff < Math.abs(nearest.getPercentage()-percentage))
                        && d.getTime() != 0) {
                        nearest = d;
                    }
                }
                if (nearest != null) {
                    timePerPercentage += (double) nearest.getTime() / percentage;
                    numUsedDeaths++;
                }
            }
        }
    
        return timePerPercentage / numUsedDeaths;
    }
    
    private String timeTillDefeated(List<Death> deaths, double[] regressionInfos) {
        int totalSeconds = totalTime;

        final double expSlope = regressionInfos[3];
        final double expY = regressionInfos[4];
    
        for (int i = deaths.size(); i < regressionInfos[5]; i++) {
            totalSeconds += (int) ((100-(expY - Math.exp(expSlope * i))) * timePerPercentage);
        }
    
        return formatSeconds(totalSeconds, true);
    }

    private String timeTillDefeatedPred(List<Death> deaths, double[] pred) {
        int totalSeconds = totalTime;

        for (double d : pred) {
            totalSeconds += (100 - d)*timePerPercentage;
        }
        
        return formatSeconds(totalSeconds, true);
    }
    
    private String timefrom100to0(List<Death> deaths) {
        return formatSeconds((int) (timePerPercentage * 100), true);
    }
    
    private String timePerPercentage(List<Death> deaths) {
        double timePerPercentage = getTimePerPercentage(deaths);
    
        int tpp = (int) (timePerPercentage * 1000);
        timePerPercentage = tpp / 1000.0;
    
        return timePerPercentage + " seconds";
    }
    
    private String totalTime() {
        return formatSeconds(totalTime, true);
    }
    
    private String formatSeconds(int totalSeconds, boolean displayHours) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
    
        if (displayHours) {
            return String.format("%03d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
