package edu.caltech.seva.models;

import java.util.Date;

public class Toilet {
    private String[] coords;
    private String description;
    private String toiletName;
    private String toiletIp;
    private Date syncTimestamp;
    private Toilet_Status status;


    public Toilet(String[] coords, String description, String toiletName, String toiletIp){
        this.coords = coords;
        this.description = description;
        this.toiletName = toiletName;
        this.toiletIp = toiletIp;
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

    public Date getSyncTimestamp() {
        return syncTimestamp;
    }

    public void setSyncTimestamp(Date syncTimestamp) {
        this.syncTimestamp = syncTimestamp;
    }

    public Toilet_Status getStatus() {
        return status;
    }

    public void setStatus(Toilet_Status status) {
        this.status = status;
    }

    public String getToiletIp() {
        return toiletIp;
    }

    public void setToiletIp(String toiletIp) {
        this.toiletIp = toiletIp;
    }
}
