package application;
	
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

import java.util.ArrayList;

import engine.Game;
import engine.board.Cell;
import exception.GameException;
import exception.InvalidCardException;
import exception.InvalidMarbleException;
import model.Colour;
import model.card.Card;
import model.card.standard.Standard;
import model.player.Marble;
import model.player.Player;


public class Main extends Application {
	private Game game;
	public Label warn;
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("JAKXML.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
    public void showWarning(String message) {
        warn.setText(message);
        warn.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> warn.setVisible(false));
        pause.play();
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}
