package engine.board;

import java.util.ArrayList;

import engine.GameManager;
import exception.CannotFieldException;
import exception.IllegalDestroyException;
import exception.IllegalMovementException;
import exception.IllegalSwapException;
import exception.InvalidMarbleException;
import model.Colour;
import model.player.Marble;

@SuppressWarnings("unused")
public class Board implements BoardManager {
    private final ArrayList<Cell> track;
    private final ArrayList<SafeZone> safeZones;
	private final GameManager gameManager;
    private int splitDistance;

    public Board(ArrayList<Colour> colourOrder, GameManager gameManager) {
        this.track = new ArrayList<>();
        this.safeZones = new ArrayList<>();
        this.gameManager = gameManager;
        
        for (int i = 0; i < 100; i++) {
            this.track.add(new Cell(CellType.NORMAL));
            
            if (i % 25 == 0) 
                this.track.get(i).setCellType(CellType.BASE);
            
            else if ((i+2) % 25 == 0) 
                this.track.get(i).setCellType(CellType.ENTRY);
        }

        for(int i = 0; i < 8; i++)
            this.assignTrapCell();

        for (int i = 0; i < 4; i++)
            this.safeZones.add(new SafeZone(colourOrder.get(i)));

        splitDistance = 3;
    }

    public ArrayList<Cell> getTrack() {
        return this.track;
    }

    public ArrayList<SafeZone> getSafeZones() {
        return this.safeZones;
    }

    public void setSplitDistance(int splitDistance) {
        this.splitDistance = splitDistance;
    }
   
    private void assignTrapCell() {
        int randIndex = -1;
        
        do
            randIndex = (int)(Math.random() * 100); 
        while(this.track.get(randIndex).getCellType() != CellType.NORMAL || this.track.get(randIndex).isTrap());
        
        this.track.get(randIndex).setTrap(true);
    }
    
    private ArrayList<Cell> getSafeZone(Colour colour) {
        for (int i = 0; i < 4; i++) 
            if (this.safeZones.get(i).getColour() == colour) 
                return this.safeZones.get(i).getCells();
        
        return null;
    }

    private int getPositionInPath(ArrayList<Cell> path, Marble marble) {
        for(int i = 0; i < path.size(); i++) {
            if(path.get(i).getMarble() == marble) 
                return i;
        }
        
        return -1;
    }

    private int getBasePosition(Colour colour) {
        for(int i = 0; i < safeZones.size(); i++) {
            if(safeZones.get(i).getColour() == colour)
                return i * 25;
        }
        
        return -1;
    }

    private int getEntryPosition(Colour colour) {
        int idx = getBasePosition(colour);
        
        if(idx == -1)
            return -1;
        
        else
            return (idx - 2 + 100) % 100;
    }
    
    private ArrayList<Cell> validateSteps(Marble marble, int steps) throws IllegalMovementException {
    	Colour ownerColour = gameManager.getActivePlayerColour();
        ArrayList<Cell> safeZone = getSafeZone(marble.getColour());
        int entryPosition = getEntryPosition(ownerColour);

        int positionOnTrack = getPositionInPath(track, marble);
        int positionInSafeZone = getPositionInPath(safeZone, marble);

        ArrayList<Cell> fullPath = new ArrayList<>();
        
        if (positionOnTrack == -1 && positionInSafeZone == -1)
            throw new IllegalMovementException("Cannot move a marble that is not on track nor Safe Zone");
        
        if (positionOnTrack != -1) {
            int distanceToEntry = entryPosition - positionOnTrack;

            if (marble.getColour() == ownerColour && steps > 0 && distanceToEntry >= 0 && steps > distanceToEntry + 4)
                throw new IllegalMovementException("Rank is too high!");
            
            if (marble.getColour() == ownerColour && steps > 0 && distanceToEntry >= 0 && steps > distanceToEntry) {
                fullPath.addAll(track.subList(positionOnTrack, entryPosition + 1));
                fullPath.addAll(safeZone.subList(0, steps - distanceToEntry));
            }
            
            else {
                int target = (positionOnTrack + steps + 100) % 100;

                if (steps > 0) {
                	int current = positionOnTrack;
                    while (true) {
                        fullPath.add(track.get(current));
                        if (current == target) {
                            break;
                        }
                        current = (current + 1 + 100) % 100; 
                    }
                }
                
                else {
                	int current = positionOnTrack;
                    while (true) {
                        fullPath.add(track.get(current));
                        if (current == target) {
                            break;
                        }
                        current = (current - 1 + 100) % 100; 
                    }
                        
                }
            }
        }
        
        if (positionInSafeZone != -1) {
            if (steps < 0) 
                throw new IllegalMovementException("Cannot move a marble backwards in Safe Zone");

            int distanceLeft = 3 - positionInSafeZone; 

            if (steps > distanceLeft)
                throw new IllegalMovementException("Rank is too high!");

            fullPath.addAll(safeZone.subList(positionInSafeZone, positionInSafeZone + steps + 1));
        }

        
        return fullPath;
        
    }

    private void validatePath(Marble marble, ArrayList<Cell> fullPath, boolean destroy) throws IllegalMovementException{
        Colour ownerColour = gameManager.getActivePlayerColour();
        
        int marbleCount = 0;
        
        for(int i = 1; i < fullPath.size(); i++) {
            Cell cell = fullPath.get(i);
            if (cell.getMarble() != null) {
            	if (i != fullPath.size() - 1) //counting marbles in my path excluding target
            		marbleCount++;

                if (cell.getCellType() == CellType.SAFE)
                    throw new IllegalMovementException("Cannot bypass my Safe Zone marbles!");
                
                if (cell.getCellType() == CellType.BASE && track.indexOf(cell) == getBasePosition(cell.getMarble().getColour()))
                    throw new IllegalMovementException("Cannot bypass or land on marbles in their Base Cell!");

                if (!destroy) {
                	//not marble colour as even with moving opponent marble I cannot kill my own marbles not his
                    if (ownerColour == cell.getMarble().getColour())
                        throw new IllegalMovementException("Cannot bypass or land on my own marble!");
                    
                    //going into safe zone with a marble in the entry
                    if (cell.getCellType() == CellType.ENTRY && (i+1) < fullPath.size() && fullPath.get(i+1).getCellType() == CellType.SAFE)
                        throw new IllegalMovementException("Cannot bypass a marble blocking my Safe Zone!");

                    if (marbleCount > 1)
                        throw new IllegalMovementException("Cannot bypass more than 1 marble in my path!");
                }
                
            }
        }

    }
    
    private void move(Marble marble, ArrayList<Cell> fullPath, boolean destroy) throws IllegalDestroyException {
    	Cell currentCell = fullPath.get(0);
    	Cell targetCell = fullPath.get(fullPath.size()-1);
    	
    	currentCell.setMarble(null);

        if (destroy) {
            for(Cell cell : fullPath) {
                if (cell.getMarble() != null) 
                    destroyMarble(cell.getMarble());
            }
        }
        
        else if (targetCell.getMarble() != null) 
        	destroyMarble(targetCell.getMarble());   
        
        targetCell.setMarble(marble);

        if(targetCell.isTrap()) {
            destroyMarble(marble);
            targetCell.setTrap(false);
            assignTrapCell();
        }
        
	}
    
    private void validateSwap(Marble marble_1, Marble marble_2) throws IllegalSwapException {
    	Colour ownerColour = gameManager.getActivePlayerColour();

        int trackPosition_1 = getPositionInPath(track, marble_1);
        int trackPosition_2 = getPositionInPath(track, marble_2);

        if (trackPosition_1 == -1 || trackPosition_2 == -1) 
            throw new IllegalSwapException("Cannot swap marbles that are not on track.");
        
        Cell cell_1 = track.get(trackPosition_1);
        Cell cell_2 = track.get(trackPosition_2);

        if(ownerColour != marble_1.getColour() && cell_1.getCellType() == CellType.BASE && trackPosition_1 == getBasePosition(cell_1.getMarble().getColour())
        || ownerColour != marble_2.getColour() && cell_2.getCellType() == CellType.BASE && trackPosition_2 == getBasePosition(cell_2.getMarble().getColour()))
            throw new IllegalSwapException("Marbles that are in their Base Cell can not be selected as a swap target.");
    }
    
    private void validateDestroy(int positionInPath) throws IllegalDestroyException {
    	if (positionInPath == -1) 
            throw new IllegalDestroyException("Cannot burn marbles that aren't on track.");
    
        Cell current = track.get(positionInPath);
        
        if(current.getCellType() == CellType.BASE && current.getMarble() != null && positionInPath == getBasePosition(current.getMarble().getColour()))
            throw new IllegalDestroyException("Cannot burn marbles that are safe in their Base Cell.");
		
	}
    
    private void validateFielding(Cell occupiedBaseCell) throws CannotFieldException {
    	if (occupiedBaseCell.getMarble().getColour() == gameManager.getActivePlayerColour())
            throw new CannotFieldException("One of your marbles is already on your Base Cell");
	}
    
    private void validateSaving(int positionInSafeZone, int positionOnTrack) throws InvalidMarbleException {
    	if(positionInSafeZone != -1)
            throw new InvalidMarbleException("Cannot save marbles that are already in the Safe Zone.");
        
        if(positionOnTrack == -1)
            throw new InvalidMarbleException("Cannot save marbles that aren't on track.");
    }

    @Override
    public int getSplitDistance() {
        return this.splitDistance;
    }

    @Override
    public void moveBy(Marble marble, int steps, boolean destroy) throws IllegalMovementException, IllegalDestroyException{
    	ArrayList<Cell> fullPath = validateSteps(marble, steps);
    	
        validatePath(marble, fullPath, destroy);
        
        move(marble, fullPath, destroy);
    }

	@Override
    public void swap(Marble marble_1, Marble marble_2) throws IllegalSwapException {
        validateSwap(marble_1, marble_2);
        
        int trackPosition_1 = getPositionInPath(track, marble_1);
        int trackPosition_2 = getPositionInPath(track, marble_2);

        track.get(trackPosition_1).setMarble(marble_2);
        track.get(trackPosition_2).setMarble(marble_1);
    }

    @Override
    public void destroyMarble(Marble marble) throws IllegalDestroyException {
    	Colour ownerColour = gameManager.getActivePlayerColour();
        int positionOnTrack = getPositionInPath(track, marble); 
        
        if (marble.getColour() != ownerColour)
        	validateDestroy(positionOnTrack);

        this.track.get(positionOnTrack).setMarble(null);
        this.gameManager.sendHome(marble);
    }

    @Override
	public void sendToBase(Marble marble) throws CannotFieldException, IllegalDestroyException {
    	int basePosition = getBasePosition(marble.getColour());
    	Cell baseCell = this.track.get(basePosition);
    	
    	if(baseCell.getMarble() != null) {
    		validateFielding(baseCell);
            destroyMarble(baseCell.getMarble());
    	}
    	
    	baseCell.setMarble(marble);
	}

	@Override
	public void sendToSafe(Marble marble) throws InvalidMarbleException {
		int positionOnTrack = getPositionInPath(track, marble);
        int positionInSafeZone = getPositionInPath(getSafeZone(marble.getColour()), marble);

        validateSaving(positionInSafeZone, positionOnTrack);
    
        ArrayList<Cell> safeZone = getSafeZone(marble.getColour());
        ArrayList<Cell> freeSpaces = new ArrayList<>();

        for(Cell cell : safeZone){
            if(cell.getMarble() == null)
                freeSpaces.add(cell);
        }

        int randIndex = (int)(Math.random() * freeSpaces.size());
        freeSpaces.get(randIndex).setMarble(marble);
        this.track.get(positionOnTrack).setMarble(null);
	}
    
    @Override
    public ArrayList<Marble> getActionableMarbles() {
        ArrayList<Marble> marbles = new ArrayList<>();
        Colour colour = gameManager.getActivePlayerColour();
        ArrayList<Cell> safeZone = getSafeZone(colour);

        for(Cell cell : safeZone) {
            if(cell.getMarble() != null)
                marbles.add(cell.getMarble());
        }

        for(Cell cell : track) {
            if(cell.getMarble() != null)
                marbles.add(cell.getMarble());
        }

        return marbles;
    }	
    
}
