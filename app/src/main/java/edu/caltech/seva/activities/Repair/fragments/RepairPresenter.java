package edu.caltech.seva.activities.Repair.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import edu.caltech.seva.activities.MainApplication;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.models.RepairStep;

/**
 * Represents the Presenter for the Repair Tab Fragment. Used to provide logic and data objects
 * to the view.
 */
public class RepairPresenter implements RepairContract.Presenter {

    private RepairContract.View view;

    RepairPresenter(RepairContract.View view) {
        this.view = view;
    }

    @Override
    public void loadRepairStep(String repairCode, int stepNum) {
        //load single repair step from local db, add it to adapter in view
        DbHelper dbHelper = new DbHelper(MainApplication.getContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.readStep(database, repairCode, new String[]{Integer.toString(stepNum)});
        String stepPic, stepText, stepInfo, stepSymbol;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
//            stepNum = cursor.getInt(cursor.getColumnIndex(DbContract.STEP_NUM));
            stepInfo = cursor.getString(cursor.getColumnIndex(DbContract.STEP_INFO));
            stepPic = cursor.getString(cursor.getColumnIndex(DbContract.STEP_PIC));
            stepText = cursor.getString(cursor.getColumnIndex(DbContract.STEP_TEXT));
            stepSymbol = cursor.getString(cursor.getColumnIndex(DbContract.STEP_SYMBOL));
//                repairSteps.add(new RepairStep(stepNum, stepPic, stepText, stepInfo, stepSymbol));
            RepairStep repairStep = new RepairStep(stepNum, stepPic, stepText, stepInfo, stepSymbol);
            view.showRepairStep(repairStep);
        }
        cursor.close();
        dbHelper.close();
    }
}
