package edu.caltech.seva.activities.Main.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.models.ToiletsDO;
import edu.caltech.seva.models.UsersDO;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;


//TODO: connect help button to email/phone?
//the Home fragment that is the default starting page of the app which displays the user info
public class Home extends Fragment {
    TextView displayName,id, numNotifications, numToilets;
    Button helpButton;
    private BroadcastReceiver broadcastReceiver;
    static String name,uid;
    ArrayList<String> toilets = new ArrayList<>();
    UsersDO user = new UsersDO();
    ArrayList<ToiletsDO> dynamoToilets = new ArrayList<>();

    //dynamodb mapper object
    DynamoDBMapper dynamoDBMapper;
    //String userId = "us-east-1:c419b39c-2aa7-403a-bef7-f325fe6da450"; //clement
    String userId = "us-east-1:76e9c848-0f19-41fb-88bd-fd616972566c"; //josh


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.activity_home,null);
        helpButton = (Button) rootView.findViewById(R.id.helpButton);
        displayName = (TextView) rootView.findViewById(R.id.opName);
        id = (TextView) rootView.findViewById(R.id.opID);
        numNotifications = (TextView) rootView.findViewById(R.id.numNotifications);
        numToilets = (TextView) rootView.findViewById(R.id.numToilets);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(!preferences.getBoolean("firstTime",false)){
            // Initialize the Amazon Cognito credentials provider
            final CognitoCachingCredentialsProvider creds = new CognitoCachingCredentialsProvider(
                    getContext(),
                    "us-east-1:c56fb4a5-f2c8-4bf6-bc11-bc91b0461b28", // Identity pool ID
                    Regions.US_EAST_1 // Region
            );

            //initialize dynamodb
            AWSMobileClient.getInstance().initialize(getContext()).execute();
            AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
            AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(configuration)
                    .build();
            loadUser(creds);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("firstTime",true);
        editor.commit();

        getActivity().setTitle("Home");
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"Help Clicked..",Toast.LENGTH_SHORT).show();
            }
        });

        loadNotifications();

        //connects to the ReceiverSMS class that listens for sms notifications from a specific number
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadNotifications();
            }
        };

        return rootView;
    }

    public void loadUser(final CognitoCachingCredentialsProvider creds) {
       Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                user = dynamoDBMapper.load(UsersDO.class,userId);
                uid = creds.getIdentityId();
                name = user.getDisplayName();
                toilets.addAll(user.getToilets());
                final int num = toilets.size();
                final String numString = Integer.toString(num);

                Log.d("log","loading user...");
                Log.d("log","displayName: " + name);
                Log.d("log","toilets: " + toilets);
                Log.d("log","uid: " + uid);

                Set<String> toiletSet = new HashSet<String>();
                toiletSet.addAll(toilets);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putStringSet("toilets",toiletSet);
                editor.commit();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayName.setText(name);
                        numToilets.setText(numString);
                        id.setText(uid);
                    }
                });

                for(String toilet:toilets) {
                    DynamoDBQueryExpression<ToiletsDO> queryExpression = new DynamoDBQueryExpression<ToiletsDO>()
                            .withHashKeyValues(new ToiletsDO("aws/things/" + toilet))
                            .withConsistentRead(false);
                    PaginatedQueryList<ToiletsDO> list = dynamoDBMapper.query(ToiletsDO.class, queryExpression);
                    for(ToiletsDO row:list){
                        Log.d("log", row.getDeviceId().substring(11));
                        DbHelper dbHelper = new DbHelper(getContext());
                        SQLiteDatabase database =dbHelper.getWritableDatabase();
                        dbHelper.saveErrorCode(row.getData().get("error"),row.getDeviceId().substring(11),row.getTimestamp(),database);
                        dbHelper.close();
                    }
                }
            }
        });
       t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(DbContract.UPDATE_UI_FILTER));
    }

    //unregisters broadcastreceiver
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    private void loadNotifications() {
        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        int count = (int) DatabaseUtils.queryNumEntries(database, DbContract.NOTIFY_TABLE);
        numNotifications.setText(String.valueOf(count));
        database.close();
    }
}
