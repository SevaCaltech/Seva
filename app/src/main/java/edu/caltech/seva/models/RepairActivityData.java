package edu.caltech.seva.models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class RepairActivityData implements Serializable {
    private String errorCode;
    private String repairCode;
    private String repairTitle;
    private String toolInfo;
    private int totalSteps;
    private String toiletIP;
    private String totalTime;
    private String timestamp;
    private String lat;
    private String lng;

    public RepairActivityData() {}

    public RepairActivityData(String errorCode, String repairCode, String repairTitle, String totalTime, String toolInfo, int totalSteps, String toiletIP, String timestamp, String lat, String lng) {
        this.errorCode = errorCode;
        this.repairCode = repairCode;
        this.repairTitle = repairTitle;
        this.toolInfo = toolInfo;
        this.totalTime = totalTime;
        this.totalSteps = totalSteps;
        this.toiletIP = toiletIP;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lng = lng;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getRepairCode() {
        return repairCode;
    }

    public String getRepairTitle() {
        return repairTitle;
    }

    public String getToolInfo() {
        return toolInfo;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public String getToiletIP() {
        return toiletIP;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() { return lng; }

    public String getTotalTime() { return totalTime; }
}
