package application;

import engine.Game;
import engine.board.Board;
import engine.board.Cell;
import exception.CannotDiscardException;
import exception.CannotFieldException;
import exception.GameException;
import exception.IllegalDestroyException;
import exception.InvalidCardException;
import exception.InvalidMarbleException;
import exception.SplitOutOfRangeException;
import model.Colour;
import model.card.standard.Standard;
import model.card.Card;
import model.player.Marble;
import model.player.Player;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.control.Slider;
import javafx.geometry.Insets;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Controller2 implements Initializable {
    @FXML private Label playerName;
    @FXML private Label CurrentPlayer;
    @FXML private Label NextPlayer;
    @FXML private Label NumofCards;
    @FXML private Label NumofTurns;
    @FXML private Label CPU1;
    @FXML private Label CPU2;
    @FXML private Label CPU3;
    @FXML private VBox CPU1Cards;
    @FXML private HBox CPU2Cards;
    @FXML private VBox CPU3Cards;
    @FXML public Label warn;
    @FXML private HBox PlayerCards;
    @FXML private ImageView FirePitimage;
    @FXML private Label FirePitlabel;
    @FXML private HBox Track0, Track2, Track4, Track6, Track8, Track10;
    @FXML private VBox Track1, Track3, Track5, Track7, Track9, Track11;
    @FXML private VBox Safezone0, Safezone2;
    @FXML private HBox Safezone1, Safezone3;
    @FXML private HBox Homezone0, Homezone00,
                       Homezone1, Homezone11,
                       Homezone2, Homezone22,
                       Homezone3, Homezone33;
    @FXML private VBox splitDistanceControls;
    @FXML private Slider splitDistanceSlider;
    @FXML private Label splitDistanceValueLabel;
    @FXML private Button confirmSplitButton;
    private boolean cpuScheduled = false;
    private Game game;
    private Board board;
    private List<Pane> trackPanes;
    private List<Pane> safeZones;
    private List<HBox> homeZoneRows;
    private final Image emptyImage = new Image(
        getClass().getResourceAsStream("/application/Empty.png")
    );

    @Override
    public void initialize(URL loc, ResourceBundle rb) {
        trackPanes = Arrays.asList(
            Track0, Track1, Track2, Track3,
            Track4, Track5, Track6, Track7,
            Track8, Track9, Track10, Track11
        );
        if (FirePitimage != null) {
            FirePitimage.setVisible(false);
        }
        safeZones = Arrays.asList(
            Safezone0, Safezone1, Safezone2, Safezone3
        );
        homeZoneRows = Arrays.asList(
            Homezone0, Homezone00,
            Homezone1, Homezone11,
            Homezone2, Homezone22,
            Homezone3, Homezone33
        );
        if (splitDistanceSlider != null) {
            splitDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                int value = newValue.intValue();
                splitDistanceValueLabel.setText("Current: " + value);
            });
        }
    }

    public void initGame(Game gameModel, String username) {
        this.game = gameModel;
        this.board = game.getBoard();
        playerName.setText("Player: " + username);
        refreshGameUI();
    }

    private void refreshGameUI() {
        Colour winner = game.checkWin();
        if (winner != null) {
            showWinner(winner + " has won!");
            return;
        }
        if (splitDistanceControls != null) {
            splitDistanceControls.setVisible(false);
        }
        showPlayerCards();
        updateCPUCardVisuals();
        if(game.getFirePit().size() > 0) {
            int lastIndex = game.getFirePit().size()-1;
            Card topCard = game.getFirePit().get(lastIndex);
            if(topCard != null) {
                FirePitlabel.setText(topCard.getName());
                Image cardImage = getCardImage(topCard);
                FirePitimage.setImage(cardImage);
                FirePitimage.setPreserveRatio(true);
                FirePitimage.setVisible(true);
            }
        } else {
            FirePitlabel.setText("FirePit");
            FirePitimage.setVisible(false);
        }
        CurrentPlayer.setText("Current: " + game.getActivePlayerColour());
        NextPlayer.setText("Next: " + game.getNextPlayerColour());
        NumofCards.setText("Cards: " + game.getPlayers().get(0).getHand().size());
        CPU1.setText("CPU1: " + game.getPlayers().get(1).getHand().size());
        CPU2.setText("CPU2: " + game.getPlayers().get(2).getHand().size());
        CPU3.setText("CPU3: " + game.getPlayers().get(3).getHand().size());
        try {
            java.lang.reflect.Field f = game.getClass().getDeclaredField("turn");
            f.setAccessible(true);
            int turnValue = (int) f.get(game);
            NumofTurns.setText("Turns: " + turnValue);
        } catch (Exception e) {
            NumofTurns.setText("Turns: ?");
        }

        if(game.getFirePit().size() > 0){
            int LI = game.getFirePit().size()-1;
            if(game.getFirePit().get(LI) != null){
                FirePitlabel.setText(game.getFirePit().get(LI).getName());
            }
        }

        drawBoard();
        showPlayerCards();

        int currentIndex = getCurrentPlayerIndex();
        if (currentIndex != 0 && !cpuScheduled) {
            cpuScheduled = true;
            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(e -> {
                cpuScheduled = false;
                cpuTakeTurn();
            });
            delay.play();
        }
    }

    private void showPlayerCards() {
        PlayerCards.getChildren().clear();
        Player human = game.getPlayers().get(0);
        for (Card c : human.getHand()) {
            Button b = new Button();
            b.setPrefSize(150, 250);
            Image cardImage = getCardImage(c);
            ImageView iv = new ImageView(cardImage);
            iv.setFitWidth(150);
            iv.setFitHeight(250);
            iv.setPreserveRatio(true);
            b.setGraphic(iv);
            b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            b.setStyle("-fx-background-color: transparent; -fx-background-radius: 0; -fx-background-insets: 0; -fx-border-width: 0;");
            if (c == human.getSelectedCard()) {
                b.setStyle("-fx-background-color: transparent; -fx-border-color: gold; -fx-border-width: 3;");
            }
            b.setOnMouseEntered(e -> {
                if (c != human.getSelectedCard()) {
                    b.setStyle("-fx-background-color: transparent; -fx-effect: dropshadow(three-pass-box, rgba(255,215,0,0.8), 10, 0, 0, 0);");
                }
            });
            b.setOnMouseExited(e -> {
                if (c != human.getSelectedCard()) {
                    b.setStyle("-fx-background-color: transparent; -fx-background-radius: 0; -fx-background-insets: 0; -fx-border-width: 0;");
                } else {
                    b.setStyle("-fx-background-color: transparent; -fx-border-color: gold; -fx-border-width: 3;");
                }
            });
            b.setOnAction(e -> handleCardClick(c));
            PlayerCards.getChildren().add(b);
        }
    }

    private void handleCardClick(Card c) {
        try {
            game.selectCard(c);
            if (c instanceof Standard && ((Standard)c).getRank() == 7) {
                splitDistanceControls.setVisible(true);
                return;
            }
            int marblesBefore = game.getPlayers().get(getCurrentPlayerIndex()).getMarbles().size();
            if (game.canPlayTurn()) {
                game.playPlayerTurn();
                int marblesAfter = game.getPlayers().get(getCurrentPlayerIndex()).getMarbles().size();
                if (marblesAfter > marblesBefore) {
                    showWarning("Your marble hit a trap and was sent back to the home zone!");
                }
            }
            game.endPlayerTurn();
            refreshGameUI();
        } catch (Exception ex) {
            showWarning(ex.getMessage());
        }
    }

    @FXML
    public void confirmSplitDistance(ActionEvent event) {
        try {
            int splitDistance = (int) splitDistanceSlider.getValue();
            game.editSplitDistance(splitDistance);
            splitDistanceControls.setVisible(false);
            int marblesBefore = game.getPlayers().get(getCurrentPlayerIndex()).getMarbles().size();
            if (game.canPlayTurn()) {
                game.playPlayerTurn();
                int marblesAfter = game.getPlayers().get(getCurrentPlayerIndex()).getMarbles().size();
                if (marblesAfter > marblesBefore) {
                    showWarning("Your marble hit a trap and was sent back to the home zone!");
                }
            }
            game.endPlayerTurn();
            refreshGameUI();
        } catch (Exception ex) {
            showWarning(ex.getMessage());
        }
    }

    private void checkForTrapActivation() {
        List<Marble> homeMarbles = game.getPlayers().get(getCurrentPlayerIndex()).getMarbles();
        if (!homeMarbles.isEmpty()) {
            showWarning("A marble has hit a trap and been sent back to home zone!");
        }
    }

    private void drawBoard() {
        List<Cell> cells = board.getTrack();
        int start = 0;

        for (Pane pane : trackPanes) {
            clearPane(pane);
        }

        for (int pi = 0; pi < trackPanes.size(); pi++) {
            Pane pane = trackPanes.get(pi);
            int len = (pane instanceof VBox) ? 9 : 8;

            for (int offset = 0; offset < len && start + offset < cells.size(); offset++) {
                Cell cell = cells.get(start + offset);
                Marble m = cell.getMarble();

                int childIndex;
                switch(pi) {
                    case 0:
                        childIndex = (len - 1) - offset;
                        break;
                    case 2:
                        childIndex = (len - 1) - offset;
                        break;
                    case 4:
                        childIndex = offset;
                        break;
                    case 6:
                        childIndex = offset;
                        break;
                    case 8:
                        childIndex = offset;
                        break;
                    case 10:
                        childIndex = (len - 1) - offset;
                        break;
                    case 1:
                        childIndex = (len - 1) - offset;
                        break;
                    case 3:
                        childIndex = (len - 1) - offset;
                        break;
                    case 5:
                        childIndex = (len - 1) - offset;
                        break;
                    case 7:
                        childIndex = offset;
                        break;
                    case 9:
                        childIndex = offset;
                        break;
                    case 11:
                        childIndex = offset;
                        break;
                    default:
                        childIndex = offset;
                }

                if (childIndex >= 0 && childIndex < pane.getChildren().size()) {
                    Node n = pane.getChildren().get(childIndex);
                    if (n instanceof Button) {
                        Button b = (Button) n;
                        if (m != null) {
                            placeMarble(m, b);
                        } else {
                            ImageView iv = new ImageView(emptyImage);
                            iv.setFitWidth(25);
                            iv.setFitHeight(25);
                            b.setGraphic(iv);
                            b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            b.setPadding(Insets.EMPTY);
                            b.setUserData(null);
                        }
                    }
                }
            }

            start += len;
        }

        for (Pane pane : safeZones) {
            clearPane(pane);
        }
        
        List<engine.board.SafeZone> sz = board.getSafeZones();
        for (int i = 0; i < sz.size() && i < safeZones.size(); i++) {
            Pane pane = safeZones.get(i);
            int cellIdx = 0;
            
            for (Cell c : sz.get(i).getCells()) {
                if (cellIdx < pane.getChildren().size()) {
                    Node n = pane.getChildren().get(cellIdx);
                    if (n instanceof Button) {
                        Button b = (Button) n;
                        Marble m = c.getMarble();
                        
                        if (m != null) {
                            placeMarble(m, b);
                        } else {
                            ImageView iv = new ImageView(emptyImage);
                            iv.setFitWidth(25);
                            iv.setFitHeight(25);
                            b.setGraphic(iv);
                            b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            b.setPadding(Insets.EMPTY);
                            b.setUserData(null);
                        }
                    }
                    cellIdx++;
                }
            }
        }

        for (HBox box : homeZoneRows) {
            box.getChildren().clear();
        }
        
        for (int pi = 0; pi < 4 && pi < game.getPlayers().size(); pi++) {
            List<Marble> homeMarbles = game.getPlayers().get(pi).getMarbles();
            for (int row = 0; row < 2; row++) {
                HBox box = homeZoneRows.get(pi*2 + row);
                for (int col = 0; col < 2; col++) {
                    int marbleIndex = row*2 + col;
                    if (marbleIndex < homeMarbles.size()) {
                        Marble m = homeMarbles.get(marbleIndex);
                        String colorName = m.getColour().name().toLowerCase();
                        Image img = new Image(
                            getClass().getResourceAsStream("/application/" + colorName + ".png")
                        );
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(25);
                        iv.setFitHeight(25);
                        Button marbleBtn = new Button();
                        marbleBtn.setGraphic(iv);
                        marbleBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        marbleBtn.setPadding(Insets.EMPTY);
                        marbleBtn.setPrefSize(25, 25);
                        marbleBtn.setUserData(m);
                        marbleBtn.setOnAction(this::handleMarbleClick);
                        box.getChildren().add(marbleBtn);
                    }
                }
            }
        }
    }

    private void clearPane(Pane pane) {
        for (Node n : pane.getChildren()) {
            if (n instanceof Button) {
                Button b = (Button) n;
                ImageView iv = new ImageView(emptyImage);
                iv.setFitWidth(25);
                iv.setFitHeight(25);
                b.setGraphic(iv);
                b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                b.setPadding(Insets.EMPTY);
                b.setPrefSize(25, 25);
                b.setMinSize(25, 25);
                b.setMaxSize(25, 25);
                b.setUserData(null);
                b.setOnAction(null);
            }
        }
    }


    private void placeMarble(Marble m, Button b) {
        String clr = m.getColour().name().toLowerCase();
        Image img = new Image(
            getClass().getResourceAsStream("/application/" + clr + ".png")
        );
        ImageView iv = new ImageView(img);
        iv.setFitWidth(25);
        iv.setFitHeight(25);
        b.setGraphic(iv);
        b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        b.setPadding(Insets.EMPTY);
        b.setPrefSize(25, 25);
        b.setMinSize(25, 25);
        b.setMaxSize(25, 25);
        b.setUserData(m);
        b.setOnAction(this::handleMarbleClick);
        b.setStyle("");
    }

    private void handleMarbleClick(ActionEvent e) {
        if (getCurrentPlayerIndex() != 0) {
            showWarning("It's not your turn.");
            return;
        }
        Button b = (Button)e.getSource();
        Marble m = (Marble)b.getUserData();
        try {
            game.selectMarble(m);
            refreshGameUI();
        } catch (InvalidMarbleException ex) {
            showWarning(ex.getMessage());
        }
    }

    @FXML public void Deselect(ActionEvent e) {
        game.deselectAll();
        refreshGameUI();
    }

    public void showWinner(String msg) {
        warn.setText(msg);
        warn.setVisible(true);
    }
    public void showWarning(String msg) {
        warn.setText(msg);
        warn.setVisible(true);
        warn.toFront();
        PauseTransition pt = new PauseTransition(Duration.seconds(3));
        pt.setOnFinished(a -> warn.setVisible(false));
        pt.play();
    }
    /** Find active player index by colour. */
    private int getCurrentPlayerIndex() {
        Colour active = game.getActivePlayerColour();
        List<Player> pls = game.getPlayers();
        for (int i = 0; i < pls.size(); i++) {
            if (pls.get(i).getColour() == active) {
                return i;
            }
        }
        return -1;
    }

    /** CPU auto-turn with discard logic. */
    private void cpuTakeTurn() {
        try {
            int playerIndex = getCurrentPlayerIndex();
            int marblesBefore = game.getPlayers().get(playerIndex).getMarbles().size();
            
            if (!game.canPlayTurn()) {
                game.discardCard();
            } else {
                game.playPlayerTurn();
                int marblesAfter = game.getPlayers().get(playerIndex).getMarbles().size();
                if (marblesAfter > marblesBefore) {
                    Colour playerColour = game.getPlayers().get(playerIndex).getColour();
                    showWarning(playerColour + " marble hit a trap and was sent back to the home zone!");
                }
            }
        } catch (GameException ex) {
            // optionally log
        } finally {
            game.endPlayerTurn();
            refreshGameUI();
        }
    }
    
    /** EndTurn button: human only, with discard logic. */
    @FXML
    public void EndTurn(ActionEvent e) {
        // Only the human may use this button
        if (getCurrentPlayerIndex() != 0) {
            return;
        }

        Player human = game.getPlayers().get(0);
        Card selected = human.getSelectedCard();

        if (human.getHand().isEmpty()) {
            game.endPlayerTurn();
            refreshGameUI();
            return;
        }

        if (selected == null) {
            showWarning("Please select a card to discard before ending your turn.");
            return;
        }

        try {
            human.getHand().remove(selected);
            game.getFirePit().add(selected);
            human.deselectAll();
            FirePitlabel.setText(selected.getName());
        } catch (Exception ex) {
            showWarning("Error discarding card: " + ex.getMessage());
            return;
        }

        game.endPlayerTurn();
        refreshGameUI();
    }

    private Image getCardImage(Card card) {
        if (card instanceof Standard) {
            Standard stdCard = (Standard) card;
            int rankValue = stdCard.getRank();
            String rank;
            
            // Convert numeric rank to string name
            switch (rankValue) {
                case 1:
                    rank = "ace";
                    break;
                case 2:
                    rank = "two";
                    break;
                case 3:
                    rank = "three";
                    break;
                case 4:
                    rank = "four";
                    break;
                case 5:
                    rank = "five";
                    break;
                case 6:
                    rank = "six";
                    break;
                case 7:
                    rank = "seven";
                    break;
                case 8:
                    rank = "eight";
                    break;
                case 9:
                    rank = "nine";
                    break;
                case 10:
                    rank = "ten";
                    break;
                case 11:
                    rank = "jack";
                    break;
                case 12:
                    rank = "queen";
                    break;
                case 13:
                    rank = "king";
                    break;
                default:
                    rank = String.valueOf(rankValue);
            }
            
            String suit = "";
            switch (stdCard.getSuit()) {
                case CLUB:
                    suit = "g";
                    break;
                case SPADE:
                    suit = "p";
                    break;
                case DIAMOND:
                    suit = "b";
                    break;
                case HEART:
                    suit = "r";
                    break;
            }
            
            String filename = rank + "." + suit + ".png";
            try {
                return new Image(getClass().getResourceAsStream("/application/" + filename));
            } catch (Exception e) {
                System.err.println("Could not load card image: " + filename);
                return new Image(getClass().getResourceAsStream("/application/Covered deck.jpg"));
            }
        }
        else if(card.getName().equals("MarbleBurner"))
        	return new Image(getClass().getResourceAsStream("/application/Burnercard.jpg"));
        else if(card.getName().equals("MarbleSaver"))
        	return new Image(getClass().getResourceAsStream("/application/SaverCard.jpg"));
        else {
            return new Image(getClass().getResourceAsStream("/application/Covered deck.jpg"));
        }
    }
    
    private void updateCPUCardVisuals() {
        updateCPUCardBox(1, game.getPlayers().get(1).getHand().size());
        updateCPUCardBox(2, game.getPlayers().get(2).getHand().size());
        updateCPUCardBox(3, game.getPlayers().get(3).getHand().size());
    }

    private void updateCPUCardBox(int cpuIndex, int numCards) {
        Pane cardContainer = null;
        boolean isHorizontal = false;
        
        switch(cpuIndex) {
            case 1:
                cardContainer = CPU1Cards;
                isHorizontal = false; // VBox is vertical
                break;
            case 2:
                cardContainer = CPU2Cards;
                isHorizontal = true;  // HBox is horizontal
                break;
            case 3:
                cardContainer = CPU3Cards;
                isHorizontal = false; // VBox is vertical
                break;
        }
        
        if (cardContainer == null) return;
        
        cardContainer.getChildren().clear();
        
        // The covered card image for CPU players
        Image coveredCardImage = new Image(
            getClass().getResourceAsStream("/application/Covered deck.jpg")
        );
        
        // Add visual representation for each card
        for (int i = 0; i < numCards; i++) {
            ImageView cardView = new ImageView(coveredCardImage);
            cardView.setFitWidth(150);
            cardView.setFitHeight(250);
            cardView.setPreserveRatio(true);
            
            // Set margins differently based on container type
            if (isHorizontal) {
                // For HBox: overlap cards horizontally
                HBox.setMargin(cardView, new Insets(0, -30, 0, 0));
            } else {
                // For VBox: overlap cards vertically
                VBox.setMargin(cardView, new Insets(0, 0, -40, 0));
            }
            
            cardContainer.getChildren().add(cardView);
        }
        
        // If there's at least one card, fix the margin of the last card
        if (!cardContainer.getChildren().isEmpty()) {
            if (isHorizontal) {
                HBox.setMargin(cardContainer.getChildren().get(cardContainer.getChildren().size()-1), 
                              new Insets(0, 0, 0, 0));
            } else {
                VBox.setMargin(cardContainer.getChildren().get(cardContainer.getChildren().size()-1), 
                              new Insets(0, 0, 0, 0));
            }
        }
    }
    
    
    
}