package edu.caltech.seva.activities.Repair.fragments;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.LambdaInterface;
import edu.caltech.seva.models.LambdaTriggerInfo;
import edu.caltech.seva.models.ToiletsDO;

//TODO: connect to server to start the process/poll sensors, should communicate back to let user know success
//handles the test fragment at the end of the repair guide viewpager
public class TestFragment extends Fragment {

    DynamoDBMapper dynamoDBMapper;
    PrefManager prefManager;
    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String TOILET_IP = "TOILET_ID";
    private static final String TIMESTAMP = "TIMESTAMP";
    LambdaInterface lambdaInterface;
    LambdaInvokerFactory factory;

    public TestFragment() {

    }

    public static TestFragment newInstance(String errorCode, String toiletIP, String timestamp) {
        TestFragment fragment = new TestFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ERROR_CODE, errorCode);
        bundle.putString(TOILET_IP, toiletIP);
        bundle.putString(TIMESTAMP, timestamp);
        fragment.setArguments(bundle);
        return fragment;
    }

    //sets up test repair button
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        prefManager = new PrefManager(getContext());
        View rootView = inflater.inflate(R.layout.repair_test, null);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Button testButton = (Button) rootView.findViewById(R.id.testButton);
        Button doneButton = (Button) rootView.findViewById(R.id.doneButton);

        Bundle arguments = getArguments();
        final String errorCode = arguments.getString(ERROR_CODE);
        final String toiletIP = arguments.getString(TOILET_IP);
        final String timestamp = arguments.getString(TIMESTAMP);

        if(!prefManager.isGuest()){
            AWSMobileClient.getInstance().initialize(getContext()).execute();
            AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
            AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
            dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(configuration)
                    .build();

            IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
            try {
                JSONObject cognitoObj = identityManager.getConfiguration().optJsonObject("CredentialsProvider");
                JSONObject myJSON = cognitoObj.getJSONObject("CognitoIdentity").getJSONObject("Default");
                final String IDENTITY_POOL_ID = myJSON.getString("PoolId");
                final String REGION = myJSON.getString("Region");
                Log.d("log", "check: " + IDENTITY_POOL_ID + " " + REGION);
                factory = new LambdaInvokerFactory(getActivity().getApplicationContext(),
                        Regions.fromName(REGION), credentialsProvider);
                lambdaInterface = factory.build(LambdaInterface.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Testing System..", Toast.LENGTH_SHORT).show();

                Date timestamp = new Date(System.currentTimeMillis());
                LambdaTriggerInfo info = new LambdaTriggerInfo(prefManager.getUsername(), timestamp, toiletIP, errorCode );
                LambdaTask lambda = new LambdaTask();
                lambda.execute(info);
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Marked as done..", Toast.LENGTH_SHORT).show();
                completeRepair(errorCode, toiletIP, timestamp);
            }
        });
        return rootView;
    }

    //delete item from dynamodb and sqlite
    public void completeRepair(final String errorCode, final String toiletIP, final String timestamp) {
        DbHelper dbHelper = new DbHelper(getActivity());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.deleteError(errorCode, toiletIP, database);
        dbHelper.close();

        if(!prefManager.isGuest()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ToiletsDO toilet = new ToiletsDO();
                    toilet.setDeviceId("aws/things/" + toiletIP);
                    toilet.setTimestamp(timestamp);
                    dynamoDBMapper.delete(toilet);
                }
            }).start();
        }
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    private class LambdaTask extends AsyncTask<LambdaTriggerInfo, Void, JsonObject> {

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
            }
        }

        @Override
        protected void onPostExecute(JsonObject result) {
            if (result == null) {
                return;
            }
            String message = result.get("msg").toString().replaceAll("\"","");
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
