package edu.caltech.seva.activities.Main.Fragments.Notifications;

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
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.gson.Gson;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
import it.gmariotti.recyclerview.adapter.ScaleInAnimatorAdapter;

/**
 * The notification fragment lists out notifications along with some necessary information.
 * Allows the user to accept or decline the repair.
 */
public class Notifications extends Fragment implements RecyclerAdapter.ClickListener, DeleteDialog.DialogData, AdapterView.OnItemSelectedListener, NotificationsContract.View {
    //utility helpers
    private BroadcastReceiver broadcastReceiver;
    private PrefManager prefManager;

    //ui elements
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerAdapter adapter;
    private TextToSpeech mTTs;
    public ProgressBar progressBar;
    private Spinner toilet_spinner, sort_spinner;
    public Animation animation;

    //data objects
    private int idToDelete, posToDelete, result;

    //presenter
    private NotificationsPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_notifications, null);
        getActivity().setTitle("Notifications");
        ((MainActivity) Objects.requireNonNull(getActivity())).setCurrentFragmentTag("NOTIFICATIONS");

        progressBar = rootView.findViewById(R.id.notify_progress);
        prefManager = new PrefManager(getContext());
        presenter = new NotificationsPresenter(this, prefManager);

        //sets up swiperefresh
        swipeContainer = rootView.findViewById(R.id.swipeContainer);
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
        recyclerView = rootView.findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        //sets up adapter, readErrorFromDb should get the notifications that have already been stored in the db
        adapter = new RecyclerAdapter(getContext());
        adapter.setClickListener(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(4);
        recyclerView.setNestedScrollingEnabled(false);
        setupAnimationAdapter();

        //set up spinners
        toilet_spinner = rootView.findViewById(R.id.toilet_spinner);
        sort_spinner = rootView.findViewById(R.id.sort_spinner);
        presenter.loadToiletNames();

        Loader load = new Loader();
        load.execute();

        //connects to the ReceiverSMS class that listens for sms notifications from a specific number
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Loader load = new Loader();
                load.execute();
                adapter.filter(toilet_spinner.getSelectedItem().toString(), sort_spinner.getSelectedItemPosition());
            }
        };

        return rootView;
    }

    @Override
    public void addToiletNamesToSpinner(List<CharSequence> toiletNames) {
        ArrayAdapter<CharSequence> sort_adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_types, R.layout.spinner_item_main);
        ArrayAdapter<CharSequence> toilet_adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_item_main,
                toiletNames
        );
        sort_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toilet_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_spinner.setAdapter(sort_adapter);
        toilet_spinner.setAdapter(toilet_adapter);
        sort_spinner.setOnItemSelectedListener(this);
        toilet_spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void addErrorToAdapter(IncomingError error) {
        adapter.add(error);
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
        Log.d("log", "id: " + idToDelete + ", pos: " + posToDelete);

        //handles the dialog function
        DeleteDialog dialog = new DeleteDialog();
        dialog.setTargetFragment(this, 0);
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
        Gson gson = new Gson();
        String json = gson.toJson(data);
        prefManager.setCurrentJob(json);
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
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(msg.format(args)));
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
                if (status == TextToSpeech.SUCCESS)
                    result = mTTs.setLanguage(Locale.US);
                else
                    Toast.makeText(getActivity(), "Feature not supported in your device..", Toast.LENGTH_SHORT).show();
                if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA)
                    Toast.makeText(getActivity(), "Feature not supported in your device..", Toast.LENGTH_SHORT).show();
                else
                    mTTs.speak(errorCode, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTTs != null) {
            mTTs.stop();
            mTTs.shutdown();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        adapter.filter(toilet_spinner.getSelectedItem().toString(), sort_spinner.getSelectedItemPosition());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void setupAnimationAdapter() {
        ScaleInAnimatorAdapter animatorAdapter = new ScaleInAnimatorAdapter<>(adapter, recyclerView);
        recyclerView.setAdapter(animatorAdapter);
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                adapter.removeNotification(posToDelete, idToDelete);
                Toast.makeText(getActivity(), "Notification Removed Successfully..", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private class Loader extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            swipeContainer.setRefreshing(true);
            toilet_spinner.setEnabled(false);
            sort_spinner.setEnabled(false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            //first try to clear and sync localdb with dynamodb
            if (!prefManager.isGuest() && ((MainActivity) getActivity()).isConnected) {
                presenter.loadErrors();
            }
            // load local db
            adapter.clearAdapter();
            presenter.loadErrorInfo();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            adapter.notifyDataSetChanged();
            toilet_spinner.setEnabled(true);
            sort_spinner.setEnabled(true);
            toilet_spinner.setSelection(0);
            sort_spinner.setSelection(0);
            adapter.filter("All", 0);
            swipeContainer.setRefreshing(false);
        }
    }
}
