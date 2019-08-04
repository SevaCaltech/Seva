package edu.caltech.seva.models;

public class Toilet {
    private String[] coords;
    private String description;
    private String toiletName;

    public Toilet(String[] coords, String description, String toiletName){
        this.coords = coords;
        this.description = description;
        this.toiletName = toiletName;
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

    public String getToiletName() {return  toiletName;}

    public void setToiletName(String toiletName) { this.toiletName = toiletName;}
}
