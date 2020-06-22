package edu.caltech.seva.helpers;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("FirebaseInstIDListener", "Refreshed token: " + refreshedToken);

        PrefManager prefManager = new PrefManager(getApplicationContext());
        prefManager.setDeviceToken(refreshedToken);
    }
}
