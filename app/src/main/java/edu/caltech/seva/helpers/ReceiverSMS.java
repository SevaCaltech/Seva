package edu.caltech.seva.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import edu.caltech.seva.helpers.DbContract;
import edu.caltech.seva.helpers.DbHelper;

public class ReceiverSMS extends BroadcastReceiver {

    //will only add notifications if its from the server
    private final String SPECIAL_PHONE_NUMBER="37083";

    @Override
    public void onReceive(Context context, Intent intent) {
        processReceive(context, intent);
    }

    //breaks the message into its parts and should sendBroadcast to Notification
    private void processReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String body = "";
        String address = "";

        //when message is received
        if(extras!=null){
            //get content of message
            Object[] smsExtras = (Object[]) extras.get("pdus");

            //read message
            for (int i=0;i<smsExtras.length;i++) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtras[i]);
                body = sms.getMessageBody();
                address = sms.getOriginatingAddress();
                }

            if (address.equals(SPECIAL_PHONE_NUMBER)) {
                String[] message = body.split(",");
                DbHelper dbHelper = new DbHelper(context);
                SQLiteDatabase database =dbHelper.getWritableDatabase();
                dbHelper.saveErrorCode(message[0],message[1],message[2],database);
                dbHelper.close();
            }
            Intent intent1 = new Intent(DbContract.UPDATE_UI_FILTER);
            context.sendBroadcast(intent1);
        }
    }
}
