package edu.caltech.seva.activities.Repair.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.SingleShotLocationProvider;

//handles the title fragment
public class TitleFragment extends Fragment {

    private static final String REPAIR_CODE = "REPAIR_CODE";
    private static final String TITLE = "TITLE";
    private static final String TOOL = "TOOL";
    private static final String TIME = "TIME";
    private static final String STEPS = "STEPS";
    private static final String LAT = "LAT";
    private static final String LNG = "LNG";

    private String repairCode, repairTitle, totalTime, toolInfo, lat, lng;
    private int totalSteps;

    //should be empty
    public TitleFragment( ) {

    }

    //sends the errorcode to the created fragment
    public static TitleFragment newInstance(String repairCode, String repairTitle, String toolInfo, String totalTime, int totalSteps, String lat, String lng) {
        TitleFragment titleFragment = new TitleFragment();
        Bundle bundle = new Bundle();
        bundle.putString(REPAIR_CODE,repairCode);
        bundle.putString(TITLE,repairTitle);
        bundle.putString(TOOL,toolInfo);
        bundle.putString(TIME,totalTime);
        bundle.putInt(STEPS,totalSteps);
        bundle.putString(LAT, lat);
        bundle.putString(LNG, lng);
        titleFragment.setArguments(bundle);
        return titleFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.repair_info,container,false);

        //receives the errorcode and reads the db
        Bundle bundle = getArguments();
        repairCode = bundle.getString(REPAIR_CODE);
        repairTitle = bundle.getString(TITLE);
        totalTime = bundle.getString(TIME);
        toolInfo = bundle.getString(TOOL);
        totalSteps = bundle.getInt(STEPS);
        lat = bundle.getString(LAT);
        lng = bundle.getString(LNG);
        setHasOptionsMenu(true);

        //sets text to repairInfo from db
        TextView display_repairTitle = (TextView) rootView.findViewById(R.id.repairTitle);
        TextView display_totalSteps = (TextView) rootView.findViewById(R.id.totalSteps);
        TextView display_totalTime = (TextView) rootView.findViewById(R.id.totalTime);
        TextView display_toolInfo = (TextView) rootView.findViewById(R.id.toolInfo);
        TextView display_errorCode = (TextView) rootView.findViewById(R.id.errorCode);
        Button confirm_button  = (Button) rootView.findViewById(R.id.confirm_location_button);

        display_repairTitle.setText(repairTitle);
        display_toolInfo.setText(toolInfo);
        display_totalSteps.setText(Integer.toString(totalSteps));
        display_totalTime.setText(totalTime);
        display_errorCode.setText("Repair Code: "+repairCode);

        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("log","toilet coords: " + lat + ", " + lng);
                checkLocation();
            }
        });

        return rootView;
    }

    public void checkLocation(){
        Toast.makeText(getContext(), "Checking Location..", Toast.LENGTH_SHORT).show();
        SingleShotLocationProvider.requestSingleUpdate(getContext(), new SingleShotLocationProvider.LocationCallback() {
            @Override
            public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                Log.d("log", "my location: " + location.toString());
            }
        });
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_repair_info, menu);
    }
}