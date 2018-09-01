package edu.caltech.seva.activities.Main.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;

import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.models.IncomingError;
import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.adapters.RecyclerAdapter;
import edu.caltech.seva.activities.Repair.RepairActivity;

//The notification fragment lists out notifications
public class Notifications extends Fragment implements RecyclerAdapter.ClickListener, DeleteDialog.DialogData {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter adapter;
    private ArrayList<IncomingError> arrayList = new ArrayList<>();
    private BroadcastReceiver broadcastReceiver;
    private int tempId,tempPos, result;
    private TextToSpeech mTTs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_notifications, null);
        getActivity().setTitle("Notifications");

        //sets up the recycler list view
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        //sets up adapter, readErrorFromDb should get the notifications that have already been stored in the db
        adapter = new RecyclerAdapter(getContext(),arrayList);
        adapter.setClickListener(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        readErrorFromDb();

        //connects to the ReceiverSMS class that listens for sms notifications from a specific number
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                readErrorFromDb();
            }
        };

        return rootView;
    }

    //connects to the db and reads each row into an arraylist populated by IncomingError objects
    private void readErrorFromDb(){
        arrayList.clear();
        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.readErrorCode(database);
        String errorCode,toiletId,date;
        int id;
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex("id"));
                errorCode = cursor.getString(cursor.getColumnIndex(DbContract.ERROR_CODE));
                toiletId = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_ID));
                date = cursor.getString(cursor.getColumnIndex(DbContract.NOTIFY_DATE));
                arrayList.add(new IncomingError(id,errorCode,toiletId,date,null,null,null,0,null,null,null));
            }
            cursor.close();
        }

        //gets info from repairInfo
        for (int i=0;i<arrayList.size();i++){
            Cursor cursor1 = dbHelper.readErrorInfo(database,arrayList.get(i).getErrorCode());
            String repairTitle, toolInfo, totalTime;
            int totalSteps;
            if (cursor1.getCount()>0){
                cursor1.moveToFirst();
                repairTitle = cursor1.getString(cursor1.getColumnIndex(DbContract.REPAIR_TITLE));
                toolInfo = cursor1.getString(cursor1.getColumnIndex(DbContract.TOOL_INFO));
                totalTime = cursor1.getString(cursor1.getColumnIndex(DbContract.TOTAL_TIME));
                totalSteps = cursor1.getInt(cursor1.getColumnIndex(DbContract.TOTAL_STEPS));
                arrayList.get(i).setRepairTitle(repairTitle);
                arrayList.get(i).setToolInfo(toolInfo);
                arrayList.get(i).setTotalTime(totalTime);
                arrayList.get(i).setTotalSteps(totalSteps);
            }
            cursor1.close();
        }

        //gets info from toiletInfo
        for (int i=0;i<arrayList.size();i++){
            Cursor cursor2 = dbHelper.readToiletInfo(database,arrayList.get(i).getToiletId());
            String lat,lng,description;
            if(cursor2.getCount()>0){
                cursor2.moveToFirst();
                lat = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_LAT));
                lng = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_LNG));
                description = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_DESC));
                arrayList.get(i).setLat(lat);
                arrayList.get(i).setLng(lng);
                arrayList.get(i).setDescription(description);
            }
            cursor2.close();
        }
        dbHelper.close();
        adapter.notifyDataSetChanged();
    }

    //will autoupdate the list when a new sms is received
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(DbContract.UPDATE_UI_FILTER));
    }

    //unregisters broadcastreceiver
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    //will bring up the delete dialog to check if user actually wants to delete the notification item
    @Override
    public void declineClicked(View view, int position, int id) {
        tempId = id;
        tempPos = position;
        //handles the dialog function
        DeleteDialog dialog = new DeleteDialog();
        dialog.setTargetFragment(this,0);
        dialog.show(getFragmentManager(), "delete_dialog");
    }

    //creates a new activity which is the repair guide and passes the errorCode from the notification to populate guide
    @Override
    public void acceptClicked(View view, IncomingError incomingError) {
        Intent intent = new Intent(getActivity(), RepairActivity.class);
        intent.putExtra("errorCode", incomingError.getErrorCode());
        intent.putExtra("repairTitle", incomingError.getRepairTitle());
        intent.putExtra("toolInfo", incomingError.getToolInfo());
        intent.putExtra("totalTime", incomingError.getTotalTime());
        intent.putExtra("totalSteps", incomingError.getTotalSteps());
        intent.putExtra("toiletId", incomingError.getToiletId());
        intent.putExtra("timestamp", incomingError.getDate());
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //TODO: should receive a toiletID which then is used to access db which matches toiletID to lat/lng coordinates
    //takes the lng and lat numbers from the sms notification and launches google maps directions
    @Override
    public void mapClicked(View view, String lat, String lng) {
        String directions = "http://maps.google.com/maps?daddr={0},{1}";
        Object[] args = {lat, lng};
        MessageFormat msg = new MessageFormat(directions);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(msg.format(args)));
        startActivity(intent);
    }

    //this occurs once the user is sure that the item should be deleted, accesses the db and deletes the row as well as the object from the arrayList
    @Override
    public void deleteConfirmed() {
        DbHelper dbHelper = new DbHelper(getActivity());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.deleteErrorCodeId(tempId, database);
        dbHelper.close();
        arrayList.remove(tempPos);
        adapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), "Notification Removed Successfully..", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void speechClicked(View view, final String errorCode) {
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
                    mTTs.speak(errorCode, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTTs!=null) {
            mTTs.stop();
            mTTs.shutdown();
        }
    }
}
