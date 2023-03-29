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
    private PropertyChangeListener bossListener, deathListener, timerListener;
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
            try {
                ap.getChildren().set(0, progressChartController.render());
            } catch (IOException ignored) {}
        };

        bossListener = e -> {
            ((Boss) e.getOldValue()).listeners().removePropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
            player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);

            bossController.destroy();
            bossController = new BossController(player, player.getCurrentBoss());
            bossController.init();

            deathController.destroy();
            deathController = new DeathController(player.getCurrentBoss(), player);

            progressChartController.destroy();
            progressChartController = new ProgressChartController(player);
            progressChartController.init();

            try {
                actionBox.getChildren().set(1, bossController.render());
                actionBox.getChildren().set(2, deathController.render());
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
        player.listeners().addPropertyChangeListener(Player.PROPERTY_SHOW_TIMER, timerListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_GARBAGE_FACTOR, deathListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_SHOW_EXP, deathListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_SHOW_LINEAR, deathListener);
        player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);

        return parent;
    }

    @Override
    public void destroy() {
        gameController.destroy();
        bossController.destroy();
        deathController.destroy();
        progressChartController.destroy();

        player.listeners().removePropertyChangeListener(Player.PROPERTY_CURRENT_BOSS, bossListener);
        player.listeners().removePropertyChangeListener(Player.PROPERTY_SHOW_TIMER, timerListener);
        player.listeners().removePropertyChangeListener(Player.PROPERTY_GARBAGE_FACTOR, deathListener);
        player.listeners().removePropertyChangeListener(Player.PROPERTY_SHOW_EXP, deathListener);
        player.listeners().removePropertyChangeListener(Player.PROPERTY_SHOW_LINEAR, deathListener);
        player.getCurrentBoss().listeners().removePropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
    }
    
}
