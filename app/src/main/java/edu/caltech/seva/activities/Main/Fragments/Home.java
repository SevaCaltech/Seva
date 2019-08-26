package edu.caltech.seva.activities.Main.Fragments;

import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleOwner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.helpers.AWSLoginModel;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.ToiletsDO;
import edu.caltech.seva.models.UsersDO;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;


//the Home fragment that is the default starting page of the app which displays the user info
public class Home extends Fragment implements View.OnClickListener{
    //utility helpers
    private PrefManager prefManager;
    private BroadcastReceiver broadcastReceiver;
    DynamoDBMapper dynamoDBMapper;
    private final String CHANNEL_ID = "seva_notification";
    private final int NOTIFICATION_ID = 1;
    InitialSync sync = new InitialSync();

    //data objects
    private UsersDO user = new UsersDO();
    private ArrayList<String> toilets = new ArrayList<>();

    //ui elements
    private ProgressBar progressBar;
    private TextView displayName, subtext, numNotifications, numToilets;
    private static String name, uid, email;
    private CardView toiletCard, notificationCard;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_home, null);
        displayName = (TextView) rootView.findViewById(R.id.opName);
        subtext = (TextView) rootView.findViewById(R.id.opID);
        numNotifications = (TextView) rootView.findViewById(R.id.numNotifications);
        numToilets = (TextView) rootView.findViewById(R.id.numToilets);
        progressBar = (ProgressBar) rootView.findViewById(R.id.spin_kit);
        toiletCard = (CardView) rootView.findViewById(R.id.toilet_card);
        notificationCard = (CardView) rootView.findViewById(R.id.notification_card);

        prefManager = new PrefManager(getContext());
        uid = prefManager.getUid();
        name = prefManager.getUsername();
        email = prefManager.getEmail();

        displayName.setText(name);
        numToilets.setText("0");
        subtext.setText(email);
        numNotifications.setText("0");

        if( prefManager.isFirstTimeLaunch() && !prefManager.isGuest()) {
            progressBar.setVisibility(View.VISIBLE);

            //initialize dynamodb
            Log.d("log", "Initializing AWS...");
            AWSMobileClient.getInstance().initialize(getContext()).execute();
            AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
            AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(configuration)
                    .build();
            sync.execute();
        }
        else {
            toilets.clear();
            if (prefManager.getToilets() != null)
                toilets.addAll(prefManager.getToilets());
            final String numString = Integer.toString(toilets.size());
            displayName.setText(name);
            numToilets.setText(numString);
            subtext.setText(email);

            Log.d("log", "SavedPrefs: ");
            Log.d("log", "\tdisplayName: " + name);
            Log.d("log", "\temail: " + email);
            Log.d("log", "\ttoilets: " + toilets);
            Log.d("log", "\tuid: " + uid);
            Log.d("log","\tisGuest: " + prefManager.isGuest());
            Log.d("log", "\tcurrentJob: " + prefManager.getCurrentJob());
        }

        getActivity().setTitle("Home");
        toiletCard.setOnClickListener(this);
        notificationCard.setOnClickListener(this);

        loadNumNotifications();

        //connects to the ReceiverSMS class that listens for sms notifications from a specific number
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadNumNotifications();
                displayNotification();
            }
        };
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(DbContract.UPDATE_UI_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    private void loadNumNotifications() {
        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        int count = (int) DatabaseUtils.queryNumEntries(database, DbContract.NOTIFY_TABLE);
        numNotifications.setText(String.valueOf(count));
        database.close();
    }

    private void displayNotification() {
        Context context = getContext();
        Intent intent = new Intent(context, Notifications.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.icon_seva_small);
        builder.setContentTitle("New Repair");
        builder.setContentText("There is a new repair.");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        String fragment_tag= "";
        switch (view.getId()){
            case R.id.toilet_card:
                //launch mytoilets fragment
                fragment = new Toilets();
                fragment_tag = "TOILETS";
                break;
            case R.id.notification_card:
                //launch notifications fragment
                fragment = new Notifications();
                fragment_tag = "NOTIFICATIONS";
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.screen_area, fragment, fragment_tag);
            ft.addToBackStack(null);
            ft.commit();
            ((MainActivity)getActivity()).currentFragmentTag = fragment_tag;
        }
    }

    private class InitialSync extends AsyncTask<Void, String, String> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            loadUserInfo();
            loadErrorInfo();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.d("log", values[0] + ": " +  values[1] + " errors.");
            Log.d("log", "total: " + values[2]);
            numNotifications.setText(values[2]);
        }

        @Override
        protected void onPostExecute(String s) {
            prefManager.setFirstTimeLaunch(false);
            progressBar.setVisibility(View.INVISIBLE);
        }

        public void loadUserInfo(){
            user = dynamoDBMapper.load(UsersDO.class, uid);
            name = user.getDisplayName();
            email = user.getEmail();
            toilets.clear();
            toilets.addAll(user.getToilets());
            final int num = toilets.size();
            final String numString = Integer.toString(num);

            Log.d("log", "loading user...");
            Log.d("log", "\tdisplayName: " + name);
            Log.d("log", "\temail: " + email);
            Log.d("log", "\ttoilets: " + toilets);
            Log.d("log", "\tuid: " + uid);
            Log.d("log","\tisGuest: " + prefManager.isGuest());

            Set<String> toiletSet = new HashSet<String>();
            toiletSet.addAll(toilets);
            prefManager.setToilets(toiletSet);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayName.setText(name);
                    numToilets.setText(numString);
                    subtext.setText(email);
                }
            });
        }

        public void loadErrorInfo(){
            int total = 0;

            //get all errors for assigned toilets and store in local db
            for(String toilet:toilets) {
                int numErrors;
                DynamoDBQueryExpression<ToiletsDO> queryExpression = new DynamoDBQueryExpression<ToiletsDO>()
                        .withHashKeyValues(new ToiletsDO("aws/things/" + toilet))
                        .withConsistentRead(false);
                PaginatedQueryList<ToiletsDO> list = dynamoDBMapper.query(ToiletsDO.class, queryExpression);
                DbHelper dbHelper = new DbHelper(getContext());
                SQLiteDatabase database =dbHelper.getWritableDatabase();
                numErrors = dbHelper.saveErrorCodeBatch(list, database);
                total += numErrors;
                publishProgress(toilet, Integer.toString(numErrors), Integer.toString(total));
            }
        }
    }
}
