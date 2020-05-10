package edu.caltech.seva.activities.Repair.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import edu.caltech.seva.activities.Repair.fragments.TabFragment;
import edu.caltech.seva.activities.Repair.fragments.TestFragment;
import edu.caltech.seva.activities.Repair.fragments.TitleFragment;
import edu.caltech.seva.models.RepairActivityData;

//creates a different fragment depending on which position the tab is
public class MyPagerAdapter extends FragmentStatePagerAdapter {
    private RepairActivityData repairData;

    public MyPagerAdapter(FragmentManager fm, RepairActivityData repairData) {
        super(fm);
        this.repairData = repairData;
    }

    //the first fragment is the title page, the last is the test page, the rest are set tab fragments
    @Override
    public Fragment getItem(int position) {

        if (position == 0)
            return TitleFragment.newInstance(repairData.getRepairCode(), repairData.getRepairTitle(),
                    repairData.getToolInfo(), repairData.getTotalTime(), repairData.getTotalSteps(),
                    repairData.getLat(), repairData.getLng());
        if (position == repairData.getTotalSteps()+1)
            return TestFragment.newInstance(repairData.getErrorCode(), repairData.getToiletIP(),
                    repairData.getTimestamp());
        else
            return TabFragment.newInstance(repairData.getRepairCode(),position,
                    repairData.getToiletIP(), repairData.getErrorCode(), repairData.getTotalSteps());
    }

    //gives the total amount of tabs
    @Override
    public int getCount() {
        return repairData.getTotalSteps()+2;
    }


    //sets the titles for the tabs in the layout
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position ==0)
            return "Info";
        if (position == repairData.getTotalSteps()+1)
            return "Test";
        else
            return "Step " + position;
    }
}