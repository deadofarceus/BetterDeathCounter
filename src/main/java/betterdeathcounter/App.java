package betterdeathcounter;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

import betterdeathcounter.controller.Controller;
import betterdeathcounter.controller.SetupController;

public class App extends Application {
    private Stage stage;
    private Controller controller;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        primaryStage.setScene(new Scene(new Label("Loading...")));
        primaryStage.setTitle("Better Deathcounter");
        primaryStage.setResizable(false);

        show(new SetupController(this));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> controller.destroy());
    }

    public void show(Controller controller) {
        controller.init();
        try {
            stage.getScene().setRoot(controller.render());
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        if (this.controller != null) {
            this.controller.destroy();
        }
        this.controller = controller;
        stage.setTitle(controller.getTitle());
    }

    public Stage getStage() {
        return stage;
    }
}