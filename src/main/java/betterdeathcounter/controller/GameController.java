package betterdeathcounter.controller;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Game;
import betterdeathcounter.model.Player;
import betterdeathcounter.service.CalculateService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.text.Text;

public class GameController implements Controller {

    private final Player player;
    private final CalculateService calculateService = new CalculateService();
    private PropertyChangeListener gameListener, bossListener, deathListener;

    public GameController(Player player) {
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
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/Game.fxml"));

        final Text gameName = (Text) parent.lookup("#gameName");
        final Text allDeaths = (Text) parent.lookup("#allDeaths");

        gameName.setText(player.getCurrentGame().getName());
        allDeaths.setText("Total Deaths: " + "\n" + calculateService.getNumOfDeaths(player.getCurrentGame()));


        /*
         * Player Listener
         */
        gameListener = e -> {
            gameName.setText(player.getCurrentGame().getName());
            allDeaths.setText("Total Deaths: " + "\n" + calculateService.getNumOfDeaths(player.getCurrentGame()));

            Game g = player.getCurrentGame();
            player.setCurrentBoss(player.getCurrentGame().getBosses().get(g.getBosses().size()-1));
        };

        bossListener = e -> {
            ((Boss) e.getOldValue()).listeners().removePropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
            player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
        };
        
        deathListener = e -> {
            allDeaths.setText("Total Deaths: " + "\n" + calculateService.getNumOfDeaths(player.getCurrentGame()));
        };

        player.listeners().addPropertyChangeListener(Player.PROPERTY_CURRENT_GAME, gameListener);
        player.listeners().addPropertyChangeListener(Player.PROPERTY_CURRENT_BOSS, bossListener);
        player.getCurrentBoss().listeners().addPropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);

        return parent;
    }

    @Override
    public void destroy() {
        player.listeners().removePropertyChangeListener(gameListener);
        player.listeners().removePropertyChangeListener(bossListener);
        player.getCurrentBoss().listeners().removePropertyChangeListener(Boss.PROPERTY_DEATHS, deathListener);
    }
    
}
