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
import android.widget.Toast;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Repair.adapters.MyPagerAdapter;
import edu.caltech.seva.activities.Repair.fragments.TabFragment;
import edu.caltech.seva.activities.Repair.fragments.TestFragment;
import edu.caltech.seva.activities.Repair.fragments.TitleFragment;
import edu.caltech.seva.models.RepairActivityData;

public class RepairActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar mToolbar;
        TabLayout mTabLayout;
        ViewPager mPager;
        MyPagerAdapter mAdapter;
        RepairActivityData repairData;

        //receive error code from the notification clicked and passes to fragments
        repairData = (RepairActivityData) getIntent().getSerializableExtra("RepairData");

        //sets up new activity toolbar and tab layout
        setContentView(R.layout.activity_repair);
        mAdapter = new MyPagerAdapter(getSupportFragmentManager(),repairData);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
            case R.id.cancel_option:
                Toast.makeText(this,"Cancel selected..", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.help_option:
                Toast.makeText(this,"Help selected..", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.search_option:
                Toast.makeText(this,"Search selected..", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.test_option:
                Toast.makeText(this,"Test selected..", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

