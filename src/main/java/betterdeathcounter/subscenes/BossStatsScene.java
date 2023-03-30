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

    public BossStatsScene(App app) {
        this.app = app;
    }
    
    public void showBossStats(Player player) {
        app.getStage().getScene().getRoot().setDisable(true);
        app.getStage().getScene().setOnMouseClicked(event -> {
            Toolkit.getDefaultToolkit().beep();
        });

        Boss boss = player.getCurrentBoss();
        double[] regressionInfos = calculateService.getRegressionInfos(player);
    
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
    
        Label deathLabel = new Label("Number of deaths: \n" + boss.getDeaths().size());
        grid.add(deathLabel, 0, 1);
        deathLabel.getStyleClass().add("info-label");
    
        Label lastTryLabel = new Label("Expected last try: \n" + regressionInfos[5]);
        grid.add(lastTryLabel, 1, 1);
        lastTryLabel.getStyleClass().add("info-label");
    
        Label totalTimeLabel = new Label("Total spend time: \n" + totalTime(boss.getDeaths()));
        grid.add(totalTimeLabel, 0, 2);
        totalTimeLabel.getStyleClass().add("info-label");
    
        Label timePerPercLabel = new Label("Time per percentage: \n" + timePerPercentage(boss.getDeaths()));
        grid.add(timePerPercLabel, 1, 2);
        timePerPercLabel.getStyleClass().add("info-label");
    
        Label time100to0Label = new Label("Time from 100 to 0: \n" + timefrom100to0(boss.getDeaths()));
        grid.add(time100to0Label, 0, 3);
        time100to0Label.getStyleClass().add("info-label");
    
        Label timeTillDefeatLabel = new Label("Time till defeated: \n" + timeTillDefeated(boss.getDeaths(), regressionInfos));
        grid.add(timeTillDefeatLabel, 1, 3);
        timeTillDefeatLabel.getStyleClass().add("info-label");
    
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

    private String timeTillDefeated(List<Death> deaths, double[] regressionInfos) {
        double timePerPercentage = 0.0;

        for (Death death : deaths) {
            timePerPercentage += death.getTime()/death.getPercentage();
        }

        timePerPercentage /= deaths.size();

        int totalSeconds = 0;
        final double expSlope = regressionInfos[3];
        final double expY = regressionInfos[4];

        for (int i = deaths.size(); i < regressionInfos[5]; i++) {
            totalSeconds += (int)((expY - Math.exp(expSlope*i)) * timePerPercentage);
        }

        for (Death death : deaths) {
            totalSeconds += death.getTime();
        }

        return formatSeconds(totalSeconds, true);
    }

    private String timefrom100to0(List<Death> deaths) {
        double timePerPercentage = 0.0;

        for (Death death : deaths) {
            timePerPercentage += death.getTime()/death.getPercentage();
        }

        timePerPercentage /= deaths.size();

        return formatSeconds((int)timePerPercentage*100, true);
    }

    private String timePerPercentage(List<Death> deaths) {

        double timePerPercentage = 0.0;

        for (Death death : deaths) {
            timePerPercentage += death.getTime()/death.getPercentage();
        }

        timePerPercentage /= deaths.size();

        return formatSeconds((int) timePerPercentage, false);
    }

    private String totalTime(List<Death> deaths) {
        int totalSeconds = 0;

        for (Death death : deaths) {
            totalSeconds += death.getTime();
        }

        return formatSeconds(totalSeconds, true);
    }

    private String formatSeconds(int totalSeconds, boolean displayHours) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if(displayHours) {
            return String.format("%03d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
