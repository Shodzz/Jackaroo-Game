package model.player;

import model.Colour;

public class Marble {

    private final Colour colour;
    
    public Marble(Colour colour) {
        this.colour = colour;
    }

    public Colour getColour() {
        return this.colour;
    }
}
