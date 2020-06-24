package edu.caltech.seva.activities.Main.Fragments.Notifications;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.caltech.seva.activities.MainApplication;
import edu.caltech.seva.activities.Repair.RepairActivity;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.IncomingError;
import edu.caltech.seva.models.ToiletsDO;

/**
 * Represents the Presenter for the Notification Fragment. Used to provide logic and data objects
 * to the view.
 */
public class NotificationsPresenter implements NotificationsContract.Presenter {
    private PrefManager prefManager;
    private NotificationsContract.View view;
    private List<String> saved_toilets = new ArrayList<>();
    private AmazonDynamoDBClient dynamoDBClient;
    private AWSConfiguration configuration;

    /**
     * Constructor for the Presenter which takes a view and preferences to give access to the data.
     *
     * @param view        The Notification fragment view to which data is presented
     * @param prefManager The sharedPreferences manager, used to access user data
     */
    NotificationsPresenter(NotificationsContract.View view, PrefManager prefManager) {
        this.prefManager = prefManager;
        this.view = view;
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        configuration = AWSMobileClient.getInstance().getConfiguration();
        dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
    }

    @Override
    public void loadToiletNames() {
        if (!prefManager.isGuest() && saved_toilets.size() == 0) {
            saved_toilets.addAll(prefManager.getToilets());
        }

        List<CharSequence> toilet_options = new ArrayList<>();
        toilet_options.add("All");

        //get names of toilets in localdb
        DbHelper dbHelper = new DbHelper(MainApplication.getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        for (int i = 0; i < saved_toilets.size(); i++) {
            Cursor cursor = dbHelper.readToiletInfo(database, saved_toilets.get(i));
            String toiletName;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                toiletName = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_NAME));
                toilet_options.add(toiletName);
            }
        }
        dbHelper.close();

        //add toilets to spinner
        view.addToiletNamesToSpinner(toilet_options);
    }

    @Override
    public void loadErrors() {
        DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();

        //clear local db
        DbHelper dbHelper = new DbHelper(MainApplication.getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.clearNotifications(database);
        dbHelper.close();

        //read errors from dynamoDB
        for (String toilet : saved_toilets) {
            int numErrors;
            DynamoDBQueryExpression<ToiletsDO> queryExpression = new DynamoDBQueryExpression<ToiletsDO>()
                    .withHashKeyValues(new ToiletsDO("aws/things/" + toilet))
                    .withConsistentRead(false);
            PaginatedQueryList<ToiletsDO> list = dynamoDBMapper.query(ToiletsDO.class, queryExpression);
            dbHelper = new DbHelper(MainApplication.getContext());
            database = dbHelper.getWritableDatabase();
            numErrors = dbHelper.saveErrorCodeBatch(list, database);
            dbHelper.close();
            Log.d("log", "Toilet: " + toilet + " " + numErrors + " errors.");
        }
    }

    @Override
    public void loadErrorInfo() {
        //connects to the db and reads each row into an arraylist populated by IncomingError objects
        ArrayList<IncomingError> incomingErrors = new ArrayList<>();
        incomingErrors.clear();
        DbHelper dbHelper = new DbHelper(MainApplication.getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.readErrorCode(database);
        String errorCode, toiletIP, date;
        int id;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex(DbContract.NOTIFY_ID));
                errorCode = cursor.getString(cursor.getColumnIndex(DbContract.ERROR_CODE));
                toiletIP = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_IP));
                date = cursor.getString(cursor.getColumnIndex(DbContract.NOTIFY_DATE));
                incomingErrors.add(new IncomingError(id, errorCode, toiletIP, date, null, null, null, null, null, 0, null, null, null));
            }
            cursor.close();
        }

        //lookup and set repairCode
        for (int i = 0; i < incomingErrors.size(); i++) {
            Cursor cursor3 = dbHelper.readRepairCode(database, incomingErrors.get(i).getErrorCode());
            String repairCode;
            if (cursor3.getCount() > 0) {
                cursor3.moveToFirst();
                repairCode = cursor3.getString(cursor3.getColumnIndex(DbContract.REPAIR_CODE));
                incomingErrors.get(i).setRepairCode(repairCode);
            }
            cursor3.close();
        }

        //gets info from toiletInfo
        for (int i = 0; i < incomingErrors.size(); i++) {
            Cursor cursor2 = dbHelper.readToiletInfo(database, incomingErrors.get(i).getToiletIP());
            String lat, lng, description, toiletName;
            if (cursor2.getCount() > 0) {
                cursor2.moveToFirst();
                lat = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_LAT));
                lng = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_LNG));
                description = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_DESC));
                toiletName = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_NAME));
                incomingErrors.get(i).setLat(lat);
                incomingErrors.get(i).setLng(lng);
                incomingErrors.get(i).setDescription(description);
                incomingErrors.get(i).setToiletName(toiletName);
            }
            cursor2.close();
        }

        //gets info from repairInfo
        for (int i = 0; i < incomingErrors.size(); i++) {
            Cursor cursor1 = dbHelper.readRepairInfo(database, incomingErrors.get(i).getRepairCode());
            String repairTitle, toolInfo, totalTime;
            int totalSteps;
            if (cursor1.getCount() > 0) {
                cursor1.moveToFirst();
                repairTitle = cursor1.getString(cursor1.getColumnIndex(DbContract.REPAIR_TITLE));
                toolInfo = cursor1.getString(cursor1.getColumnIndex(DbContract.TOOL_INFO));
                totalTime = cursor1.getString(cursor1.getColumnIndex(DbContract.TOTAL_TIME));
                totalSteps = cursor1.getInt(cursor1.getColumnIndex(DbContract.TOTAL_STEPS));
                incomingErrors.get(i).setRepairTitle(repairTitle);
                incomingErrors.get(i).setToolInfo(toolInfo);
                incomingErrors.get(i).setTotalTime(totalTime);
                incomingErrors.get(i).setTotalSteps(totalSteps);
            }
            cursor1.close();
        }
        dbHelper.close();

        //add error to adapter in the view
        for (IncomingError error : incomingErrors) {
            view.addErrorToAdapter(error);
        }
    }

    @Override
    public void handleDeleteNotification(final String timestamp, final String toiletID) {
        final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ToiletsDO toilet = new ToiletsDO();
                    toilet.setDeviceId("aws/things/" + toiletID);
                    toilet.setTimestamp(timestamp);
                    dynamoDBMapper.delete(toilet);
                } catch (NotAuthorizedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
