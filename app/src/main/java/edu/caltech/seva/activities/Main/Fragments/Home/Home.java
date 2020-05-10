package edu.caltech.seva.activities.Main.Fragments.Home;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.Fragments.Notifications.Notifications;
import edu.caltech.seva.activities.Main.Fragments.Toilets.Toilets;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.User;

/**
 * Represents the Home fragment that is the default starting page of the app which displays the
 * user info.
 */
public class Home extends Fragment implements View.OnClickListener, HomeContract.View {
    //utility helpers
    private PrefManager prefManager;
    private BroadcastReceiver broadcastReceiver;
    private final String CHANNEL_ID = "seva_notification";
    private final int NOTIFICATION_ID = 1;

    //ui elements
    private ProgressBar progressBar;
    private TextView displayName, subtext, numNotifications, numToilets;
    private CardView toiletCard, notificationCard;

    //presenter
    private HomePresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_home, null);
        displayName = rootView.findViewById(R.id.opName);
        subtext = rootView.findViewById(R.id.opID);
        numNotifications = rootView.findViewById(R.id.numNotifications);
        numToilets = rootView.findViewById(R.id.numToilets);
        progressBar = rootView.findViewById(R.id.spin_kit);
        toiletCard = rootView.findViewById(R.id.toilet_card);
        notificationCard = rootView.findViewById(R.id.notification_card);

        Objects.requireNonNull(getActivity()).setTitle("Home");
        ((MainActivity) Objects.requireNonNull(getActivity())).setCurrentFragmentTag("HOME");
        Log.d("fragment_tag", "current: " + ((MainActivity) Objects.requireNonNull(getActivity())).getCurrentFragmentTag());

        toiletCard.setOnClickListener(this);
        notificationCard.setOnClickListener(this);

        prefManager = new PrefManager(getContext());
        presenter = new HomePresenter(this, prefManager);

        //load userInfo and notifications
        presenter.loadUserInfo();
        presenter.loadNotifications();

        //connects to the ReceiverSMS class that listens for sms notifications from a specific number
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                presenter.loadNotifications();
                displayNotification();
            }
        };
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(DbContract.UPDATE_UI_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        String fragment_tag = "";
        switch (view.getId()) {
            case R.id.toilet_card:
                //launch mytoilets fragment
                fragment = new Toilets();
                fragment_tag = "TOILETS";
                break;
            case R.id.notification_card:
                //launch notifications fragment
                fragment = new Notifications();
                fragment_tag = "NOTIFICATIONS";
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.screen_area, fragment, fragment_tag);
            ft.addToBackStack(null);
            ft.commit();
            ((MainActivity) Objects.requireNonNull(getActivity())).setCurrentFragmentTag(fragment_tag);
            Log.d("fragment_tag", "current: " + ((MainActivity) Objects.requireNonNull(getActivity())).getCurrentFragmentTag());
        }
    }

    @Override
    public void showNumNotifications(int num) {
        numNotifications.setText(Integer.toString(num));
    }

    @Override
    public void showUserInfo(User user) {
        displayName.setText(user.getName());
        numToilets.setText(Integer.toString(user.getNumToilets()));
        subtext.setText(user.getEmail());
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void displayNotification() {
        Context context = getContext();
        Intent intent = new Intent(context, Notifications.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.icon_seva_small);
        builder.setContentTitle("New Repair");
        builder.setContentText("There is a new repair.");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }
}
