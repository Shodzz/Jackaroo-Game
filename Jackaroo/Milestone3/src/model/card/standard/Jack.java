package model.card.standard;

import java.util.ArrayList;

import engine.GameManager;
import engine.board.BoardManager;
import exception.ActionException;
import exception.InvalidMarbleException;
import model.Colour;
import model.player.Marble;

public class Jack extends Standard {

    public Jack(String name, String description, Suit suit, BoardManager boardManager, GameManager gameManager) {
        super(name, description, 11, suit, boardManager, gameManager);
    }
    
    @Override
    public boolean validateMarbleSize(ArrayList<Marble> marbles) {
        return marbles.size() == 2 || super.validateMarbleSize(marbles);
    }

    @Override
    public boolean validateMarbleColours(ArrayList<Marble> marbles) {
    	if(marbles.size() == 2) {
    		Colour myColour = gameManager.getActivePlayerColour();
    		return marbles.get(0).getColour().equals(myColour) != marbles.get(1).getColour().equals(myColour); 		
    	}
    	
    	else
    		return super.validateMarbleColours(marbles);
    }

    @Override
    public void act(ArrayList<Marble> marbles) throws ActionException, InvalidMarbleException {
        if(marbles.size() == 2)
            boardManager.swap(marbles.get(0), marbles.get(1));
        
        else
            super.act(marbles);
    }

}
