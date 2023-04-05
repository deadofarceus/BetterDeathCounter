package betterdeathcounter.controller;

import java.io.IOException;
import java.util.List;

import betterdeathcounter.App;
import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Game;
import betterdeathcounter.model.Player;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class PlayerController implements Controller {

    private final Player player;
    private final App app;
    private List<Player> oldPlayers;

    public PlayerController(Player player, List<Player> oldPlayers, App app) {
        this.player = player;
        this.oldPlayers = oldPlayers;
        this.app = app;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void init() {
    }

    @Override
    public Parent render() throws IOException {
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/LoadPlayer.fxml"));
        final Text name = (Text) parent.lookup("#name");
        final Text games = (Text) parent.lookup("#games");
        final Text deaths = (Text) parent.lookup("#deaths");

        name.setText(player.getName());
        games.setText("Games played: " + player.getGames().size());
        int allDeaths = 0;
        for (Game game : player.getGames()) {
            for (Boss boss : game.getBosses()) {
                allDeaths += boss.getDeaths().size();
            }
        }
        deaths.setText("Deaths: " + allDeaths);

        final Button loadButton = (Button) parent.lookup("#loadButton");

        loadButton.setOnAction(e -> {
            IngameController ic = new IngameController(app, oldPlayers, player);
            app.show(ic);
        });

        return parent;
    }

    @Override
    public void destroy() {
    }
    
}
