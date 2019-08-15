package edu.caltech.seva.models;

import java.util.Date;

public class LambdaTriggerInfo {
    private String username;
    private Date timestamp;
    private String toiletIP;
    private String errorCode;

    public LambdaTriggerInfo() {}

    public LambdaTriggerInfo(String username, Date timestamp, String toiletIP, String errorCode) {
        this.username = username;
        this.timestamp = timestamp;
        this.toiletIP = toiletIP;
        this.errorCode = errorCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getToiletIP() {
        return toiletIP;
    }

    public void setToiletIP(String toiletIP) {
        this.toiletIP = toiletIP;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
