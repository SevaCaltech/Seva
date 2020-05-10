package edu.caltech.seva.activities.Repair;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.activities.Repair.adapters.MyPagerAdapter;
import edu.caltech.seva.activities.Repair.fragments.RepairPresenter;
import edu.caltech.seva.activities.Repair.fragments.TabFragment;
import edu.caltech.seva.activities.Repair.fragments.TestFragment;
import edu.caltech.seva.activities.Repair.fragments.TitleFragment;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.LambdaInterface;
import edu.caltech.seva.models.LambdaTriggerInfo;
import edu.caltech.seva.models.RepairActivityData;

public class RepairActivity extends AppCompatActivity {
    //utility helpers
    private PrefManager prefManager;
    private ViewPager mPager;
    private LambdaInterface lambdaInterface;
    private LambdaInvokerFactory factory;
    private static PinpointManager pinpointManager;

    //data objects
    private RepairActivityData repairData;
    public Long timeStarted, timeEnded;
    public boolean isConnected;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNetworkState();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(this);

        Toolbar mToolbar;
        TabLayout mTabLayout;
        MyPagerAdapter mAdapter;

        //receive error code from the notification clicked and passes to fragments
        repairData = (RepairActivityData) getIntent().getSerializableExtra("RepairData");

        //sets up new activity toolbar and tab layout
        setContentView(R.layout.activity_repair);
        mAdapter = new MyPagerAdapter(getSupportFragmentManager(),repairData);
        mToolbar = findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setTitle("Repair Guide");
        mTabLayout = findViewById(R.id.tab_layout);
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mPager);

        timeStarted = System.currentTimeMillis();
        updateNetworkState();
        Log.d("log","guest: " + prefManager.isGuest() + " isConnected: "+ isConnected);
        if (!prefManager.isGuest() && isConnected)
            initializeAWS();
    }

    private void updateNetworkState() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = (activeNetwork != null && activeNetwork.isConnected());
        if (!isConnected && !prefManager.isGuest()) {
            Toast.makeText(this,"No Network Connection..", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeAWS(){
        Log.d("log", "Initializing AWS...");
        AWSMobileClient.getInstance().initialize(this).execute();
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        factory = new LambdaInvokerFactory(this.getApplicationContext(), Regions.fromName("us-east-1"), credentialsProvider);
        lambdaInterface = factory.build(LambdaInterface.class);

        PinpointConfiguration config = new PinpointConfiguration(this, credentialsProvider, configuration);
        pinpointManager = new PinpointManager(config);
        pinpointManager.getSessionClient().startSession();
    }

    public void testSystem(){
        Toast.makeText(this, "Testing System..", Toast.LENGTH_SHORT).show();

        Date timestamp = new Date(System.currentTimeMillis());
        LambdaTriggerInfo info = new LambdaTriggerInfo(prefManager.getUsername(), timestamp, repairData.getToiletIP(), repairData.getErrorCode() );
        LambdaTask lambda = new LambdaTask();
        lambda.execute(info);
    }

    public void logEvent() {
        try {
            long seconds_diff = (timeEnded - timeStarted) / 1000;
            Log.d("log", "[" + repairData.getRepairCode() + "] repair time (seconds): " + seconds_diff);
            if(pinpointManager != null){
                final AnalyticsEvent event =
                        pinpointManager.getAnalyticsClient().createEvent("RepairDone")
                                .withAttribute("repairCode", repairData.getRepairCode())
                                .withMetric("seconds_to_complete", (double) seconds_diff);

                pinpointManager.getAnalyticsClient().recordEvent(event);
                pinpointManager.getAnalyticsClient().submitEvents();
            }
        } catch (AmazonClientException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_repair, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel_option:
                AlertDialog.Builder cancel_dialog = new AlertDialog.Builder(this);
                cancel_dialog.setTitle("Cancel Job");
                cancel_dialog.setMessage("This job should be assigned to another technician.");
                cancel_dialog.setNegativeButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        prefManager.setCurrentJob(null);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
                cancel_dialog.setPositiveButton("GO BACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                cancel_dialog.show();
                return true;

            case R.id.help_option:
                final String helpNumber = "555-555-5555";
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Check app permissions..", Toast.LENGTH_SHORT).show();
                else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + helpNumber));
                    startActivity(callIntent);
                }
                return true;
            case R.id.test_option:
                if(isConnected)
                    testSystem();
                return true;
            case R.id.directions_option:
                String directions = "http://maps.google.com/maps?daddr={0},{1}";
                Object[] args = {repairData.getLat(), repairData.getLng()};
                MessageFormat msg = new MessageFormat(directions);
                Intent intent1 = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(msg.format(args)));
                startActivity(intent1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        updateNetworkState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isConnected && pinpointManager !=null){
            pinpointManager.getSessionClient().stopSession();
            pinpointManager.getAnalyticsClient().submitEvents();
        }
    }

    public class LambdaTask extends AsyncTask<LambdaTriggerInfo, Void, JsonObject> {

        @Override
        protected JsonObject doInBackground(LambdaTriggerInfo... params) {
            try{
                Log.d("log", "Sending: \n\tusername: " + params[0].getUsername() +
                        "\n\ttoiletIP: " + params[0].getToiletIP() + "\n\terrorCode: " +
                        params[0].getErrorCode() + "\n\ttimestamp: " + params[0].getTimestamp().toString());
                return  lambdaInterface.testButtonTriggered(params[0]);
            } catch (LambdaFunctionException e) {
                Log.d("log", "Failed to invoke test lambda", e);
                return null;
            } catch (RuntimeException e) {
                Log.d("log", "Took too long to connect to lambda", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JsonObject result) {
            if (result == null) {
                Toast.makeText(getApplicationContext(), "Unable to connect to toilet..", Toast.LENGTH_LONG).show();
                return;
            }
            String message = result.get("msg").toString().replaceAll("\"","");
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

}

