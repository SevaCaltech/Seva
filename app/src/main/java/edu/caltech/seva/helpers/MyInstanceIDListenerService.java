package edu.caltech.seva.helpers;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Listener Service that saves the Firebase Cloud Messaging Token to SharedPreferences when it is
 * refreshed. This event occurs on user logout, or application install.
 */
public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("FirebaseInstIDListener", "Refreshed token: " + refreshedToken);

        PrefManager prefManager = new PrefManager(getApplicationContext());
        prefManager.setDeviceToken(refreshedToken);
    }
}
