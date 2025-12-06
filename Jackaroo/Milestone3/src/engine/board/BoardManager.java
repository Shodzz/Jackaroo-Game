package engine.board;

import java.util.ArrayList;

import exception.CannotFieldException;
import exception.IllegalDestroyException;
import exception.IllegalMovementException;
import exception.IllegalSwapException;
import exception.InvalidMarbleException;
import model.player.Marble;

public interface BoardManager { 
	public int getSplitDistance();
    public void moveBy(Marble marble, int steps, boolean destroy) throws IllegalMovementException, IllegalDestroyException;
    public void swap(Marble marble_1, Marble marble_2) throws IllegalSwapException;
    public void destroyMarble(Marble marble) throws IllegalDestroyException;
    public void sendToBase(Marble marble) throws CannotFieldException, IllegalDestroyException;
    public void sendToSafe(Marble marble) throws InvalidMarbleException;
    public ArrayList<Marble> getActionableMarbles();
}
