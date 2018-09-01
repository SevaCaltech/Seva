package edu.caltech.seva.models;

public class Toilet {
    private String[] coords;
    private String description;

    public Toilet(String[] coords, String description){
        this.coords = coords;
        this.description = description;
    }

    public String[] getCoords() {
        return coords;
    }

    public void setCoords(String[] coords) {
        this.coords = coords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
