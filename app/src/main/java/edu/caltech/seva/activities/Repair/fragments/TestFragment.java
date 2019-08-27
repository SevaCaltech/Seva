package edu.caltech.seva.activities.Repair.fragments;

import android.content.ClipData;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.util.Date;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.activities.Repair.RepairActivity;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.LambdaInterface;
import edu.caltech.seva.models.LambdaTriggerInfo;
import edu.caltech.seva.models.ToiletsDO;

//TODO: connect to server to start the process/poll sensors, should communicate back to let user know success
//handles the test fragment at the end of the repair guide viewpager
public class TestFragment extends Fragment implements View.OnClickListener {

    DynamoDBMapper dynamoDBMapper;
    PrefManager prefManager;
    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String TOILET_IP = "TOILET_ID";
    private static final String TIMESTAMP = "TIMESTAMP";
    String errorCode, toiletIP, timestamp;

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
        Button pictureButton = (Button) rootView.findViewById(R.id.pictureButton);
        testButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);
        pictureButton.setOnClickListener(this);

        Bundle arguments = getArguments();
        errorCode = arguments.getString(ERROR_CODE);
        toiletIP = arguments.getString(TOILET_IP);
        timestamp = arguments.getString(TIMESTAMP);

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
        }

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

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.testButton:
                ((RepairActivity)getActivity()).testSystem();
                break;
            case R.id.pictureButton:
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
//                ClipData clip = ClipData.newUri(getContext().getContentResolver(), "A photo", Uri.fromFile(f));
//                intent.setClipData(clip);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, 0);
                break;
            case R.id.doneButton:
                Toast.makeText(getActivity(), "Marked as done..", Toast.LENGTH_SHORT).show();
                prefManager.setCurrentJob(null);
                completeRepair(errorCode, toiletIP, timestamp);
                break;
        }
    }
}
