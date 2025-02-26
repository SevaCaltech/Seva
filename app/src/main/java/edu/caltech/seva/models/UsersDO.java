package edu.caltech.seva.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "SevaOperators")

public class UsersDO {
    private String _uid;
    private String _displayName;
    private String _phone;
    private String _email;
    private String _deviceToken;
    private Map<String, Boolean> _userSettings;
    private List<String> _toilets;

    @DynamoDBHashKey(attributeName = "uid")
    @DynamoDBIndexHashKey(attributeName = "uid", globalSecondaryIndexName = "uid")
    public String getUserId() {
        return _uid;
    }

    public void setUserId(final String _uid) {
        this._uid = _uid;
    }

    @DynamoDBAttribute(attributeName = "displayName")
    public String getDisplayName() {
        return _displayName;
    }

    public void setDisplayName(final String _displayName) {
        this._displayName = _displayName;
    }

    @DynamoDBAttribute(attributeName = "toilets")
    public List<String> getToilets() {
        return _toilets;
    }

    public void setToilets(final List<String> _toilets) {
        this._toilets = _toilets;
    }

    @DynamoDBAttribute(attributeName = "phone")
    public String getPhone() {
        return _phone;
    }

    public void setPhone(final String _phone) {
        this._phone = _phone;
    }

    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return _email;
    }

    public void setEmail(final String _email) {
        this._email = _email;
    }

    @DynamoDBAttribute(attributeName = "deviceToken")
    public String getDeviceToken() { return _deviceToken; }

    public void setDeviceToken(final String _deviceToken) { this._deviceToken = _deviceToken; }

    @DynamoDBAttribute(attributeName = "userSettings")
    public Map<String, Boolean> getUserSettings() { return this._userSettings; }

    public void setUserSettings(Map<String, Boolean> _userSettings) { this._userSettings = _userSettings; }
}
