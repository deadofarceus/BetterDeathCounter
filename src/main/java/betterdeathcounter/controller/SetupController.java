package betterdeathcounter.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import betterdeathcounter.App;
import betterdeathcounter.Main;
import betterdeathcounter.model.Player;
import betterdeathcounter.model.Settings;
import betterdeathcounter.service.IandOService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SetupController implements Controller {

    private final App app;
    private final IandOService iandOService = new IandOService();
    private List<Controller> subcontrollers = new ArrayList<>(); 
    private List<Player> oldPlayers = new ArrayList<>();

    public SetupController(App app) {
        this.app = app;
    }

    @Override
    public String getTitle() {
        return "Better Deathcounter";
    }

    @Override
    public void init() {
        oldPlayers.addAll(iandOService.loadPlayers()) ;
    }

    @Override
    public Parent render() throws IOException {
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/Setup.fxml"));
        final TextField nameInput = (TextField) parent.lookup("#nameField");
        final Button createNewPlayer = (Button) parent.lookup("#createNewPlayer");
        final ScrollPane savedPlayersBar = (ScrollPane) parent.lookup("#savedPlayers");
        final VBox savedPlayers = (VBox) savedPlayersBar.getContent();

        createNewPlayer.setOnAction(e -> {
            if(!nameInput.getText().isBlank()) {
                File playerDir = new File("graphs/" + nameInput.getText());
                if (!playerDir.exists()){
                    playerDir.mkdirs();

                    Player player = new Player()
                        .setName(nameInput.getText())
                        .setSettings(new Settings()
                            .setShowLinear(true)
                            .setShowExp(true)
                            .setShowTimer(true)
                            .setUseCostumPrediction(false));
                    oldPlayers.add(player);
                    IngameController ic = new IngameController(app, oldPlayers, player);
                    ic.init();
                    app.show(ic);
                } else {
                    Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    exitDialog.setTitle("Player exists");
                    exitDialog.setContentText("The player with the name " + nameInput.getText() + " already exists!");
                    exitDialog.showAndWait();
                }
            }
        });

        for (Player player : oldPlayers) {
            PlayerController pc = new PlayerController(player, oldPlayers, app);
            subcontrollers.add(pc);
            savedPlayers.getChildren().add(pc.render());
        }

        return parent;
    }

    @Override
    public void destroy() {
        for (Controller controller : subcontrollers) {
            controller.destroy();
        }
    }
    
}
