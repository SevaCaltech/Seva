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
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Login.LoginActivity;
import edu.caltech.seva.activities.Main.Fragments.Home;
import edu.caltech.seva.activities.Main.Fragments.Notifications;
import edu.caltech.seva.activities.Main.Fragments.Settings;
import edu.caltech.seva.activities.Main.Fragments.Toilets;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;

//This is the main activity for the app which contains the nav drawer and its fragments
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public boolean isConnected;
    FragmentTransaction fragmentTransaction;
    String currentFragmentTag;
    Toolbar toolbar;
    NavigationView navigationView;
    PrefManager prefManager;
    String[] PERMISSIONS = {
            Manifest.permission.READ_SMS,
            Manifest.permission.CALL_PHONE
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
                if (current instanceof Home)
                    navigationView.setCheckedItem(R.id.nav_home);
                else if (current instanceof Toilets)
                    navigationView.setCheckedItem(R.id.nav_toilets);
                else if (current instanceof Notifications)
                    navigationView.setCheckedItem(R.id.nav_notifications);
                else
                    navigationView.setCheckedItem(R.id.nav_settings);
            }
        });

        //check app permissions
        for (String permission: PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{permission},0);
        }
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

            case R.id.nav_toilets:
                fragment = new Toilets();
                fragment_tag = "TOILETS";
                break;

            case R.id.nav_notifications:
                fragment = new Notifications();
                fragment_tag = "NOTIFICATIONS";
                break;

            case R.id.nav_settings:
                fragment = new Settings();
                fragment_tag = "SETTINGS";
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

    public void logout(boolean launchGuest) {
        IdentityManager.getDefaultIdentityManager().signOut();

        if(launchGuest) {
            prefManager.setIsGuest(true);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            prefManager.clearPrefs();
            DbHelper dbHelper = new DbHelper(this);
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            dbHelper.clearNotifications(database);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            dbHelper.close();
        }
        finish();
    }

    public void updateNetworkState() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = (activeNetwork != null && activeNetwork.isConnected());
        if (!isConnected && !prefManager.isGuest()) {
            Toast.makeText(this,"No Network Connection..", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        updateNetworkState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}