package edu.caltech.seva.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.DbContract;

public class DbHelper extends SQLiteAssetHelper{

    private static final String DATABASE_NAME = "sevaDb.db";
    private static final int DATABASE_VERSION = 1;
    private Context context;

    public DbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);

    }

    public void saveErrorCode(String errorCode,String toiletId,String date, SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ERROR_CODE,errorCode);
        contentValues.put(DbContract.NOTIFY_DATE,date);
        contentValues.put(DbContract.TOILET_ID,toiletId);
        database.insert(DbContract.NOTIFY_TABLE,null,contentValues);
    }

    public Cursor readErrorCode(SQLiteDatabase database) {
        String[] projection = {"id",DbContract.ERROR_CODE,DbContract.TOILET_ID,DbContract.NOTIFY_DATE};
        return (database.query(DbContract.NOTIFY_TABLE,projection,null,null,null,null,null));
    }

    public void deleteErrorCodeId(int id, SQLiteDatabase database) {
        String selection = "id = "+id;
        database.delete(DbContract.NOTIFY_TABLE,selection,null);
    }

    public Cursor readRepairCode(SQLiteDatabase database, String errorCode){
        String[] projection = {"*"};
        String selection = DbContract.ERROR_CODE+" = '"+errorCode+"'";
        return (database.query(DbContract.REPAIR_LOOKUP_TABLE,projection,selection,null,null,null,null));
    }

    public Cursor readRepairInfo(SQLiteDatabase database, String repairCode){
        String[] projection = {"*"};
        String selection = DbContract.REPAIR_CODE+" = '"+repairCode+"'";
        return (database.query(DbContract.INFO_TABLE,projection,selection,null,null,null,null));
    }

    public Cursor readToiletInfo(SQLiteDatabase database, String toiletId){
        String[] projection = {"*"};
        String selection = DbContract.TOILET_ID+" = '"+toiletId+"'";
        return (database.query(DbContract.TOILET_INFO_TABLE,projection,selection,null,null,null,null));
    }

    public Cursor readStep(SQLiteDatabase database, String repairCode){
        String[] projection = {DbContract.STEP_NUM,DbContract.STEP_PIC,DbContract.STEP_TEXT,DbContract.STEP_INFO,DbContract.STEP_SYMBOL};
        return (database.query("'"+DbContract.REPAIR_TABLE+repairCode+"'",projection,null,null,null,null,null));
    }

    public static Cursor readStepCount(SQLiteDatabase database, String errorCode){
        String query = "SELECT * FROM '" + DbContract.REPAIR_TABLE+errorCode + "'";
        return(database.rawQuery(query,null));
    }

    public void deleteError(String errorCode, String toiletId, SQLiteDatabase database){
        String selection = DbContract.ERROR_CODE + "  = '" + errorCode + "' AND " + DbContract.TOILET_ID + " = '" + toiletId + "'";
        database.delete(DbContract.NOTIFY_TABLE,selection,null);
   }

   public void clearNotifications(SQLiteDatabase database){
        String clearDBQuery = "DELETE FROM " + DbContract.NOTIFY_TABLE;
        database.execSQL(clearDBQuery);
   }

}
