package betterdeathcounter.subscenes;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.net.URI;

import betterdeathcounter.App;
import betterdeathcounter.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AboutScene {

    final App app;

    public AboutScene(App app) {
        this.app = app;
    }

    public void showAbout() {
        app.getStage().getScene().getRoot().setDisable(true);
        app.getStage().getScene().setOnMouseClicked(event -> {
            Toolkit.getDefaultToolkit().beep();
        });

        Stage stage = new Stage();
        stage.setTitle("About");
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(20);
        vbox.setAlignment(Pos.CENTER);

        Label versionLabel = new Label("Version: 1.0");
        versionLabel.getStyleClass().add("info-label");
        vbox.getChildren().add(versionLabel);

        Label programmerLabel = new Label("Developted by deadofarceus");
        programmerLabel.getStyleClass().add("info-label");
        vbox.getChildren().add(programmerLabel);

        Label thanksLabel = new Label("Special thanks to\nkutcherlol\nfor the idea");
        thanksLabel.getStyleClass().add("thanks-label");
        thanksLabel.setOnMouseClicked(e -> {
            try {
                String url = "https://www.twitch.tv/kutcherlol";
                Desktop desktop = java.awt.Desktop.getDesktop();
                URI uri = new java.net.URI(url);
                desktop.browse(uri);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }); 
        vbox.getChildren().add(thanksLabel);

        Label infoLabel = new Label("Other information: ...");
        infoLabel.getStyleClass().add("info-label");
        infoLabel.setOnMouseClicked(e -> {
            try {
                String url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
                Desktop desktop = java.awt.Desktop.getDesktop();
                URI uri = new java.net.URI(url);
                desktop.browse(uri);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }); 
        vbox.getChildren().add(infoLabel);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> stage.close());

        vbox.getChildren().add(closeButton);

        Scene scene = new Scene(vbox, 500, 375);

        scene.getStylesheets().add(Main.class.getResource("style/AboutStyle.css").toString());
        
        stage.setScene(scene);
        stage.showAndWait();

        app.getStage().getScene().getRoot().setDisable(false);
        app.getStage().getScene().setOnMouseClicked(event ->{});
    }
}
