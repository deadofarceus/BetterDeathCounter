package betterdeathcounter.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import betterdeathcounter.App;
import betterdeathcounter.Main;
import betterdeathcounter.model.*;
import betterdeathcounter.service.IandOService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

public class IngameController implements Controller {

    private final App app;
    private final Player player;
    private final IandOService iandOService = new IandOService();
    private final Timer saveTimer = new Timer();
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
        if(player.getGames().isEmpty()) {
            player.setCurrentGame(new Game().setName("Please create a new game"));
            player.setCurrentBoss(new Boss().setName("Please create a new game").setSecondPhase(false));
        } else {
            Game g = player.getGames().get(player.getGames().size()-1);
            player.setCurrentGame(g);
            if(g.getBosses().isEmpty()) {
                player.setCurrentBoss(new Boss().setName("Other Monsters or Heights").setGame(g).setSecondPhase(false));
            }
        }

        for (Game game : player.getGames()) {
            for (Boss boss : game.getBosses()) {
                if(boss.getSecondPhase() == null) boss.setSecondPhase(false);
            }
        }

        saveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (Player oldPlayer : oldPlayers) {
                    iandOService.savePlayer(oldPlayer);
                }
                System.out.println("Saved all Players");
            }
        }, 30000, 60000);
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
        final Menu openOtherPlayer = (Menu) file.getItems().get(0);
        final MenuItem openFromExcel = file.getItems().get(1);
        final MenuItem save = file.getItems().get(3);
        final MenuItem saveAs = file.getItems().get(4);
        final MenuItem savegraph = file.getItems().get(6);
        final MenuItem quit = file.getItems().get(8);

        for (Player oldplayer : oldPlayers) {
            MenuItem mi = new MenuItem(oldplayer.getName());

            mi.setOnAction(e -> {
                Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
                exitDialog.setTitle("Change Player");
                exitDialog.setContentText("You sure, you want to change? Your State will be saved.");
                exitDialog.showAndWait();

                if(exitDialog.getResult() == ButtonType.OK) {
                    System.out.println("Changed player to: " + oldplayer.getName());
                    iandOService.savePlayer(player);
                    IngameController ic = new IngameController(app, oldPlayers, oldplayer);
                    ic.init();
                    app.show(ic);
                }
            });

            openOtherPlayer.getItems().add(mi);
        } 

        openFromExcel.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
     
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName(player.getCurrentGame().getName());
     
            File savedGame = fileChooser.showOpenDialog(app.getStage());
     
            if (file != null) {
                Game g = iandOService.getGameFrom(savedGame);
                player.withGames(g);
                player.setCurrentGame(g);
            }

            System.out.println("Opened Game from Excel: ");
        });

        save.setOnAction(e -> {
            iandOService.savePlayer(player);

            System.out.println("Saved Player");
        });

        saveAs.setOnAction(e -> {
            if(!player.getCurrentGame().getBosses().isEmpty()) {
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
                exitDialog.setTitle("No Bosses fo this Game");
                exitDialog.setContentText("You added 0 Bosses to the Game");

                exitDialog.showAndWait();
            }
        });

        savegraph.setOnAction(e -> {
            iandOService.saveGraph(player);
        });

        quit.setOnAction(e -> {
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
        });


        /*
         * EDIT MENU
         */
        final MenuItem newGame = edit.getItems().get(0);
        final MenuItem newBoss = edit.getItems().get(1);
        final Menu changeGame = (Menu) edit.getItems().get(3);
        final Menu changeBoss = (Menu) edit.getItems().get(4);
        final MenuItem deleteLastDeath = edit.getItems().get(6);
        final MenuItem deleteSpecificDeath = edit.getItems().get(7);

        newGame.setOnAction(e -> {
            TextInputDialog inputNewGame = new TextInputDialog();
            inputNewGame.setHeaderText("Enter new game name");
            inputNewGame.getEditor().setPromptText("New Game…");
            Optional<String> result = inputNewGame.showAndWait();
            
            if(result.isPresent() && !inputNewGame.getEditor().getText().equals("")) {
                boolean exists = false;
                for (Game game : player.getGames()) {
                    if(game.getName().equals(inputNewGame.getEditor().getText())) exists = true;
                }

                if(exists) {
                    Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    exitDialog.setTitle("Games exists");
                    exitDialog.setContentText("The game with the name " + inputNewGame.getEditor().getText() + " already exists!");
                    exitDialog.showAndWait();
                } else {
                    Game g = new Game().setName(inputNewGame.getEditor().getText());
                    Boss b = new Boss().setName("Other Monsters or Heights").setSecondPhase(false);
                    g.withBosses(b);
                    player.withGames(g);
                    player.setCurrentGame(g);
    
                    changeGame.getItems().add(gameMenuItem(g, changeBoss));
    
                    changeBoss.getItems().clear();
                    for (Boss otherBoss : player.getCurrentGame().getBosses()) {
                        changeBoss.getItems().add(bossMenuItem(otherBoss));
                    }
    
                    System.out.println("New game created: " + g.getName());
                }
            }
        });

        newBoss.setOnAction(e -> {
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
        
                        changeBoss.getItems().add(bossMenuItem(b));
        
                        System.out.println("New boss created: " + b.getName());
                    }

                }
            } else {
                Alert exitDialog = new Alert(Alert.AlertType.ERROR);
                exitDialog.setTitle("No Game");
                exitDialog.setContentText("Please create a game before you create a boss.");

                exitDialog.showAndWait();
            }
        });

        for (Game otherGame : player.getGames()) {
            changeGame.getItems().add(gameMenuItem(otherGame, changeBoss));
        }

        for (Boss otherBoss : player.getCurrentGame().getBosses()) {
            changeBoss.getItems().add(bossMenuItem(otherBoss));
        }

        deleteLastDeath.setOnAction(e -> {
            if(!player.getCurrentBoss().getDeaths().isEmpty()) {
                Death lastDeath = player.getCurrentBoss().getDeaths().get(player.getCurrentBoss().getDeaths().size()-1);
                player.getCurrentBoss().withoutDeaths(lastDeath);

                System.out.println("Last Death deleted: " + lastDeath.getPercentage());
            } else {
                Alert exitDialog = new Alert(Alert.AlertType.ERROR);
                exitDialog.setTitle("No Deaths for this Boss");
                exitDialog.setContentText("You died 0 times so no Death can be deleted");

                exitDialog.showAndWait();
            }
        });

        deleteSpecificDeath.setOnAction(e -> {

            Alert exitDialog = new Alert(Alert.AlertType.ERROR);
            exitDialog.setTitle("Warning");
            exitDialog.setContentText("Feature not yet implemented");

            exitDialog.showAndWait();

            // if(!player.getCurrentBoss().getDeaths().isEmpty()) {
            //     // SpecificDialog.display(player.getCurrentBoss().getDeaths());

            //     System.out.println("Deleted Death: ");
            // } else {
            //     Alert exitDialog = new Alert(Alert.AlertType.ERROR);
            //     exitDialog.setTitle("No Deaths for this Boss");
            //     exitDialog.setContentText("You died 0 times so no Death can be deleted");

            //     exitDialog.showAndWait();
            // }
        });

        /*
         * Graph Menu
         */
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

        /*
         * Connect Menu
         */
        final MenuItem googleUsernaMenuItem = connect.getItems().get(0);
        final MenuItem googleSheets = connect.getItems().get(1);

        googleUsernaMenuItem.setOnAction(e -> {
            TextInputDialog inputNewGame = new TextInputDialog();
            inputNewGame.setHeaderText("Enter your google API Username");
            inputNewGame.getEditor().setPromptText("Your username here…");
            if(player.getAPIUsername() != null) {
                inputNewGame.getEditor().setText(player.getAPIUsername());
            }
            Optional<String> result = inputNewGame.showAndWait();
            
            if(result.isPresent() && !inputNewGame.getEditor().getText().equals("")) {
                player.setAPIUsername(inputNewGame.getEditor().getText());
            }

        });

        googleSheets.setOnAction(e -> {
            Game g = player.getCurrentGame();
            TextInputDialog inputNewGame = new TextInputDialog();
            inputNewGame.setHeaderText("Enter the spreadsheetId for this game");
            inputNewGame.getEditor().setPromptText("Your spreadsheetId here…");
            if(g.getSpreadsheetId() != null) {
                inputNewGame.getEditor().setText(g.getSpreadsheetId());
            }
            Optional<String> result = inputNewGame.showAndWait();
            
            if(result.isPresent() && !inputNewGame.getEditor().getText().equals("")) {
                g.setSpreadsheetId(inputNewGame.getEditor().getText());
            }

        });

        /*
         * Ingame Screen
         */
        final AnchorPane anchor = (AnchorPane) parent.lookup("#anchor");
        isc = new IngameScreenController(player);
        anchor.getChildren().add(isc.render());

        return parent;
    }


    private MenuItem bossMenuItem(Boss b) {
        MenuItem mi = new MenuItem(b.getName());
        mi.setOnAction(event -> {
            Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
            exitDialog.setTitle("Change Boss");
            exitDialog.setContentText("You sure, you want to change? Your State will be saved.");
            exitDialog.showAndWait();

            if(exitDialog.getResult() == ButtonType.OK) {
                player.setCurrentBoss(b);
                System.out.println("Boss switched to: " + b.getName());
            }
        });
        return mi;
    }

    private MenuItem gameMenuItem(Game g, Menu changeBoss) {
        MenuItem mi = new MenuItem(g.getName());
        mi.setOnAction(event -> {
            Alert exitDialog = new Alert(Alert.AlertType.CONFIRMATION);
            exitDialog.setTitle("Change Game");
            exitDialog.setContentText("You sure, you want to change? Your State will be saved.");
            exitDialog.showAndWait();

            if(exitDialog.getResult() == ButtonType.OK) {
                player.setCurrentGame(g);

                changeBoss.getItems().clear();
                for (Boss otherBoss : player.getCurrentGame().getBosses()) {
                    changeBoss.getItems().add(bossMenuItem(otherBoss));
                }

                System.out.println("Game switched to: " + g.getName());
            }
        });
        return mi;
    }

    @Override
    public void destroy() {
        isc.destroy();
        saveTimer.cancel();
    }
    
}
