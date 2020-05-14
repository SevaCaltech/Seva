package edu.caltech.seva.activities.Repair.fragments;

import android.app.Activity;
import android.content.Intent;
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
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.util.Locale;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.RepairStep;

/**
 * Handles the tab fragments for a single repair being performed by the user.
 */
public class TabFragment extends Fragment implements RepairContract.View {

    //utility helpers
    private static final String REPAIR_CODE = "REPAIR_CODE";
    private static final String POSITION = "POSITION";
    private static final String TOILET_IP = "TOILET_IP";
    private static final String ERRORCODE = "ERRORCODE";
    private static final String NUM_STEPS = "NUM_STEPS";
    private static final int TAKE_PICTURE = 1;
    private TextToSpeech mTTs;
    private Uri imageUri;
    private File f;
    private PrefManager prefManager;
    private AmazonS3Client s3Client;

    //data objects
    private String errorCode, toiletIP;
    private int result, numSteps;

    //ui elements
    private TextView display_stepNum;
    private ImageView display_stepPic;
    private TextView display_stepText;
    private TextView display_stepInfo;
    //       private ImageView display_stepSymbol;
    private ImageView speech;
    private Button takePictureButton;


    //presenter
    private RepairPresenter presenter;

    //should be empty
    public TabFragment() {

    }

    //changes the step number on each tab
    public static TabFragment newInstance(String repairCode, int position, String toiletIP, String errorCode, int numSteps) {
        TabFragment tabFragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putString(REPAIR_CODE, repairCode);
        bundle.putString(TOILET_IP, toiletIP);
        bundle.putString(ERRORCODE, errorCode);
        bundle.putInt(POSITION, position);
        bundle.putInt(NUM_STEPS, numSteps);
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    //takes the information sent in the bundle and creates the tab fragment layouts
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.repair_step, container, false);
        prefManager = new PrefManager(getContext());
        presenter = new RepairPresenter(this);

        Bundle arguments = getArguments();
        String repairCode = arguments.getString(REPAIR_CODE);
        toiletIP = arguments.getString(TOILET_IP);
        errorCode = arguments.getString(ERRORCODE);
        int position = arguments.getInt(POSITION); //-1
        numSteps = arguments.getInt(NUM_STEPS);

        display_stepNum = rootView.findViewById(R.id.stepNum);
        display_stepPic = rootView.findViewById(R.id.stepPic);
        display_stepText = rootView.findViewById(R.id.stepText);
        display_stepInfo = rootView.findViewById(R.id.stepInfo);
//        display_stepSymbol = rootView.findViewById(R.id.stepSymbol);
        speech = rootView.findViewById(R.id.speechButton);
        takePictureButton = rootView.findViewById(R.id.take_picture_button);

        presenter.loadRepairStep(repairCode, position);

        if (!prefManager.isGuest()) {
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
                        if (status == TextToSpeech.SUCCESS)
                            result = mTTs.setLanguage(Locale.US);
                        else
                            Toast.makeText(getActivity(), "Feature not supported in your device..", Toast.LENGTH_SHORT).show();
                        if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA)
                            Toast.makeText(getActivity(), "Feature not supported in your device..", Toast.LENGTH_SHORT).show();
                        else
                            //    speak(errorCode);
                            mTTs.speak((String) display_stepText.getText(), TextToSpeech.QUEUE_FLUSH, null);
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

    private void takePhoto() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private void uploadPhoto() {
        String fileName = prefManager.getUsername() + "/" + toiletIP + "/" + errorCode + "-" + System.currentTimeMillis();

        TransferUtility transferUtility = TransferUtility.builder()
                .context(getContext().getApplicationContext())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(s3Client)
                .build();
        TransferObserver observer = transferUtility.upload(fileName + ".jpg", f);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("log", "onStateChanged: " + state.name());
                if (TransferState.COMPLETED == state) {
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
                int percentDone = (int) percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("log", ex.toString());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK && !prefManager.isGuest()) {
                    uploadPhoto();
                }
        }
    }

    @Override
    public void showRepairStep(RepairStep repairStep) {
//        repairSteps.add(repairStep);

        int picID = getResources().getIdentifier(repairStep.getStepPic(), "drawable", getActivity().getPackageName());
        int symbolID = getResources().getIdentifier(repairStep.getStepSymbol(), "drawable", getActivity().getPackageName());
        final String stepNumText = "Step " + repairStep.getStepNum() + " of " + numSteps;
        final String stepInfoText = "Tools : " + repairStep.getStepInfo();

        display_stepNum.setText(stepNumText);
        display_stepPic.setImageResource(picID);
        display_stepText.setText(repairStep.getStepText());
        display_stepInfo.setText(stepInfoText);
//        display_stepSymbol.setImageResource(symbolID);

        if (repairStep.getStepText().equals("Take a picture")) {
            takePictureButton.setVisibility(View.VISIBLE);
            display_stepPic.requestLayout();
            int height_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 334, getResources().getDisplayMetrics());
            display_stepPic.getLayoutParams().height = height_dp;
        }
    }
}
