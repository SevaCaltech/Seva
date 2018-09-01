package edu.caltech.seva.activities.Repair.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.models.RepairStep;

//handles the tab fragments
public class TabFragment extends Fragment {
    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String POSITION = "POSITION";
    private ArrayList<RepairStep> arrayList = new ArrayList<>();
    private TextToSpeech mTTs;
    private int result;

    //should be empty
    public TabFragment() {

    }

    //changes the step number on each tab
    public static TabFragment newInstance(String errorCode, int position) {
        TabFragment tabFragment = new TabFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ERROR_CODE,errorCode);
        bundle.putInt(POSITION,position);
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    //takes the information sent in the bundle and creates the tab fragment layouts
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.repair_step,container,false);

        Bundle arguments = getArguments();
        String errorCode = arguments.getString(ERROR_CODE);
        int position = arguments.getInt(POSITION)-1;
        readStepFromDb(errorCode);

        TextView display_stepNum = (TextView) rootView.findViewById(R.id .stepNum);
        ImageView display_stepPic = (ImageView) rootView.findViewById(R.id.stepPic);
        TextView display_stepText = (TextView) rootView.findViewById(R.id.stepText);
        TextView display_stepInfo = (TextView) rootView.findViewById(R.id.stepInfo);
        ImageView display_stepSymbol = (ImageView) rootView.findViewById(R.id.stepSymbol);
        ImageView speech = (ImageView) rootView.findViewById(R.id.speechButton);

        int picID = getResources().getIdentifier(arrayList.get(position).getStepPic(),"drawable",getActivity().getPackageName());
        int symbolID = getResources().getIdentifier(arrayList.get(position).getStepSymbol(),"drawable",getActivity().getPackageName());
        final String directions = arrayList.get(position).getStepText();

        display_stepNum.setText("Step " + Integer.toString(arrayList.get(position).getStepNum()) + " of "+arrayList.size());
        display_stepPic.setImageResource(picID);
        display_stepText.setText(directions);
        display_stepInfo.setText("Tools : " + arrayList.get(position).getStepInfo());
        display_stepSymbol.setImageResource(symbolID);

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
                            Toast.makeText(getActivity(), "Featuren not supported in your device..",Toast.LENGTH_SHORT).show();
                        else
                            //    speak(errorCode);
                            mTTs.speak(directions, TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
            }
        });

        return rootView;
    }

    //accesses the db and reads each row from the proper table, sets it into an arrayList with RepairStep objects
    public void readStepFromDb(String errorCode){
        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.readStep(database, errorCode);
        String stepPic, stepText, stepInfo, stepSymbol;
        int stepNum;
        if(cursor.getCount()>0){
            while (cursor.moveToNext()){
                stepNum = cursor.getInt(cursor.getColumnIndex(DbContract.STEP_NUM));
                stepInfo = cursor.getString(cursor.getColumnIndex(DbContract.STEP_INFO));
                stepPic = cursor.getString(cursor.getColumnIndex(DbContract.STEP_PIC));
                stepText = cursor.getString(cursor.getColumnIndex(DbContract.STEP_TEXT));
                stepSymbol = cursor.getString(cursor.getColumnIndex(DbContract.STEP_SYMBOL));
                arrayList.add(new RepairStep(stepNum,stepPic,stepText,stepInfo,stepSymbol));
            }
            cursor.close();
            dbHelper.close();
        }

    }
}
