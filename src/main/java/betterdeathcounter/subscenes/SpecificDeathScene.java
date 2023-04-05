package betterdeathcounter.subscenes;

import java.awt.Toolkit;
import java.util.List;

import betterdeathcounter.App;
import betterdeathcounter.Main;
import betterdeathcounter.model.Boss;
import betterdeathcounter.model.Death;
import betterdeathcounter.model.Player;
import betterdeathcounter.service.TimeService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SpecificDeathScene {

    final App app;

    public SpecificDeathScene(App app) {
        this.app = app;
    }

    public void showDeleteSpecificDeath(Player player) {
        app.getStage().getScene().getRoot().setDisable(true);
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

        for (int j = 0; j < deaths.size(); j+=5) {
            HBox deathrow = new HBox();
            deathrow = new HBox();
            deathrow.setSpacing(10);
            for (int k = 0; k < 5; k++) {
                if (j+k >= deaths.size()) {
                    break;
                }
                Death death = deaths.get(j+k);
                Label deathLabel = new Label("No. " + (j+k) + ": " + death.getPercentage() + "%");
                deathLabel.setPadding(new Insets(5));
                deathLabel.getStyleClass().add("death-label");
                deathLabel.setAlignment(Pos.CENTER);
                deathLabel.setOnMouseClicked(event -> {
                    currentBoss.withoutDeaths(death);
                    deathsPane.getScene().getWindow().hide();
                    TimeService.print("Death deleted: " + death.getPercentage());
                });
                deathrow.getChildren().add(deathLabel);
            }
            deathsPane.getChildren().add(deathrow);
        }
        
        ScrollPane scrollPane = new ScrollPane(deathsPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(420);
        
        Scene scene = new Scene(scrollPane);
        scene.setFill(Color.web("#1f85de"));
        scene.getStylesheets().add(Main.class.getResource("style/AboutStyle.css").toString());

        Stage stage = new Stage();
        stage.setTitle("Delete Death");
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.showAndWait();

        app.getStage().getScene().getRoot().setDisable(false);
        app.getStage().getScene().setOnMouseClicked(event ->{});
    }
}
