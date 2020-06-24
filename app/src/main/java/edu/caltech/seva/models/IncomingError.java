package edu.caltech.seva.models;

public class IncomingError {
    private int id;
    private String errorCode;
    private String repairCode;
    private String toiletIP;
    private String toiletName;
    private String date;
    private String repairTitle;
    private String toolInfo;
    private String totalTime;
    private int totalSteps;
    private String lat;
    private String lng;
    private String description;

       public IncomingError(int id, String errorCode, String toiletIP, String date, String repairCode, String toiletName, String repairTitle, String toolInfo, String totalTime, int totalSteps, String lat, String lng, String description) {
        this.setId(id);
        this.setErrorCode(errorCode);
        this.setToiletIP(toiletIP);
        this.setDate(date);
        this.setRepairCode(repairCode);
        this.setRepairTitle(repairTitle);
        this.setToiletName(toiletName);
        this.setToolInfo(toolInfo);
        this.setTotalTime(totalTime);
        this.setTotalSteps(totalSteps);
        this.setLat(lat);
        this.setLng(lng);
        this.setDescription(description);

    }

    public IncomingError(IncomingError e) {
           this.id = e.id;
           this.errorCode = e.errorCode;
           this.toiletIP = e.toiletIP;
           this.date = e.date;
           this.repairCode = e.repairCode;
           this.repairTitle = e.repairTitle;
           this.toiletName = e.toiletName;
           this.toolInfo = e.toolInfo;
           this.totalTime = e.totalTime;
           this.totalSteps = e.totalSteps;
           this.lat = e.lat;
           this.lng = e.lng;
           this.description = e.description;
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

    public String getToiletIP() {
        return toiletIP;
    }

    public void setToiletIP(String toiletIP) {
        this.toiletIP = toiletIP;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getRepairCode() { return repairCode;}

    public void setRepairCode(String repairCode) { this.repairCode = repairCode;}

    public String getToiletName() {return toiletName;}

    public void setToiletName(String toiletName) { this.toiletName = toiletName;}

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
