package edu.caltech.seva.activities.Repair;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Date;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.activities.Repair.adapters.MyPagerAdapter;
import edu.caltech.seva.activities.Repair.fragments.TabFragment;
import edu.caltech.seva.activities.Repair.fragments.TestFragment;
import edu.caltech.seva.activities.Repair.fragments.TitleFragment;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.LambdaInterface;
import edu.caltech.seva.models.LambdaTriggerInfo;
import edu.caltech.seva.models.RepairActivityData;

public class RepairActivity extends AppCompatActivity {

    PrefManager prefManager;
    ViewPager mPager;
    RepairActivityData repairData;
    LambdaInterface lambdaInterface;
    LambdaInvokerFactory factory;

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
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setTitle("Repair Guide");
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mPager);

        if (!prefManager.isGuest())
            initializeAWS();
    }

    public void initializeAWS(){
        AWSMobileClient.getInstance().initialize(this).execute();
        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();

        IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
        try {
            JSONObject cognitoObj = identityManager.getConfiguration().optJsonObject("CredentialsProvider");
            JSONObject myJSON = cognitoObj.getJSONObject("CognitoIdentity").getJSONObject("Default");
            final String IDENTITY_POOL_ID = myJSON.getString("PoolId");
            final String REGION = myJSON.getString("Region");
            Log.d("log", "check: " + IDENTITY_POOL_ID + " " + REGION);
            factory = new LambdaInvokerFactory(this.getApplicationContext(),
                    Regions.fromName(REGION), credentialsProvider);
            lambdaInterface = factory.build(LambdaInterface.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void testSystem(){
        Toast.makeText(this, "Testing System..", Toast.LENGTH_SHORT).show();

        Date timestamp = new Date(System.currentTimeMillis());
        LambdaTriggerInfo info = new LambdaTriggerInfo(prefManager.getUsername(), timestamp, repairData.getToiletIP(), repairData.getErrorCode() );
        LambdaTask lambda = new LambdaTask();
        lambda.execute(info);
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
            case R.id.search_option:
                Toast.makeText(this,"Search selected..", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.test_option:
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

