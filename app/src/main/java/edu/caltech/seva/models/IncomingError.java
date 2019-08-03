package edu.caltech.seva.models;

public class IncomingError {
    private int id;
    private String errorCode;
    private String repairCode;
    private String toiletId;
    private String date;
    private String repairTitle;
    private String toolInfo;
    private String totalTime;
    private int totalSteps;
    private String lat;
    private String lng;
    private String description;

       public IncomingError(int id, String errorCode, String toiletId, String date, String repairCode, String repairTitle, String toolInfo, String totalTime, int totalSteps, String lat, String lng, String description) {
        this.setId(id);
        this.setErrorCode(errorCode);
        this.setToiletId(toiletId);
        this.setDate(date);
        this.setRepairCode(repairCode);
        this.setRepairTitle(repairTitle);
        this.setToolInfo(toolInfo);
        this.setTotalTime(totalTime);
        this.setTotalSteps(totalSteps);
        this.setLat(lat);
        this.setLng(lng);
        this.setDescription(description);

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getToiletId() {
        return toiletId;
    }

    public void setToiletId(String toiletId) {
        this.toiletId = toiletId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getRepairCode() { return repairCode;}

    public void setRepairCode(String repairCode) { this.repairCode = repairCode;}

    public String getRepairTitle() {
        return repairTitle;
    }

    public void setRepairTitle(String repairTitle) {
        this.repairTitle = repairTitle;
    }

    public String getToolInfo() {
        return toolInfo;
    }

    public void setToolInfo(String toolInfo) {
        this.toolInfo = toolInfo;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
