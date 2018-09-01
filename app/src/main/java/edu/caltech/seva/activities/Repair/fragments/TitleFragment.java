package edu.caltech.seva.activities.Repair.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;

//handles the title fragment
public class TitleFragment extends Fragment {

    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String TITLE = "TITLE";
    private static final String TOOL = "TOOL";
    private static final String TIME = "TIME";
    private static final String STEPS = "STEPS";

    private String errorCode, repairTitle, totalTime, toolInfo;
    private int totalSteps;

    //should be empty
    public TitleFragment( ) {

    }

    //sends the errorcode to the created fragment
    public static TitleFragment newInstance(String errorCode, String repairTitle, String toolInfo, String totalTime, int totalSteps) {
        TitleFragment titleFragment = new TitleFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ERROR_CODE,errorCode);
        bundle.putString(TITLE,repairTitle);
        bundle.putString(TOOL,toolInfo);
        bundle.putString(TIME,totalTime);
        bundle.putInt(STEPS,totalSteps);
        titleFragment.setArguments(bundle);
        return titleFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.repair_info,container,false);

        //receives the errorcode and reads the db
        Bundle bundle = getArguments();
        errorCode = bundle.getString(ERROR_CODE);
        repairTitle = bundle.getString(TITLE);
        totalTime = bundle.getString(TIME);
        toolInfo = bundle.getString(TOOL);
        totalSteps = bundle.getInt(STEPS);

        //sets text to repairInfo from db
        TextView display_repairTitle = (TextView) rootView.findViewById(R.id.repairTitle);
        TextView display_totalSteps = (TextView) rootView.findViewById(R.id.totalSteps);
        TextView display_totalTime = (TextView) rootView.findViewById(R.id.totalTime);
        TextView display_toolInfo = (TextView) rootView.findViewById(R.id.toolInfo);
        TextView display_errorCode = (TextView) rootView.findViewById(R.id.errorCode);
        display_repairTitle.setText(repairTitle);
        display_toolInfo.setText(toolInfo);
        display_totalSteps.setText(Integer.toString(totalSteps));
        display_totalTime.setText(totalTime);
        display_errorCode.setText("Error Code: "+errorCode);
        return rootView;
    }
}