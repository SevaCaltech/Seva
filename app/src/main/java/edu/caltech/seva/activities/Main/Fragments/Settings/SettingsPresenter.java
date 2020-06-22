package edu.caltech.seva.activities.Main.Fragments.Settings;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;

import java.util.HashMap;
import java.util.Map;

import edu.caltech.seva.helpers.PrefManager;

/**
 * Represents the Presenter for the Settings Fragment. Used to provide logic and data objects to
 * the view.
 */
public class SettingsPresenter implements SettingsContract.Presenter {

    private PrefManager prefManager;
    private AmazonDynamoDBClient dynamoDBClient;

    /**
     * Constructor for the Presenter which takes a preferences to give access.
     *
     * @param prefManager The sharedPreferences manager, used to access user data
     */
    public SettingsPresenter(PrefManager prefManager) {
        this.prefManager = prefManager;
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    }

    @Override
    public void handleNotificationSettings(SettingsChoice choice, boolean isChecked) {
        switch (choice) {
            case SMS:
                prefManager.setSendSms(isChecked);
                break;
            case PUSH:
                prefManager.setSendPush(isChecked);
                break;
            default:
                throw new RuntimeException("not a valid settings choice");
        }
        UpdateSettings updateSettings = new UpdateSettings(prefManager, dynamoDBClient);
        updateSettings.execute();
    }



    private static class UpdateSettings extends AsyncTask<Void, Void, String> {

        private final PrefManager prefManager;
        private final AmazonDynamoDBClient dynamoDBClient;

        UpdateSettings(PrefManager prefManager, AmazonDynamoDBClient dynamoDBClient) {
            this.prefManager = prefManager;
            this.dynamoDBClient = dynamoDBClient;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return buildUpdateItemRequest();
        }

        private String buildUpdateItemRequest() {
            HashMap<String, AttributeValue> key = new HashMap<>();
            key.put("uid", new AttributeValue().withS(prefManager.getUid()));

            //create userSettings Map
            Map<String, AttributeValue> userSettings = new HashMap<>();
            userSettings.put("sendSMS", new AttributeValue().withBOOL(prefManager.getSendSms()));
            userSettings.put("sendPush", new AttributeValue().withBOOL(prefManager.getSendPush()));

            Map<String, AttributeValue> updateAttributeValues = new HashMap<>();
            updateAttributeValues.put(":val1", new AttributeValue().withM(userSettings));

            //create the put item request
            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                    .withTableName("SevaOperators")
                    .withKey(key)
                    .withUpdateExpression("set userSettings = :val1")
                    .withExpressionAttributeValues(updateAttributeValues);

            UpdateItemResult result = dynamoDBClient.updateItem(updateItemRequest);
            Log.d("updateItemUserSettings", result.toString());
            return result.toString();
        }
    }
}
