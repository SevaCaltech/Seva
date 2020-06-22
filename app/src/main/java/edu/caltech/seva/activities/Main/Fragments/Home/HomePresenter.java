package edu.caltech.seva.activities.Main.Fragments.Home;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.caltech.seva.activities.MainApplication;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.ToiletsDO;
import edu.caltech.seva.models.User;
import edu.caltech.seva.models.UsersDO;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

/**
 * Represents the Presenter for the Home Fragment. Used to provide logic and data objects to the
 * view.
 */
public class HomePresenter implements HomeContract.Presenter {
    private HomeContract.View view;
    private PrefManager prefManager;

    /**
     * Constructor for the Presenter which takes a view and preferences to give access.
     *
     * @param view        The Home fragment view to which data is presented
     * @param prefManager The sharedPreferences manager, used to access user data
     */
    public HomePresenter(HomeContract.View view, PrefManager prefManager) {
        this.view = view;
        this.prefManager = prefManager;
    }

    @Override
    public void loadNotifications() {
        DbHelper dbHelper = new DbHelper(MainApplication.getContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        int count = (int) DatabaseUtils.queryNumEntries(database, DbContract.NOTIFY_TABLE);
        database.close();
        view.showNumNotifications(count);
    }

    @Override
    public void loadUserInfo() {
        if (prefManager.isFirstTimeLaunch() && !prefManager.isGuest()) {
            //initialize dynamodb
            Log.d("log", "Initializing AWS...");
//            AWSMobileClient.getInstance().initialize(MainApplication.getContext()).execute();
            AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
            AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(configuration)
                    .build();

            UpdateToken updateToken = new UpdateToken(dynamoDBClient, prefManager);
            updateToken.execute();

            InitialSync sync = new InitialSync(view, prefManager, dynamoDBMapper);
            sync.execute();
        } else {
            String name = prefManager.getUsername();
            String uid = prefManager.getUid();
            String email = prefManager.getEmail();
            ArrayList<String> toiletList = new ArrayList<>(prefManager.getToilets());
            User user = new User(name, email, uid, toiletList);

            view.showUserInfo(user);

//            Log.d("log", "SavedPrefs: ");
//            Log.d("log", "\tdisplayName: " + name);
//            Log.d("log", "\temail: " + email);
//            Log.d("log", "\ttoilets: " + user.getNumToilets());
//            Log.d("log", "\tuid: " + uid);
//            Log.d("log", "\tisGuest: " + prefManager.isGuest());
//            Log.d("log", "\tcurrentJob: " + prefManager.getCurrentJob());
        }

    }

    private static class InitialSync extends AsyncTask<Void, String, String> {
        private final HomeContract.View view;
        private final PrefManager prefManager;
        private final DynamoDBMapper dbMapper;

        InitialSync(HomeContract.View view, PrefManager prefManager, DynamoDBMapper dbMapper) {
            this.view = view;
            this.prefManager = prefManager;
            this.dbMapper = dbMapper;
        }

        @Override
        protected void onPreExecute() {
            view.showProgressBar();
        }

        @Override
        protected String doInBackground(Void... voids) {
            queryUserInfo();
            loadErrorInfo();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d("log", values[0] + ": " + values[1] + " errors.");
            Log.d("log", "total: " + values[2]);
            view.showNumNotifications(Integer.parseInt(values[2]));
        }

        @Override
        protected void onPostExecute(String s) {
            prefManager.setFirstTimeLaunch(false);
            view.hideProgressBar();
        }

        private void queryUserInfo() {
            UsersDO userObj = dbMapper.load(UsersDO.class, prefManager.getUid());
            final String name = userObj.getDisplayName();
            final String email = userObj.getEmail();
            String phone = userObj.getPhone();
            String uid = userObj.getUserId();
            Map<String, Boolean> userSettings = userObj.getUserSettings();
            ArrayList<String> toilets = new ArrayList<>(userObj.getToilets());
            final User user = new User(name, email, phone, uid, toilets);
            boolean sendPush = userSettings.get("sendPush");
            boolean sendSMS = userSettings.get("sendSMS");

            Log.d("log", "loading user...");
            Log.d("log", "\tdisplayName: " + name);
            Log.d("log", "\temail: " + email);
            Log.d("log", "\ttoilets: " + toilets);
            Log.d("log", "\tuid: " + uid);
            Log.d("log", "\tphone: " + phone);
            Log.d("log", "\tdeviceToken: " + userObj.getDeviceToken());
            Log.d("log", String.format("\tuserSettings: sendPush [%s], sendSMS [%s]", sendPush,
                    sendSMS));
            Log.d("log", "\tisGuest: " + prefManager.isGuest());

            Set<String> toiletSet = new HashSet<String>(toilets);
            prefManager.setToilets(toiletSet);
            prefManager.setSendPush(sendPush);
            prefManager.setSendSms(sendSMS);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.showUserInfo(user);
                }
            });
        }

        private void loadErrorInfo() {
            int total = 0;

            //get all errors for assigned toilets and store in local db
            for (String toilet : prefManager.getToilets()) {
                int numErrors;
                DynamoDBQueryExpression<ToiletsDO> queryExpression = new DynamoDBQueryExpression<ToiletsDO>()
                        .withHashKeyValues(new ToiletsDO("aws/things/" + toilet))
                        .withConsistentRead(false);
                PaginatedQueryList<ToiletsDO> list = dbMapper.query(ToiletsDO.class, queryExpression);
                DbHelper dbHelper = new DbHelper(MainApplication.getContext());
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                numErrors = dbHelper.saveErrorCodeBatch(list, database);
                total += numErrors;
                publishProgress(toilet, Integer.toString(numErrors), Integer.toString(total));
                database.close();
            }
        }
    }

    private static class UpdateToken extends AsyncTask<Void, Void, String> {

        private final AmazonDynamoDBClient dynamoDBClient;
        private final PrefManager prefManager;

        UpdateToken(AmazonDynamoDBClient dynamoDBClient, PrefManager prefManager) {
            this.dynamoDBClient = dynamoDBClient;
            this.prefManager = prefManager;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String token = prefManager.getDeviceToken();
            if (token.equals("")) {
                throw new RuntimeException("deviceToken can't be empty");
            }

            HashMap<String, AttributeValue> key = new HashMap<>();
            key.put("uid", new AttributeValue().withS(prefManager.getUid()));

            Map<String, AttributeValue> deviceToken = new HashMap<>();
            deviceToken.put(":val1", new AttributeValue().withS(token));

            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                    .withTableName("SevaOperators")
                    .withKey(key)
                    .withUpdateExpression("set deviceToken = :val1")
                    .withExpressionAttributeValues(deviceToken);
            UpdateItemResult result = dynamoDBClient.updateItem(updateItemRequest);;
            Log.d("updateDeviceToken", result.toString());
            return result.toString();
        }
    }
}
