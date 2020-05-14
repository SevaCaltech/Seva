package edu.caltech.seva.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.Arrays;

import edu.caltech.seva.R;
import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.models.ToiletsDO;

public class DbHelper extends SQLiteAssetHelper{

    private static final String DATABASE_NAME = "sevaDb.db";
    private static final int DATABASE_VERSION = 1;
    private Context context;

    public DbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        this.context = context;
    }

    public void saveErrorCode(String errorCode,String toiletIP,String date, SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.ERROR_CODE,errorCode);
        contentValues.put(DbContract.NOTIFY_DATE,date);
        contentValues.put(DbContract.TOILET_IP,toiletIP);
        database.insert(DbContract.NOTIFY_TABLE,null,contentValues);
    }

    public int saveErrorCodeBatch(PaginatedQueryList<ToiletsDO> list, SQLiteDatabase database){
        int numErrors = list.size();
        int numNull = 0;
        database.beginTransaction();
        String sql = "Insert or Replace into " + DbContract.NOTIFY_TABLE + " (" +
                DbContract.ERROR_CODE + ", " + DbContract.NOTIFY_DATE + ", " + DbContract.TOILET_IP
                + ") values (?,?,?)";
        SQLiteStatement insert = database.compileStatement(sql);
        for(ToiletsDO row:list){
            //for now skip null values, probably want to delete in the future
            if (row.getData().get("error") == null){
                numNull += 1;
                continue;
            }
            insert.bindString(1, row.getData().get("error"));
            insert.bindString(2, row.getTimestamp());
            insert.bindString(3, row.getDeviceId().substring(11));
            insert.execute();
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        Log.d("log","Done syncing errors.");
        return (numErrors - numNull);
    }

    public Cursor readErrorCode(SQLiteDatabase database) {
        String[] projection = {DbContract.NOTIFY_ID,DbContract.ERROR_CODE,DbContract.TOILET_IP,DbContract.NOTIFY_DATE};
        return (database.query(DbContract.NOTIFY_TABLE,projection,null,null,null,null,null));
    }

    public void deleteErrorCodeId(int id, SQLiteDatabase database) {
        String selection = DbContract.NOTIFY_ID + " = " + id;
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

    public Cursor readToiletInfo(SQLiteDatabase database, String toiletIP){
        String[] projection = {"*"};
        String selection = DbContract.TOILET_IP+" = '"+toiletIP+"'";
        return (database.query(DbContract.TOILET_INFO_TABLE,projection,selection,null,null,null,null));
    }

    public Cursor readStep(SQLiteDatabase database, String repairCode, String[] stepNum){
        String[] projection = {"*"};
        String selection = DbContract.STEP_NUM+" = "+stepNum[0];
        return (database.query("'"+DbContract.REPAIR_TABLE+repairCode+"'",projection,selection,null,null,null,null));
    }

    public static Cursor readStepCount(SQLiteDatabase database, String errorCode){
        String query = "SELECT * FROM '" + DbContract.REPAIR_TABLE+errorCode + "'";
        return(database.rawQuery(query,null));
    }

    public void deleteError(String errorCode, String toiletIP, SQLiteDatabase database){
        String selection = DbContract.ERROR_CODE + "  = '" + errorCode + "' AND " + DbContract.TOILET_IP + " = '" + toiletIP + "'";
        database.delete(DbContract.NOTIFY_TABLE,selection,null);
   }

   public int readNumToiletErrors(String toiletIP, SQLiteDatabase database) {
        String selection = DbContract.TOILET_IP + " = '" + toiletIP + "'";
        return ((int)DatabaseUtils.queryNumEntries(database,DbContract.NOTIFY_TABLE, selection));
   }

   public void clearNotifications(SQLiteDatabase database){
        String clearDBQuery = "DELETE FROM " + DbContract.NOTIFY_TABLE;
        database.execSQL(clearDBQuery);
   }

}
