package edu.caltech.seva.activities.Repair;

import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Repair.adapters.MyPagerAdapter;
import edu.caltech.seva.activities.Repair.fragments.TabFragment;
import edu.caltech.seva.activities.Repair.fragments.TestFragment;
import edu.caltech.seva.activities.Repair.fragments.TitleFragment;

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

