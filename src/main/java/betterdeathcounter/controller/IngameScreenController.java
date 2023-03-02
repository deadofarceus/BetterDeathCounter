package betterdeathcounter.controller;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Player;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class IngameScreenController implements Controller {

    private final Player player;
    private PropertyChangeListener bossListener, garbageListener, deathListener, timerListener;
    private Controller gameController, bossController, deathController, progressChartController;

    public IngameScreenController(Player player) {
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
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/IngameScreen.fxml"));

        final HBox actionBox = (HBox) parent.lookup("#actionBox");

        gameController = new GameController(player);
        actionBox.getChildren().add(gameController.render());

        bossController = new BossController(player, player.getCurrentBoss());
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
            ap.getChildren().remove(0);
            try {
                ap.getChildren().add(progressChartController.render());
            } catch (IOException e1) {}
        };

        bossListener = e -> {
            ((Boss) e.getOldValue()).listeners().removePropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
            player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);

            bossController.destroy();
            bossController = new BossController(player, player.getCurrentBoss());
            bossController.init();

            deathController.destroy();
            deathController = new DeathController(player.getCurrentBoss(), player);

            actionBox.getChildren().remove(1);
            actionBox.getChildren().remove(1);

            progressChartController.destroy();
            progressChartController = new ProgressChartController(player);
            progressChartController.init();
            ap.getChildren().remove(0);

            try {
                actionBox.getChildren().add(bossController.render());
                actionBox.getChildren().add(deathController.render());
                ap.getChildren().add(progressChartController.render());
            } catch (IOException e1) {}
        };

        garbageListener = e -> {
            progressChartController.destroy();
            progressChartController = new ProgressChartController(player);
            progressChartController.init();
            ap.getChildren().remove(0);
            try {
                ap.getChildren().add(progressChartController.render());
            } catch (IOException e1) {}
        };

        timerListener = e -> {
            deathController.destroy();
            deathController = new DeathController(player.getCurrentBoss(), player);
            actionBox.getChildren().remove(2);
            try {
                actionBox.getChildren().add(deathController.render());
            } catch (IOException e1) {}
        };

        player.listeners().addPropertyChangeListener(Player.PROPERTY_CURRENT_BOSS, bossListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_SHOW_TIMER, timerListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_GARBAGE_FACTOR, garbageListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_SHOW_EXP, garbageListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_SHOW_LINEAR, garbageListener);
        player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);

        return parent;
    }

    @Override
    public void destroy() {
        gameController.destroy();
        bossController.destroy();
        deathController.destroy();
        progressChartController.destroy();

        player.listeners().removePropertyChangeListener(Player.PROPERTY_GARBAGE_FACTOR, garbageListener);
        player.listeners().removePropertyChangeListener(Player.PROPERTY_SHOW_EXP, garbageListener);
        player.listeners().removePropertyChangeListener(Player.PROPERTY_SHOW_LINEAR, garbageListener);

        player.listeners().removePropertyChangeListener(Player.PROPERTY_CURRENT_BOSS, bossListener);
        player.listeners().removePropertyChangeListener(Player.PROPERTY_SHOW_TIMER, timerListener);

        player.getCurrentBoss().listeners().removePropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
    }
    
}
