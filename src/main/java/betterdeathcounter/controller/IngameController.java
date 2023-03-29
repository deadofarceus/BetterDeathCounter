package betterdeathcounter.controller;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import betterdeathcounter.App;
import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Death;
import betterdeathcounter.model.Game;
import betterdeathcounter.model.Player;
import betterdeathcounter.service.IandOService;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class IngameController implements Controller {

    private final App app;
    private final Player player;
    private final IandOService iandOService = new IandOService();
    private final ScheduledExecutorService saveScheduler = Executors.newSingleThreadScheduledExecutor();
    private List<Player> oldPlayers = new ArrayList<>();
    private IngameScreenController isc;

    public IngameController(App app, List<Player> oldPlayers, Player player) {
        this.app = app;
        this.player = player;
        this.oldPlayers = oldPlayers;
    }

    @Override
    public String getTitle() {
        return player.getName() + " - " + player.getCurrentGame().getName();
    }

    @Override
    public void init() {
        initializePlayerData();
        initializeBossData();

        ScheduledExecutorService saveScheduler = Executors.newSingleThreadScheduledExecutor();
        saveScheduler.scheduleAtFixedRate(() -> {
            for (Player oldPlayer : oldPlayers) {
                iandOService.savePlayer(oldPlayer);
            }
            System.out.println("Saved all Players");
        }, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public Parent render() throws IOException {
        final Parent parent = FXMLLoader.load(Main.class.getResource("view/InGame.fxml"));

        /*
         * Menu Actions
         */
        final MenuBar optionBar = (MenuBar) parent.lookup("#optionBar");

        final Menu file = optionBar.getMenus().get(0);
        final Menu edit = optionBar.getMenus().get(1);
        final Menu graph = optionBar.getMenus().get(2);
        final Menu connect = optionBar.getMenus().get(3);

        /*
         * FILE MENU
         */
        initializeFileMenuItems(file);

        /*
         * EDIT MENU
         */
        initializeEditMenuItems(edit, parent);
        
        /*
         * Graph Menu
         */
        initializeGraphMenuItems(graph);

        /*
         * Connect Menu
         */
        initializeConnectMenuItems(connect);

        /*
         * Ingame Screen
         */
        final AnchorPane anchor = (AnchorPane) parent.lookup("#anchor");
        initializeIngameScreen(anchor);
        
        return parent;
    }

    private void initializeBossData() {
        for (Game game : player.getGames()) {
            for (Boss boss : game.getBosses()) {
                if(boss.getSecondPhase() == null) boss.setSecondPhase(false);
            }
        }
    }

    private void initializePlayerData() {
        if(player.getGames().isEmpty()) {
            player.setCurrentGame(new Game().setName("Please create a new game"));
            player.setCurrentBoss(new Boss().setName("Please create a new game").setSecondPhase(false));
        } else {
            Game g = player.getCurrentGame();
            if(g.getBosses().isEmpty()) {
                player.setCurrentBoss(new Boss().setName("Other Monsters or Heights").setGame(g).setSecondPhase(false));
            }
        }
    }

    private void initializeFileMenuItems(Menu file) {
        final Menu openOtherPlayer = (Menu) file.getItems().get(0);
        final MenuItem openFromExcel = file.getItems().get(1);
        final MenuItem save = file.getItems().get(3);
        final MenuItem saveAs = file.getItems().get(4);
        final MenuItem savegraph = file.getItems().get(6);
        final MenuItem quit = file.getItems().get(8);

        for (Player oldplayer : oldPlayers) {
            MenuItem mi = new MenuItem(oldplayer.getName());
            mi.setOnAction(e -> handleOpenOtherPlayer(oldplayer));
            openOtherPlayer.getItems().add(mi);
        }

        openFromExcel.setOnAction(e -> handleOpenFromExcel());

        save.setOnAction(e -> {
            iandOService.savePlayer(player);

            System.out.println("Saved Player");
        });

        saveAs.setOnAction(e -> handleSaveAs());

        savegraph.setOnAction(e -> {
            iandOService.saveGraph(player);
        });

        quit.setOnAction(e -> handleQuit());
    }

    private void handleOpenOtherPlayer(Player oldplayer) {
        Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
        exitDialog.setTitle("Change Player");
        exitDialog.setContentText("You sure, you want to change? Your State will be saved.");
        exitDialog.showAndWait();
    
        if (exitDialog.getResult() == ButtonType.OK) {
            System.out.println("Changed player to: " + oldplayer.getName());
            iandOService.savePlayer(player);
            IngameController ic = new IngameController(app, oldPlayers, oldplayer);
            ic.init();
            app.show(ic);
        }
    }
    
    private void handleOpenFromExcel() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName(player.getCurrentGame().getName());
     
        File savedGame = fileChooser.showOpenDialog(app.getStage());
     
        if (savedGame != null) {
            Game g = iandOService.getGameFrom(savedGame);
            player.withGames(g);
            player.setCurrentGame(g);
        }
    
        System.out.println("Opened Game from Excel: ");
    }
    
    private void handleSaveAs() {
        if (!player.getCurrentGame().getBosses().isEmpty()) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName(player.getCurrentGame().getName());
     
            File savedGame = fileChooser.showSaveDialog(app.getStage());
     
            if (savedGame != null) {
                System.out.println("Saved Game as Excel under: " + savedGame.getPath());
                iandOService.saveGame(player.getCurrentGame(), savedGame.getPath());
            }
        } else {
            Alert exitDialog = new Alert(Alert.AlertType.ERROR);
            exitDialog.setTitle("No Bosses for this Game");
            exitDialog.setContentText("You added 0 Bosses to the Game");
            exitDialog.showAndWait();
        }
    }
    
    private void handleQuit() {
        Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
        exitDialog.setTitle("Quit Deathcounter");
        exitDialog.setContentText("You sure, you want to quit? Your State will be saved.");
    
        exitDialog.showAndWait();
        
        if (exitDialog.getResult() == ButtonType.OK) {
            for (Player oldPlayer : oldPlayers) {
                iandOService.savePlayer(oldPlayer);
            }
            System.exit(0);
        }
    }

    private void initializeEditMenuItems(Menu edit, Parent parent) {
        final MenuItem newGame = edit.getItems().get(0);
        final MenuItem newBoss = edit.getItems().get(1);
        final Menu changeGame = (Menu) edit.getItems().get(3);
        final Menu changeBoss = (Menu) edit.getItems().get(4);
        final MenuItem deleteLastDeath = edit.getItems().get(6);
        final MenuItem deleteSpecificDeath = edit.getItems().get(7);

        newGame.setOnAction(e -> handleNewGame(changeGame, changeBoss));

        newBoss.setOnAction(e -> handleNewBoss(changeBoss));

        for (Game otherGame : player.getGames()) {
            MenuItem mi = new MenuItem(otherGame.getName());
            mi.setOnAction(e -> handleChangeGame(otherGame, changeBoss));
            changeGame.getItems().add(mi);
        }

        for (Boss otherBoss : player.getCurrentGame().getBosses()) {
            MenuItem mi = new MenuItem(otherBoss.getName());
            mi.setOnAction(e -> handleChangeBoss(otherBoss));
            changeBoss.getItems().add(mi);
        }

        deleteLastDeath.setOnAction(e -> handleDeleteLastDeath());

        deleteSpecificDeath.setOnAction(e -> handleDeleteSpecificDeath(parent));

    }

    private void handleNewGame(Menu changeGame, Menu changeBoss) {
        TextInputDialog inputNewGame = new TextInputDialog();
            inputNewGame.setHeaderText("Enter new game name");
            inputNewGame.getEditor().setPromptText("New Game…");
            Optional<String> result = inputNewGame.showAndWait();
        
            if (!result.isPresent() || inputNewGame.getEditor().getText().equals("")) {
                return;
            }
        
            String gameName = inputNewGame.getEditor().getText();
            if (player.getGames().stream().anyMatch(game -> game.getName().equals(gameName))) {
                Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
                exitDialog.setTitle("Game already exists");
                exitDialog.setContentText("The game with the name " + gameName + " already exists!");
                exitDialog.showAndWait();
                return;
            }
        
            Game g = new Game().setName(gameName);
            Boss b = new Boss().setName("Other Monsters or Heights").setSecondPhase(false);
            g.withBosses(b);
            player.withGames(g);
            player.setCurrentGame(g);
        
            MenuItem mi = new MenuItem(g.getName());
            mi.setOnAction(e -> handleChangeGame(g, changeBoss));
            changeGame.getItems().add(mi);
        
            changeBoss.getItems().clear();
            player.getCurrentGame().getBosses().stream()
                .map(otherBoss -> {
                    MenuItem bossMi = new MenuItem(otherBoss.getName());
                    bossMi.setOnAction(e -> handleChangeBoss(otherBoss));
                    return bossMi;
                })
                .forEach(changeBoss.getItems()::add);
        
            app.getStage().setTitle(getTitle());
        
            System.out.println("New game created: " + g.getName());
    }

    private void handleNewBoss(Menu changeBoss) {
        if(!player.getGames().isEmpty()) {
            TextInputDialog inputNewBoss = new TextInputDialog();
            inputNewBoss.setHeaderText("Enter new boss name");
            inputNewBoss.getEditor().setPromptText("New Boss…");
            Optional<String> result = inputNewBoss.showAndWait();
            
            if(result.isPresent() && !inputNewBoss.getEditor().getText().equals("")) {
                boolean exists = false;
                for (Boss boss : player.getCurrentGame().getBosses()) {
                    if(boss.getName().equals(inputNewBoss.getEditor().getText())) exists = true;
                }
                
                if(exists) {
                    Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    exitDialog.setTitle("Boss exists");
                    exitDialog.setContentText("The boss with the name " + inputNewBoss.getEditor().getText() + " already exists!");
                    exitDialog.showAndWait();
                } else {
                    Boss b = new Boss()
                        .setName(inputNewBoss.getEditor().getText())
                        .setGame(player.getCurrentGame())
                        .setSecondPhase(false);
                    player.setCurrentBoss(b);
    
                    MenuItem mi = new MenuItem(b.getName());
                    mi.setOnAction(e -> handleChangeBoss(b));
                    changeBoss.getItems().add(mi);
    
                    System.out.println("New boss created: " + b.getName());
                }
    
            }
        } else {
            Alert exitDialog = new Alert(Alert.AlertType.ERROR);
            exitDialog.setTitle("No Game");
            exitDialog.setContentText("Please create a game before you create a boss.");
    
            exitDialog.showAndWait();
        }
    }

    private void handleChangeGame(Game otherGame, Menu changeBoss) {
        Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
        exitDialog.setTitle("Change Game");
        exitDialog.setContentText("Are you sure you want to change? Your state will be saved.");
        exitDialog.showAndWait();
    
        if(exitDialog.getResult() == ButtonType.OK) {
            player.setCurrentGame(otherGame);
            player.setCurrentBoss(otherGame.getBosses().get(otherGame.getBosses().size()-1));
    
            changeBoss.getItems().clear();
            player.getCurrentGame().getBosses().stream()
                .map(otherBoss -> {
                    MenuItem bossMi = new MenuItem(otherBoss.getName());
                    bossMi.setOnAction(e -> handleChangeBoss(otherBoss));
                    return bossMi;
                })
                .forEach(changeBoss.getItems()::add);
    
            app.getStage().setTitle(getTitle());
    
            System.out.println("Game switched to: " + otherGame.getName());
        }
    }

    private void handleChangeBoss(Boss otherBoss) {
        Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
        exitDialog.setTitle("Change Boss");
        exitDialog.setContentText("Are you sure you want to change? Your state will be saved.");
        exitDialog.showAndWait();
    
        if (exitDialog.getResult() == ButtonType.OK) {
            player.setCurrentBoss(otherBoss);
            System.out.println("Boss switched to: " + otherBoss.getName());
        }
    }

    private void handleDeleteLastDeath() {
        if (!player.getCurrentBoss().getDeaths().isEmpty()) {
            Death lastDeath = player.getCurrentBoss().getDeaths().get(player.getCurrentBoss().getDeaths().size() - 1);
            player.getCurrentBoss().withoutDeaths(lastDeath);
    
            System.out.println("Last Death deleted: " + lastDeath.getPercentage());
        } else {
            Alert exitDialog = new Alert(Alert.AlertType.ERROR);
            exitDialog.setTitle("No Deaths for this Boss");
            exitDialog.setContentText("You died 0 times so no Death can be deleted");
            exitDialog.showAndWait();
        }
    }

    private void handleDeleteSpecificDeath(Parent parent) {
        parent.setDisable(true);
        app.getStage().getScene().setOnMouseClicked(event -> {
            Toolkit.getDefaultToolkit().beep();
        });
        
        Boss currentBoss = player.getCurrentBoss();
        List<Death> deaths = currentBoss.getDeaths();

        if (deaths.isEmpty()) {
            Alert exitDialog = new Alert(Alert.AlertType.ERROR);
            exitDialog.setTitle("No Deaths for this Boss");
            exitDialog.setContentText("There are no deaths to delete.");
            exitDialog.showAndWait();
            return;
        }
        
        VBox deathsPane = new VBox();
        deathsPane.setSpacing(5);
        deathsPane.setStyle("-fx-background-color: #1f85de;");
        deathsPane.setPadding(new Insets(10));

        int i = 0;
        for (int j = 0; j < deaths.size(); j+=5) {
            HBox deathrow = new HBox();
            deathrow = new HBox();
            deathrow.setSpacing(10);
            for (int k = 0; k < 5; k++) {
                if (j+k >= deaths.size()) {
                    break;
                }
                Death death = deaths.get(j+k);
                Label deathLabel = new Label("No. " + i+1 + ": " + death.getPercentage() + "%");
                deathLabel.setPadding(new Insets(5));
                deathLabel.setStyle("-fx-font-size: 18px; -fx-background-color: #0b2fb0; -fx-border-color: #de781f; -fx-border-width: 2px;");
                deathLabel.setTextFill(Color.web("#f2f2f2"));
                deathLabel.setPrefSize(150, 50);
                deathLabel.setAlignment(Pos.CENTER);
                deathLabel.setOnMouseClicked(event -> {
                    currentBoss.withoutDeaths(death);
                    deathsPane.getScene().getWindow().hide();
                    System.out.println("Death deleted: " + death.getPercentage());
                });
                deathrow.getChildren().add(deathLabel);
            }
            deathsPane.getChildren().add(deathrow);
            i++;
        }
        
        ScrollPane scrollPane = new ScrollPane(deathsPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(420);
        
        Scene scene = new Scene(scrollPane);
        scene.setFill(Color.web("#1f85de"));

        Stage stage = new Stage();
        stage.setTitle("Delete Death");
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.showAndWait();

        parent.setDisable(false);
        app.getStage().getScene().setOnMouseClicked(event ->{});
    }

    private void initializeGraphMenuItems(Menu graph) {
        final RadioMenuItem linear = (RadioMenuItem) graph.getItems().get(0);
        final RadioMenuItem exp = (RadioMenuItem) graph.getItems().get(1);
        final RadioMenuItem timer = (RadioMenuItem) graph.getItems().get(2);
        
        linear.selectedProperty().set(player.getShowLinear());
        exp.selectedProperty().set(player.getShowExp());
        timer.selectedProperty().set(player.getShowTimer());

        linear.setOnAction(e -> {
            player.setShowLinear(!player.getShowLinear());
        });

        exp.setOnAction(e -> {
            player.setShowExp(!player.getShowExp());
        });

        timer.setOnAction(e -> {
            player.setShowTimer(!player.getShowTimer());
        });
    }
    
    private void initializeConnectMenuItems(Menu connect) {
        final MenuItem googleUsernameMenuItem = connect.getItems().get(0);
        final MenuItem googleSheets = connect.getItems().get(1);

        googleUsernameMenuItem.setOnAction(e -> handleGoogleUsername());

        googleSheets.setOnAction(e -> handleGoogleSheets());
    }

    private void handleGoogleUsername() {
        TextInputDialog inputDialog = new TextInputDialog(player.getAPIUsername());
        inputDialog.setHeaderText("Enter your Google API Username");
        inputDialog.getEditor().setPromptText("Your username here…");
        if(player.getAPIUsername() != null) {
            inputDialog.getEditor().setText(player.getAPIUsername());
        }

        Optional<String> result = inputDialog.showAndWait();
        result.ifPresent(username -> player.setAPIUsername(username));
    }

    private void handleGoogleSheets() {
        Game currentGame = player.getCurrentGame();
        TextInputDialog inputDialog = new TextInputDialog(currentGame.getSpreadsheetId());
        inputDialog.setHeaderText("Enter the spreadsheet ID for this game");
        inputDialog.getEditor().setPromptText("Your spreadsheet ID here…");
        if(currentGame.getSpreadsheetId() != null) {
            inputDialog.getEditor().setText(currentGame.getSpreadsheetId());
        }
    
        Optional<String> result = inputDialog.showAndWait();
        result.ifPresent(spreadsheetId -> currentGame.setSpreadsheetId(spreadsheetId));
    }

    private void initializeIngameScreen(AnchorPane anchor) throws IOException {
        isc = new IngameScreenController(player);
        anchor.getChildren().add(isc.render());
    }

    @Override
    public void destroy() {
        isc.destroy();
        saveScheduler.shutdown();
    }
    
}
