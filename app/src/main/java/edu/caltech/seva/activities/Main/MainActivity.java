package edu.caltech.seva.activities.Main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.google.gson.Gson;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Login.LoginActivity;
import edu.caltech.seva.activities.Main.Fragments.Home;
import edu.caltech.seva.activities.Main.Fragments.Notifications;
import edu.caltech.seva.activities.Main.Fragments.Settings;
import edu.caltech.seva.activities.Main.Fragments.Toilets;
import edu.caltech.seva.activities.Repair.RepairActivity;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.RepairActivityData;

//This is the main activity for the app which contains the nav drawer and its fragments
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public boolean isConnected;
    FragmentTransaction fragmentTransaction;
    public String currentFragmentTag;
    Toolbar toolbar;
    NavigationView navigationView;
    PrefManager prefManager;
    String[] PERMISSIONS = {
            Manifest.permission.READ_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNetworkState();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(this);

        //sets up toolbar
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Home");

        //sets up nav drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //display home fragment first
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.screen_area, new Home());
        fragmentTransaction.commit();
        navigationView.setCheckedItem(R.id.nav_home);
        currentFragmentTag = "HOME";

        //handle backstack nav highlighting
        this.getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment current = getCurrentFragment();
                if (current instanceof Home || current instanceof Toilets)
                    navigationView.setCheckedItem(R.id.nav_home);
                else if (current instanceof Notifications)
                    navigationView.setCheckedItem(R.id.nav_notifications);
                else if (current instanceof Settings)
                    navigationView.setCheckedItem(R.id.nav_settings);
            }
        });

        checkAppPermissions();
    }

    //if the back button is pressed and the drawer is open it will close
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        Fragment fragment = null;
        String fragment_tag = "";
        switch (item.getItemId()) {
            case R.id.nav_home:
                fragment = new Home();
                fragment_tag = "HOME";
                break;

            case R.id.nav_job:
                Gson gson = new Gson();
                String json = prefManager.getCurrentJob();
                if (json != null){
                    RepairActivityData currentJob = gson.fromJson(json, RepairActivityData.class);
                    Intent intent = new Intent(this, RepairActivity.class);
                    intent.putExtra("RepairData", currentJob);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else
                    Toast.makeText(this, "No current job..", Toast.LENGTH_SHORT).show();
                    item.setChecked(false);
                    navigationView.setCheckedItem(R.id.nav_home);
                break;

            case R.id.nav_notifications:
                fragment = new Notifications();
                fragment_tag = "NOTIFICATIONS";
                break;

            case R.id.nav_settings:
                fragment = new Settings();
                fragment_tag = "SETTINGS";
                break;

            case R.id.nav_help:
                final String helpNumber = "555-555-5555";
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Check app permissions..", Toast.LENGTH_SHORT).show();
                else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + helpNumber));
                    startActivity(callIntent);
                }
                break;
        }

        if (fragment != null && !fragment_tag.equals(currentFragmentTag)) {
            currentFragmentTag = fragment_tag;
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.screen_area, fragment, fragment_tag);
            ft.addToBackStack(null);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Fragment getCurrentFragment() {
        return this.getSupportFragmentManager().findFragmentById(R.id.screen_area);
    }

    public void checkAppPermissions(){
        if (!hasPermissions(this,PERMISSIONS))
            ActivityCompat.requestPermissions(this, PERMISSIONS,1);
    }

    public static boolean hasPermissions(Context context, String... permissions){
        if(context != null && permissions != null){
            for (String permission: permissions){
                if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

    public void logout() {
        IdentityManager.getDefaultIdentityManager().signOut();
        prefManager.clearPrefs();
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.clearNotifications(database);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        dbHelper.close();
        finish();
    }

    public void updateNetworkState() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = (activeNetwork != null && activeNetwork.isConnected());
        if (!isConnected && !prefManager.isGuest()) {
            Toast.makeText(this,"No Network Connection..", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        updateNetworkState();

        //make sure checked item is correct
        Fragment current = getCurrentFragment();
        if (current instanceof Home || current instanceof Toilets)
            navigationView.setCheckedItem(R.id.nav_home);
        else if (current instanceof Notifications)
            navigationView.setCheckedItem(R.id.nav_notifications);
        else if (current instanceof Settings)
            navigationView.setCheckedItem(R.id.nav_settings);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}