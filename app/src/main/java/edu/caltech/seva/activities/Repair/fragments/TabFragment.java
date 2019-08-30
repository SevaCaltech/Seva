package edu.caltech.seva.activities.Repair.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.RepairStep;

//handles the tab fragments
public class TabFragment extends Fragment {
    private static final String REPAIR_CODE = "REPAIR_CODE";
    private static final String POSITION = "POSITION";
    private static final String TOILET_IP = "TOILET_IP";
    private static final String ERRORCODE = "ERRORCODE";
    private static final int TAKE_PICTURE = 1;
    private String errorCode, toiletIP;
    private ArrayList<RepairStep> repairSteps = new ArrayList<>();
    private TextToSpeech mTTs;
    private int result;
    private Uri imageUri;
    private File f;
    PrefManager prefManager;
    AmazonS3Client s3Client;

    //should be empty
    public TabFragment() {

    }

    //changes the step number on each tab
    public static TabFragment newInstance(String repairCode, int position, String toiletIP, String errorCode) {
        TabFragment tabFragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putString(REPAIR_CODE,repairCode);
        bundle.putString(TOILET_IP, toiletIP);
        bundle.putString(ERRORCODE, errorCode);
        bundle.putInt(POSITION,position);
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    //takes the information sent in the bundle and creates the tab fragment layouts
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.repair_step,container,false);
        prefManager = new PrefManager(getContext());

        Bundle arguments = getArguments();
        String repairCode = arguments.getString(REPAIR_CODE);
        toiletIP = arguments.getString(TOILET_IP);
        errorCode = arguments.getString(ERRORCODE);
        int position = arguments.getInt(POSITION)-1;
        readStepFromDb(repairCode);

        TextView display_stepNum = (TextView) rootView.findViewById(R.id .stepNum);
        ImageView display_stepPic = (ImageView) rootView.findViewById(R.id.stepPic);
        TextView display_stepText = (TextView) rootView.findViewById(R.id.stepText);
        TextView display_stepInfo = (TextView) rootView.findViewById(R.id.stepInfo);
//        ImageView display_stepSymbol = (ImageView) rootView.findViewById(R.id.stepSymbol);
        ImageView speech = (ImageView) rootView.findViewById(R.id.speechButton);
        Button takePictureButton = (Button) rootView.findViewById(R.id.take_picture_button);

        int picID = getResources().getIdentifier(repairSteps.get(position).getStepPic(),"drawable",getActivity().getPackageName());
        int symbolID = getResources().getIdentifier(repairSteps.get(position).getStepSymbol(),"drawable",getActivity().getPackageName());
        final String directions = repairSteps.get(position).getStepText();

        display_stepNum.setText("Step " + Integer.toString(repairSteps.get(position).getStepNum()) + " of "+repairSteps.size());
        display_stepPic.setImageResource(picID);
        display_stepText.setText(directions);
        display_stepInfo.setText("Tools : " + repairSteps.get(position).getStepInfo());
//        display_stepSymbol.setImageResource(symbolID);

        if (directions.equals("Take a picture")){
            takePictureButton.setVisibility(View.VISIBLE);
            display_stepPic.requestLayout();
            int height_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 334, getResources().getDisplayMetrics());
            display_stepPic.getLayoutParams().height = height_dp;
        }

        if(!prefManager.isGuest()){
            AWSMobileClient.getInstance().initialize(getContext()).execute();
            AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
            s3Client = new AmazonS3Client(credentialsProvider);
        }

        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTTs = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status == TextToSpeech.SUCCESS)
                            result = mTTs.setLanguage(Locale.US);
                        else
                            Toast.makeText(getActivity(),"Feature not supported in your device..",Toast.LENGTH_SHORT).show();
                        if(result==TextToSpeech.LANG_NOT_SUPPORTED ||result== TextToSpeech.LANG_MISSING_DATA)
                            Toast.makeText(getActivity(), "Feature not supported in your device..",Toast.LENGTH_SHORT).show();
                        else
                            //    speak(errorCode);
                            mTTs.speak(directions, TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
            }
        });

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        return rootView;
    }

    //accesses the db and reads each row from the proper table, sets it into an arrayList with RepairStep objects
    public void readStepFromDb(String repairCode){
        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.readStep(database, repairCode);
        String stepPic, stepText, stepInfo, stepSymbol;
        int stepNum;
        if(cursor.getCount()>0){
            while (cursor.moveToNext()){
                stepNum = cursor.getInt(cursor.getColumnIndex(DbContract.STEP_NUM));
                stepInfo = cursor.getString(cursor.getColumnIndex(DbContract.STEP_INFO));
                stepPic = cursor.getString(cursor.getColumnIndex(DbContract.STEP_PIC));
                stepText = cursor.getString(cursor.getColumnIndex(DbContract.STEP_TEXT));
                stepSymbol = cursor.getString(cursor.getColumnIndex(DbContract.STEP_SYMBOL));
                repairSteps.add(new RepairStep(stepNum,stepPic,stepText,stepInfo,stepSymbol));
            }
            cursor.close();
            dbHelper.close();
        }
    }

    public void takePhoto(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public void uploadPhoto(){
        String fileName = prefManager.getUsername() + "/" + toiletIP + "/" + errorCode + "-" + System.currentTimeMillis();

        TransferUtility transferUtility = TransferUtility.builder()
                .context(getContext().getApplicationContext())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(s3Client)
                .build();
        TransferObserver observer = transferUtility.upload(fileName + ".jpg",f);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("log", "onStateChanged: " + state.name());
                if(TransferState.COMPLETED == state) {
                    Toast.makeText(getContext(), "Upload Completed!", Toast.LENGTH_SHORT).show();
                    Log.d("log", "upload completed");
                    f.delete();
                } else if (TransferState.FAILED == state) {
                    Log.d("log", "upload failed");
                    f.delete();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("log",ex.toString());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case TAKE_PICTURE:
                if(resultCode == Activity.RESULT_OK && !prefManager.isGuest()) {
                    uploadPhoto();
                }
        }
    }
}
