package edu.caltech.seva.activities.GetStarted;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Login.LoginActivity;
import edu.caltech.seva.helpers.PrefManager;

public class GetStartedActivity extends AppCompatActivity {
    private ViewPager mPager;
    private MyViewPagerAdapter mAdapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        mToolbar.setTitle("How Seva works");
        mToolbar.setVisibility(View.GONE);
        mPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                showToolbar(position);
            }

            @Override
            public void onPageSelected(int position) {}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    public void showToolbar(int currentPage) {
        if (currentPage == 0)
            mToolbar.setVisibility(View.GONE);
        else
            mToolbar.setVisibility(View.VISIBLE);
    }

    public void setItem(int i) {
        mPager.setCurrentItem(i);
    }

    public void launchLoginScreen() {
        startActivity(new Intent(GetStartedActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    public class MyViewPagerAdapter extends FragmentStatePagerAdapter {
        private int NUM_ITEMS = 4;

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return GetStartedHomeFragment.newInstance();
            else
                return GetStartedPageFragment.newInstance(position);
        }
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }
}


