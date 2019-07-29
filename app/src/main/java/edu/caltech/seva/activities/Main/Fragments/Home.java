package edu.caltech.seva.activities.Main.Fragments;

import android.arch.lifecycle.LifecycleOwner;
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
import edu.caltech.seva.helpers.AWSLoginModel;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.ToiletsDO;
import edu.caltech.seva.models.UsersDO;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;


//TODO: connect help button to email/phone?
//the Home fragment that is the default starting page of the app which displays the user info
public class Home extends Fragment {
    private PrefManager prefManager;
    private CognitoCachingCredentialsProvider creds;
    TextView displayName, id, numNotifications, numToilets;
    Button helpButton;
    private BroadcastReceiver broadcastReceiver;
    static String name, uid;
    ArrayList<String> toilets = new ArrayList<>();
    UsersDO user = new UsersDO();
//    ArrayList<ToiletsDO> dynamoToilets = new ArrayList<>();
    DynamoDBMapper dynamoDBMapper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_home, null);
        helpButton = (Button) rootView.findViewById(R.id.helpButton);
        displayName = (TextView) rootView.findViewById(R.id.opName);
        id = (TextView) rootView.findViewById(R.id.opID);
        numNotifications = (TextView) rootView.findViewById(R.id.numNotifications);
        numToilets = (TextView) rootView.findViewById(R.id.numToilets);

        prefManager = new PrefManager(getContext());
        uid = prefManager.getUid();
        if(name == null) {
            Log.d("log", "Initializing AWS...");

            // Initialize the Amazon Cognito credentials provider
            IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
            try{
                JSONObject myJSON = identityManager.getConfiguration().optJsonObject("CredentialsProvider");
                final String IDENTITY_POOL_ID = myJSON.getString("PoolId");
                final String REGION = myJSON.getString("Region");
                creds = new CognitoCachingCredentialsProvider(getContext(), IDENTITY_POOL_ID , Regions.fromName(REGION));
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
        else {
            toilets.addAll(prefManager.getToilets());
            final String numString = Integer.toString(toilets.size());
            displayName.setText(name);
            numToilets.setText(numString);
            id.setText(uid);
        }

        getActivity().setTitle("Home");
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Help Clicked..", Toast.LENGTH_SHORT).show();
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
                user = dynamoDBMapper.load(UsersDO.class, uid);
                name = user.getDisplayName();
                toilets.addAll(user.getToilets());
                final int num = toilets.size();
                final String numString = Integer.toString(num);

                Log.d("log", "loading user...");
                Log.d("log", "\tdisplayName: " + name);
                Log.d("log", "\ttoilets: " + toilets);
                Log.d("log", "\tuid: " + uid);

                Set<String> toiletSet = new HashSet<String>();
                toiletSet.addAll(toilets);
                prefManager.setToilets(toiletSet);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayName.setText(name);
                        numToilets.setText(numString);
                        id.setText(uid);
                    }
                });

//                for(String toilet:toilets) {
//                    DynamoDBQueryExpression<ToiletsDO> queryExpression = new DynamoDBQueryExpression<ToiletsDO>()
//                            .withHashKeyValues(new ToiletsDO("aws/things/" + toilet))
//                            .withConsistentRead(false);
//                    PaginatedQueryList<ToiletsDO> list = dynamoDBMapper.query(ToiletsDO.class, queryExpression);
//                    for(ToiletsDO row:list){
//                        Log.d("log", row.getDeviceId().substring(11));
//                        DbHelper dbHelper = new DbHelper(getContext());
//                        SQLiteDatabase database =dbHelper.getWritableDatabase();
//                        dbHelper.saveErrorCode(row.getData().get("error"),row.getDeviceId().substring(11),row.getTimestamp(),database);
//                        dbHelper.close();
//                    }
//                }
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
        String who = prefManager.getUsername();
        String saved_email = prefManager.getEmail();
        String saved_uid = prefManager.getUid();
        Log.d("log", "saved username: " + who);
        Log.d("log", "saved email: " + saved_email);
        Log.d("log", "saved uid: " + saved_uid);
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
