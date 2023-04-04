package betterdeathcounter.controller;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Player;
import betterdeathcounter.model.Settings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class IngameScreenController implements Controller {

    private final Player player;
    private final Settings settings;
    private PropertyChangeListener bossListener, deathListener, timerListener;
    private Controller gameController, bossController, deathController, progressChartController;

    public IngameScreenController(Player player) {
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
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/IngameScreen.fxml"));

        final HBox actionBox = (HBox) parent.lookup("#actionBox");

        gameController = new GameController(player);
        actionBox.getChildren().add(gameController.render());

        if (settings.getUseCostumPrediction()) {
            bossController = new BossCostumPredController(player, player.getCurrentBoss());
        } else {
            bossController = new BossController(player, player.getCurrentBoss());
        }
        bossController.init();
        actionBox.getChildren().add(bossController.render());

        deathController = new DeathController(player.getCurrentBoss(), player);
        actionBox.getChildren().add(deathController.render());


        /*
         * Progress Graph
         */
        final ScrollPane graphScroll = (ScrollPane) parent.lookup("#graphScroll");
        final AnchorPane ap = (AnchorPane) graphScroll.getContent();

        progressChartController = new ProgressChartController(player);
        progressChartController.init();
        ap.getChildren().add(progressChartController.render());

        deathListener = e -> {
            progressChartController.destroy();
            progressChartController = new ProgressChartController(player);
            progressChartController.init();
            try {
                ap.getChildren().set(0, progressChartController.render());
            } catch (IOException ignored) {}
        };

        bossListener = e -> {
            if(e.getPropertyName().equals(Player.PROPERTY_CURRENT_BOSS)) {
                ((Boss) e.getOldValue()).listeners().removePropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
                ((Boss) e.getOldValue()).listeners().removePropertyChangeListener(Boss.PROPERTY_PREDICTION, deathListener);
                
                deathController.destroy();
                deathController = new DeathController(player.getCurrentBoss(), player);
                
                player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
                player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_PREDICTION, deathListener);
            } else if (!e.getPropertyName().equals(Settings.PROPERTY_USE_COSTUM_PREDICTION)) {
                deathController.destroy();
                deathController = new DeathController(player.getCurrentBoss(), player);
            }

            bossController.destroy();
            if (settings.getUseCostumPrediction()) {
                bossController = new BossCostumPredController(player, player.getCurrentBoss());
            } else {
                bossController = new BossController(player, player.getCurrentBoss());
            }
            bossController.init();

            progressChartController.destroy();
            progressChartController = new ProgressChartController(player);
            progressChartController.init();

            try {
                actionBox.getChildren().set(1, bossController.render());
                if (!e.getPropertyName().equals(Settings.PROPERTY_USE_COSTUM_PREDICTION)) {
                    actionBox.getChildren().set(2, deathController.render());
                }
                ap.getChildren().set(0, progressChartController.render());
            } catch (IOException ignored) {}
        };


        timerListener = e -> {
            deathController.destroy();
            deathController = new DeathController(player.getCurrentBoss(), player);
            try {
                actionBox.getChildren().set(2, deathController.render());
            } catch (IOException ignored) {}
        };

        player.listeners().addPropertyChangeListener(Player.PROPERTY_CURRENT_BOSS, bossListener);
        player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
        player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_PREDICTION, deathListener);
        settings.listeners().addPropertyChangeListener(Settings.PROPERTY_SHOW_TIMER, timerListener);
        settings.listeners().addPropertyChangeListener(Settings.PROPERTY_GARBAGE_FACTOR, deathListener);
        settings.listeners().addPropertyChangeListener(Settings.PROPERTY_SHOW_EXP, deathListener);
        settings.listeners().addPropertyChangeListener(Settings.PROPERTY_SHOW_LINEAR, deathListener);
        settings.listeners().addPropertyChangeListener(Settings.PROPERTY_USE_COSTUM_PREDICTION, bossListener);

        return parent;
    }

    @Override
    public void destroy() {
        gameController.destroy();
        bossController.destroy();
        deathController.destroy();
        progressChartController.destroy();

        player.listeners().removePropertyChangeListener(Player.PROPERTY_CURRENT_BOSS, bossListener);
        player.getCurrentBoss().listeners().removePropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
        player.getCurrentBoss().listeners().removePropertyChangeListener(Boss.PROPERTY_PREDICTION, deathListener);
        settings.listeners().removePropertyChangeListener(Settings.PROPERTY_SHOW_TIMER, timerListener);
        settings.listeners().removePropertyChangeListener(Settings.PROPERTY_GARBAGE_FACTOR, deathListener);
        settings.listeners().removePropertyChangeListener(Settings.PROPERTY_SHOW_EXP, deathListener);
        settings.listeners().removePropertyChangeListener(Settings.PROPERTY_SHOW_LINEAR, deathListener);
        settings.listeners().removePropertyChangeListener(Settings.PROPERTY_USE_COSTUM_PREDICTION, bossListener);
    }
    
}
