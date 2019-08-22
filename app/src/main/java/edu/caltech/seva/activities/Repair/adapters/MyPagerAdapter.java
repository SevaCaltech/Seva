package edu.caltech.seva.activities.Repair.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import edu.caltech.seva.activities.Repair.fragments.TabFragment;
import edu.caltech.seva.activities.Repair.fragments.TestFragment;
import edu.caltech.seva.activities.Repair.fragments.TitleFragment;

//creates a different fragment depending on which position the tab is
public class MyPagerAdapter extends FragmentStatePagerAdapter {
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