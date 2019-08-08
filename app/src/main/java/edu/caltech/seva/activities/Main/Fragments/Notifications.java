package edu.caltech.seva.activities.Main.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

//The notification fragment lists out notifications
public class Notifications extends Fragment implements RecyclerAdapter.ClickListener, DeleteDialog.DialogData {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter adapter;
    private ArrayList<IncomingError> incomingErrors = new ArrayList<>();
    private BroadcastReceiver broadcastReceiver;
    private int tempId,tempPos, result;
    private TextToSpeech mTTs;
    public ProgressBar progressBar;
    private Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_notifications, null);
        getActivity().setTitle("Notifications");
        progressBar = (ProgressBar) rootView.findViewById(R.id.notify_progress);

        //sets up the recycler list view
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        //sets up adapter, readErrorFromDb should get the notifications that have already been stored in the db
        adapter = new RecyclerAdapter(getContext(), incomingErrors);
        adapter.setClickListener(this);
        adapter.setHasStableIds(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(4);
        recyclerView.setAdapter(adapter);

        Loader load = new Loader();
        load.execute();
//        readErrorFromDb();

        //connects to the ReceiverSMS class that listens for sms notifications from a specific number
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                readErrorFromDb();
            }
        };

        return rootView;
    }

    //will autoupdate the list when a new sms is received
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(DbContract.UPDATE_UI_FILTER));

        //restore recycler viewstate
        if(mBundleRecyclerViewState !=null){
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
        adapter.notifyDataSetChanged();
        Log.d("onresume","count: " + adapter.getItemCount());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);

        //save viewstate
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = recyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
        Log.d("onpause","count: " + adapter.getItemCount());

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
        intent.putExtra("repairCode", incomingError.getRepairCode());
        intent.putExtra("repairTitle", incomingError.getRepairTitle());
        intent.putExtra("toolInfo", incomingError.getToolInfo());
        intent.putExtra("totalTime", incomingError.getTotalTime());
        intent.putExtra("totalSteps", incomingError.getTotalSteps());
        intent.putExtra("toiletIP", incomingError.getToiletIP());
        intent.putExtra("timestamp", incomingError.getDate());
        intent.putExtra("toiletName", incomingError.getToiletName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

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
        incomingErrors.remove(tempPos);
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
        Log.d("ondestroy","count: " + adapter.getItemCount());
        if (mTTs!=null) {
            mTTs.stop();
            mTTs.shutdown();
        }
    }

    public class Loader extends AsyncTask<Void, Void, String>{
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            readErrorFromDb();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //connects to the db and reads each row into an arraylist populated by IncomingError objects
    private void readErrorFromDb(){
        Log.d("startreading","count: " + adapter.getItemCount());

        incomingErrors.clear();
        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.readErrorCode(database);
        String errorCode,toiletIP,date;
        int id;
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex(DbContract.NOTIFY_ID));
                errorCode = cursor.getString(cursor.getColumnIndex(DbContract.ERROR_CODE));
                toiletIP = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_IP));
                date = cursor.getString(cursor.getColumnIndex(DbContract.NOTIFY_DATE));
                incomingErrors.add(new IncomingError(id,errorCode,toiletIP,date,null,null,null,null,null,0,null,null,null));
            }
            cursor.close();
        }

        //lookup and set repairCode
        for (int i=0;i<incomingErrors.size();i++){
            Cursor cursor3 = dbHelper.readRepairCode(database,incomingErrors.get(i).getErrorCode());
            String repairCode;
            if (cursor3.getCount()>0){
                cursor3.moveToFirst();
                repairCode = cursor3.getString(cursor3.getColumnIndex(DbContract.REPAIR_CODE));
                incomingErrors.get(i).setRepairCode(repairCode);
            }
            cursor3.close();
        }

        //gets info from toiletInfo
        for (int i=0;i<incomingErrors.size();i++){
            Cursor cursor2 = dbHelper.readToiletInfo(database,incomingErrors.get(i).getToiletIP());
            String lat,lng,description, toiletName;
            if(cursor2.getCount()>0){
                cursor2.moveToFirst();
                lat = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_LAT));
                lng = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_LNG));
                description = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_DESC));
                toiletName = cursor2.getString(cursor2.getColumnIndex(DbContract.TOILET_NAME));
                incomingErrors.get(i).setLat(lat);
                incomingErrors.get(i).setLng(lng);
                incomingErrors.get(i).setDescription(description);
                incomingErrors.get(i).setToiletName(toiletName);
            }
            cursor2.close();
        }

        //gets info from repairInfo
        for (int i=0;i<incomingErrors.size();i++){
            Cursor cursor1 = dbHelper.readRepairInfo(database,incomingErrors.get(i).getRepairCode());
            String repairTitle, toolInfo, totalTime;
            int totalSteps;
            if (cursor1.getCount()>0){
                cursor1.moveToFirst();
                repairTitle = cursor1.getString(cursor1.getColumnIndex(DbContract.REPAIR_TITLE));
                toolInfo = cursor1.getString(cursor1.getColumnIndex(DbContract.TOOL_INFO));
                totalTime = cursor1.getString(cursor1.getColumnIndex(DbContract.TOTAL_TIME));
                totalSteps = cursor1.getInt(cursor1.getColumnIndex(DbContract.TOTAL_STEPS));
                incomingErrors.get(i).setRepairTitle(repairTitle);
                incomingErrors.get(i).setToolInfo(toolInfo);
                incomingErrors.get(i).setTotalTime(totalTime);
                incomingErrors.get(i).setTotalSteps(totalSteps);
            }
            cursor1.close();
        }
        dbHelper.close();
        Log.d("endreading","count: " + adapter.getItemCount());

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                adapter.notifyDataSetChanged();
//            }
//        });
    }
}
