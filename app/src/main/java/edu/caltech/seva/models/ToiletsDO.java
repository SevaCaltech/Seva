package edu.caltech.seva.models;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIgnore;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMarshalling;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "SevaToilets")

public class ToiletsDO {
    private String _deviceId;
    private String _timestamp;
    private Map<String,String> _data;

    public ToiletsDO(){}

    public ToiletsDO(String _deviceId){
        this();
        this._deviceId = _deviceId;
    }

    @DynamoDBHashKey(attributeName = "deviceId")
    @DynamoDBIndexHashKey(attributeName = "deviceId", globalSecondaryIndexName = "deleteError")
    public String getDeviceId() {
        return _deviceId;
    }

    public void setDeviceId(final String _deviceId) {
        this._deviceId = _deviceId;
    }

    @DynamoDBRangeKey(attributeName = "timestamp")
    @DynamoDBAttribute(attributeName = "timestamp")
    public String getTimestamp() {
        return _timestamp;
    }

    public void setTimestamp(final String _timestamp) {
        this._timestamp = _timestamp;
    }

    @DynamoDBIndexRangeKey(attributeName = "data", globalSecondaryIndexName = "deleteError")
    public Map<String,String> getData() {
        return _data;
    }

    public void setData(Map<String,String> _data) {
        this._data = _data;
    }

}
