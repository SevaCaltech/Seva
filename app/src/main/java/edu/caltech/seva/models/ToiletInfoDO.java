package edu.caltech.seva.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "Toilets_Information")

public class ToiletInfoDO {
    private String _deviceId;
    private String _timestamp;
    private String _Toilet_Status;

    @DynamoDBHashKey(attributeName = "deviceId")
    @DynamoDBIndexHashKey(attributeName = "deviceId", globalSecondaryIndexName = "deviceId")
    public String getDeviceId() {return _deviceId;}
    public void setDeviceId(final String _deviceId) {
        this._deviceId = _deviceId;
    }

    @DynamoDBAttribute(attributeName = "NewUpdate")
    public String getUpdateTimestamp() {return  _timestamp;}
    public void setUpdateTimestamp(final String _timestamp) {
        this._timestamp = _timestamp;
    }

    @DynamoDBAttribute(attributeName = "Toilet_Status")
    public String getToiletStatus() {return _Toilet_Status;}
    public void setToiletStatus(final String _Toilet_Status) {this._Toilet_Status = _Toilet_Status;}

}
