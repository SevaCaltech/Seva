package edu.caltech.seva.activities.Main.Fragments.Toilets;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.Date;

import edu.caltech.seva.activities.MainApplication;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.Toilet;
import edu.caltech.seva.models.ToiletInfoDO;

import static edu.caltech.seva.models.Toilet_Status.DISABLED;
import static edu.caltech.seva.models.Toilet_Status.ERROR;
import static edu.caltech.seva.models.Toilet_Status.HEALTHY;

/**
 * Represents the Presenter for the Toilets Fragment. Used to provide logic and data objects to the
 * view.
 */
public class ToiletsPresenter implements ToiletsContract.Presenter {
    private ToiletsContract.View view;
    private PrefManager prefManager;
    private ArrayList<Toilet> toiletObjs;
    private static CheckStatus checkStatus;

    /**
     * Constructor for the Presenter which takes a view and preferences to give access.
     *
     * @param view        The Toilets fragment view to which data is presented
     * @param prefManager The sharedPreferences manager, used to access local user data
     */
    public ToiletsPresenter(ToiletsContract.View view, PrefManager prefManager) {
        this.view = view;
        this.prefManager = prefManager;
        this.toiletObjs = new ArrayList<>();
    }

    @Override
    public void getToiletInfo() {
        //get toilet info from local db
        ArrayList<String> toilet_ips = new ArrayList<>(prefManager.getToilets());
        DbHelper dbHelper = new DbHelper(MainApplication.getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        for (int i = 0; i < toilet_ips.size(); i++) {
            Cursor cursor = dbHelper.readToiletInfo(database, toilet_ips.get(i));
            String lat, lng, description, toiletName;
            String[] coords = new String[2];
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                lat = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_LAT));
                lng = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_LNG));
                description = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_DESC));
                toiletName = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_NAME));
                coords[0] = lat;
                coords[1] = lng;
                Toilet tempToilet = new Toilet(coords, description, toiletName, toilet_ips.get(i));
                toiletObjs.add(tempToilet);
            }
            cursor.close();
        }
        dbHelper.close();
        view.displayToilets(toiletObjs);
    }

    @Override
    public void getToiletStatus() {
        //setup aws
//        AWSMobileClient.getInstance().initialize(MainApplication.getContext()).execute(); //don't think I need this
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        DynamoDBMapper dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();

        checkStatus = new CheckStatus(dynamoDBMapper, toiletObjs);
        checkStatus.execute();
    }

    @Override
    public boolean isStatusCheckDone() {
        Log.d("checkStatus", checkStatus.getStatus().toString());
        return checkStatus.getStatus() == AsyncTask.Status.FINISHED;
    }

    private static class CheckStatus extends AsyncTask<Void, Void, Void> {
        private DynamoDBMapper dynamoDBMapper;
        private ArrayList<Toilet> toilets;

        CheckStatus(DynamoDBMapper dynamoDBMapper, ArrayList<Toilet> toilets) {
            this.dynamoDBMapper = dynamoDBMapper;
            this.toilets = toilets;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (Toilet toilet : toilets) {
                //poll dynamodb to see if toilet is enabled
                String query = "aws/things/" + toilet.getToiletIp();
                Log.d("log", "loading: " + query);
                ToiletInfoDO dynamoToilet = dynamoDBMapper.load(ToiletInfoDO.class, query);
                String toilet_status = dynamoToilet.getToiletStatus();

                if (toilet_status.equals("Enabled")) {
                    //poll localdb to see if there are any errors for it
                    DbHelper dbHelper = new DbHelper(MainApplication.getContext());
                    SQLiteDatabase database = dbHelper.getWritableDatabase();
                    int error_count = dbHelper.readNumToiletErrors(toilet.getToiletIp(), database);
                    database.close();
                    dbHelper.close();

                    Log.d("log", "\terrors: " + error_count);
                    if (error_count > 0)
                        toilet.setStatus(ERROR);
                    else
                        toilet.setStatus(HEALTHY);
                } else {
                    toilet.setStatus(DISABLED);
                }
                toilet.setSyncTimestamp(new Date(System.currentTimeMillis()));
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
