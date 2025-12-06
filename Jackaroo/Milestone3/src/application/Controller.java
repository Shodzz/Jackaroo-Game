package application;

import java.io.IOException;

import engine.Game;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller {
    @FXML
    private TextField PlayerNametf;
    @FXML
    private Label warn;

    private Stage stage;
    private Scene scene;

    /** Called when the user clicks “Start” in Scene 1. */
    @FXML
    public void switchtoScene2(ActionEvent event) {
        String username = PlayerNametf.getText().trim();

        // 1) Validate input
        if (username.isEmpty()) {
            showWarning("Name cannot be empty!");
            return;
        }

        // 2) Create the game model
        Game gameModel;
        try {
            gameModel = new Game(username);
        } catch (Exception e) {
            showWarning("Failed to start game: " + e.getMessage());
            return;
        }

        try {
            // 3) Load Scene 2 FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("JAKXML2.fxml"));
            Parent root = loader.load();

            // 4) Get the Controller2 instance and initialize it
            Controller2 ctrl2 = loader.getController();
            ctrl2.initGame(gameModel, username);

            // 5) Swap scenes
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException ioe) {
            ioe.printStackTrace();
            showWarning("Could not load game scene.");
        }
    }

    /** Show a temporary warning label for 3 seconds. */
    private void showWarning(String message) {
        warn.setText(message);
        warn.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> warn.setVisible(false));
        pause.play();
    }
}