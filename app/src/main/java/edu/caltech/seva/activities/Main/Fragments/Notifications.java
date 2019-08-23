package edu.caltech.seva.activities.Main.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
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
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.IncomingError;
import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.adapters.RecyclerAdapter;
import edu.caltech.seva.activities.Repair.RepairActivity;
import edu.caltech.seva.models.RepairActivityData;
import edu.caltech.seva.models.ToiletsDO;
import it.gmariotti.recyclerview.adapter.AlphaAnimatorAdapter;
import it.gmariotti.recyclerview.adapter.ScaleInAnimatorAdapter;
import it.gmariotti.recyclerview.itemanimator.ScaleInOutItemAnimator;
import it.gmariotti.recyclerview.itemanimator.SlideInOutLeftItemAnimator;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

//The notification fragment lists out notifications
public class Notifications extends Fragment implements RecyclerAdapter.ClickListener, DeleteDialog.DialogData, AdapterView.OnItemSelectedListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter adapter;
    private ArrayList<IncomingError> incomingErrors = new ArrayList<>();
    List<String> saved_toilets = new ArrayList<>();
    DynamoDBMapper dynamoDBMapper;
    private BroadcastReceiver broadcastReceiver;
    private int idToDelete,posToDelete, result;
    private TextToSpeech mTTs;
    public ProgressBar progressBar;
    private PrefManager prefManager;
    private Spinner toilet_spinner, sort_spinner;
    public Animation animation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_notifications, null);
        getActivity().setTitle("Notifications");
        progressBar = (ProgressBar) rootView.findViewById(R.id.notify_progress);
        prefManager = new PrefManager(getContext());

        //sets up swiperefresh
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Loader refresh = new Loader();
                refresh.execute();
            }
        });
        Resources resources = getResources();
        swipeContainer.setColorSchemeColors(resources.getColor(android.R.color.holo_blue_bright),
                resources.getColor(android.R.color.holo_green_light),
                resources.getColor(android.R.color.holo_orange_light),
                resources.getColor(android.R.color.holo_red_light));

        //sets up the recycler list view
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        //sets up adapter, readErrorFromDb should get the notifications that have already been stored in the db
        adapter = new RecyclerAdapter(getContext(), incomingErrors);
        adapter.setClickListener(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(4);
        recyclerView.setNestedScrollingEnabled(false);
        setupAnimationAdapter();

        //set up spinners
        toilet_spinner = (Spinner) rootView.findViewById(R.id.toilet_spinner);
        sort_spinner = (Spinner) rootView.findViewById(R.id.sort_spinner);
        setupFilterSpinners();

        //setup dynamomapper
        if (!prefManager.isGuest() && ((MainActivity)getActivity()).isConnected){
            Log.d("log", "Initializing AWS...");
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

        Loader load = new Loader();
        load.execute();

        //connects to the ReceiverSMS class that listens for sms notifications from a specific number
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Loader load = new Loader();
                load.execute();
                adapter.filter(toilet_spinner.getSelectedItem().toString(),sort_spinner.getSelectedItemPosition());
            }
        };

        return rootView;
    }

    public void setupAnimationAdapter() {
        ScaleInAnimatorAdapter animatorAdapter = new ScaleInAnimatorAdapter<>(adapter, recyclerView);
        recyclerView.setAdapter(animatorAdapter);
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                adapter.removeNotification(posToDelete, idToDelete);
                Toast.makeText(getActivity(), "Notification Removed Successfully..", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    public void setupFilterSpinners() {
        if (!prefManager.isGuest() && saved_toilets.size() == 0)
            saved_toilets.addAll(prefManager.getToilets());

        List<CharSequence> toilet_options = new ArrayList<>();
        toilet_options.add("All");

        //get names of toilets in localdb
        DbHelper dbHelper = new DbHelper(getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        for(int i=0;i<saved_toilets.size();i++) {
            Cursor cursor = dbHelper.readToiletInfo(database, saved_toilets.get(i));
            String toiletName;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                toiletName = cursor.getString(cursor.getColumnIndex(DbContract.TOILET_NAME));
                toilet_options.add(toiletName);
            }
        }
        dbHelper.close();

        //add names to spinner
        ArrayAdapter<CharSequence> sort_adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_types, R.layout.spinner_item_main);
        ArrayAdapter<CharSequence> toilet_adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_item_main,
                toilet_options
        );
        sort_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toilet_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_spinner.setAdapter(sort_adapter);
        toilet_spinner.setAdapter(toilet_adapter);
        sort_spinner.setOnItemSelectedListener(this);
        toilet_spinner.setOnItemSelectedListener(this);
    }

    //will autoupdate the list when a new sms is received
    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(DbContract.UPDATE_UI_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    //will bring up the delete dialog to check if user actually wants to delete the notification item
    @Override
    public void declineClicked(View view, int position, int id) {
        idToDelete = id;
        posToDelete = position;
        Log.d("log", "id: "+ idToDelete + ", pos: " + posToDelete);

        //handles the dialog function
        DeleteDialog dialog = new DeleteDialog();
        dialog.setTargetFragment(this,0);
        dialog.show(getFragmentManager(), "delete_dialog");
    }

    //creates a new activity which is the repair guide and passes the errorCode from the notification to populate guide
    @Override
    public void acceptClicked(View view, IncomingError incomingError) {
        Intent intent = new Intent(getActivity(), RepairActivity.class);
        RepairActivityData data = new RepairActivityData(incomingError.getErrorCode(),
                incomingError.getRepairCode(), incomingError.getRepairTitle(),
                incomingError.getTotalTime(), incomingError.getToolInfo(),
                incomingError.getTotalSteps(), incomingError.getToiletIP(), incomingError.getDate(),
                incomingError.getLat(), incomingError.getLng());

        intent.putExtra("RepairData", data);
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
        dbHelper.deleteErrorCodeId(idToDelete, database);
        dbHelper.close();
        recyclerView.findViewHolderForAdapterPosition(posToDelete).itemView.startAnimation(animation);
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        adapter.filter(toilet_spinner.getSelectedItem().toString(),sort_spinner.getSelectedItemPosition());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    public class Loader extends AsyncTask<Void, Void, String>{
        @Override
        protected void onPreExecute() {
            swipeContainer.setRefreshing(true);
            toilet_spinner.setEnabled(false);
            sort_spinner.setEnabled(false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            //first try to clear and sync localdb with dynamodb
            if (!prefManager.isGuest() &&  ((MainActivity)getActivity()).isConnected) {
                DbHelper dbHelper = new DbHelper(getContext());
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                dbHelper.clearNotifications(database);
                dbHelper.close();
                getErrorsFromDynamo();
            }
            // load local db
            readErrorFromDb();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            adapter.notifyDataSetChanged();
            toilet_spinner.setEnabled(true);
            sort_spinner.setEnabled(true);
            toilet_spinner.setSelection(0);
            sort_spinner.setSelection(0);
            adapter.filter("All",0);
            swipeContainer.setRefreshing(false);
        }
    }

    private void getErrorsFromDynamo(){
        for(String toilet:saved_toilets) {
            int numErrors;
            DynamoDBQueryExpression<ToiletsDO> queryExpression = new DynamoDBQueryExpression<ToiletsDO>()
                    .withHashKeyValues(new ToiletsDO("aws/things/" + toilet))
                    .withConsistentRead(false);
            PaginatedQueryList<ToiletsDO> list = dynamoDBMapper.query(ToiletsDO.class, queryExpression);
            DbHelper dbHelper = new DbHelper(getContext());
            SQLiteDatabase database =dbHelper.getWritableDatabase();
            numErrors = dbHelper.saveErrorCodeBatch(list, database);
            Log.d("log","Toilet: "+toilet + " " + numErrors + " errors.");
        }
    }

    //connects to the db and reads each row into an arraylist populated by IncomingError objects
    private void readErrorFromDb(){

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
    }
}
