package edu.caltech.seva.activities.Splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.StartupAuthResult;
import com.amazonaws.mobile.auth.core.StartupAuthResultHandler;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.GetStarted.GetStartedActivity;
import edu.caltech.seva.activities.Login.LoginActivity;
import edu.caltech.seva.activities.Main.MainActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        AWSMobileClient.getInstance().initialize(SplashActivity.this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
                        identityManager.resumeSession(SplashActivity.this, new StartupAuthResultHandler() {
                            @Override
                            public void onComplete(StartupAuthResult authResults) {
                                if (authResults.isUserSignedIn()){
                                    Log.d("log","Refresh Threshold: " + Integer.toString(authResults.getIdentityManager().getUnderlyingProvider().getRefreshThreshold()));
                                    Log.d("log", "Session Duration: " + Integer.toString(authResults.getIdentityManager().getUnderlyingProvider().getSessionDuration()));
                                    Log.d("log", "Session Expiration: " + (authResults.getIdentityManager().getUnderlyingProvider().getSessionCredentitalsExpiration()).toString());
                                    startActivity(new Intent(SplashActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                    overridePendingTransition(0,0);
                                }
                                else{
                                    startActivity(new Intent(SplashActivity.this, GetStartedActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                    overridePendingTransition(0,0);
                                }
                            }
                        }, 3000);
                    }
                }, 2000);
            }
        }).execute();
    }
}
