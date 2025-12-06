package model.card.standard;

import java.util.ArrayList;

import engine.GameManager;
import engine.board.BoardManager;
import exception.ActionException;
import exception.InvalidMarbleException;
import model.player.Marble;

public class Ten extends Standard {

    public Ten(String name, String description, Suit suit, BoardManager boardManager, GameManager gameManager) {
        super(name, description, 10, suit, boardManager, gameManager);
    }

    @Override
    public boolean validateMarbleSize(ArrayList<Marble> marbles) {
        return marbles.isEmpty() || super.validateMarbleSize(marbles);
    }

    @Override
    public void act(ArrayList<Marble> marbles) throws ActionException, InvalidMarbleException {
        if(marbles.isEmpty())
            gameManager.discardCard(gameManager.getNextPlayerColour());
        
        else
            super.act(marbles);
    }

}
