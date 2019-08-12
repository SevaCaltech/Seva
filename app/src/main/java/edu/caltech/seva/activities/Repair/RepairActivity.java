package edu.caltech.seva.activities.Repair;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.activities.Repair.fragments.TabFragment;
import edu.caltech.seva.activities.Repair.fragments.TestFragment;
import edu.caltech.seva.activities.Repair.fragments.TitleFragment;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.models.RepairStep;

//TODO: should receive error code and connect to correct repair guides
public class RepairActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar mToolbar;
        TabLayout mTabLayout;
        ViewPager mPager;
        MyPagerAdapter mAdapter;
        String errorCode, repairCode, repairTitle, toolInfo, totalTime, toiletIP, timestamp;
        int totalSteps;

        //receive error code from the notification clicked and passes to fragments
        errorCode = getIntent().getStringExtra("errorCode");
        repairCode = getIntent().getStringExtra("repairCode");
        repairTitle = getIntent().getStringExtra("repairTitle");
        toolInfo = getIntent().getStringExtra("toolInfo");
        totalTime = getIntent().getStringExtra("totalTime");
        totalSteps = getIntent().getIntExtra("totalSteps",0);
        toiletIP = getIntent().getStringExtra("toiletIP");
        timestamp = getIntent().getStringExtra("timestamp");

        //sets up new activity toolbar and tab layout
        setContentView(R.layout.activity_repair);
        mAdapter = new MyPagerAdapter(getSupportFragmentManager(), errorCode, repairCode, repairTitle, toolInfo, totalTime, totalSteps, toiletIP, timestamp);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Repair Guide");
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_repair, menu);
        return true;
    }

    //handles when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.back:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this,intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

//creates a different fragment depending on which position the tab is
class MyPagerAdapter extends FragmentStatePagerAdapter {
    private String errorCode, repairCode, repairTitle, toolInfo, totalTime, toiletIP, timestamp;
    private int totalSteps;

    public MyPagerAdapter(FragmentManager fm, String errorCode, String repairCode, String repairTitle, String toolInfo, String totalTime, int totalSteps, String toiletIP, String timestamp) {
        super(fm);
        this.errorCode = errorCode;
        this.repairCode = repairCode;
        this.repairTitle = repairTitle;
        this.toolInfo = toolInfo;
        this.totalTime = totalTime;
        this.totalSteps = totalSteps;
        this.toiletIP = toiletIP;
        this.timestamp = timestamp;
    }

    //the first fragment is the title page, the last is the test page, the rest are set tab fragments
    @Override
    public Fragment getItem(int position) {

        if (position == 0)
            return TitleFragment.newInstance(repairCode, repairTitle, toolInfo, totalTime, totalSteps);
        if (position == totalSteps+1)
                return TestFragment.newInstance(errorCode, toiletIP, timestamp);
        else
            return TabFragment.newInstance(repairCode,position);
    }

    //gives the total amount of tabs
    @Override
    public int getCount() {
       return totalSteps+2;
    }


    //sets the titles for the tabs in the layout
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position ==0)
            return "Info";
        if (position == totalSteps+1)
            return "Test";
        else
            return "Step " + position;
    }

}