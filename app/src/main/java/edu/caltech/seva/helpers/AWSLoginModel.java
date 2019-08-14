package edu.caltech.seva.helpers;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.regions.Regions;

import org.json.JSONException;
import org.json.JSONObject;


public class AWSLoginModel {
    //constants
    private final String ATTR_EMAIL = "email";
    private final String ATTR_SUB = "sub";

    //interface handler
    private AWSLoginHandler mCallback;

    // control variables
    private String userName, userPassword;
    private Context mContext;
    private CognitoUserPool mCognitoUserPool;
    private CognitoUser mCognitoUser;
    private PrefManager prefManager;

    private final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            mCognitoUser = mCognitoUserPool.getCurrentUser();
            mCognitoUser.getDetailsInBackground(new GetDetailsHandler() {
                @Override
                public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                    //Save username and email in preferences
                    String email = cognitoUserDetails.getAttributes().getAttributes().get(ATTR_EMAIL);
                    String uid = cognitoUserDetails.getAttributes().getAttributes().get(ATTR_SUB);
                    String userName = mCognitoUser.getUserId();
                    prefManager.setEmail(email);
                    prefManager.setUsername(userName);
                    prefManager.setUid(uid);
                    prefManager.setIsGuest(false);
                    mCallback.onSignInSuccess();
                }

                @Override
                public void onFailure(Exception exception) {
                    exception.printStackTrace();
                }
            });
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            final AuthenticationDetails authenticationDetails = new AuthenticationDetails(userName, userPassword, null);
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            authenticationContinuation.continueTask();
//            userPassword = "";
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {}
        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            //I have this just for now
            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                NewPasswordContinuation newPasswordContinuation = (NewPasswordContinuation) continuation;
                newPasswordContinuation.setPassword(userPassword);
                newPasswordContinuation.continueTask();
            }
            userPassword = "";
            continuation.continueTask();
        }

        @Override
        public void onFailure(Exception exception) {
            mCallback.onFailure(exception);
        }
    };

    public AWSLoginModel(Context context, AWSLoginHandler callback) {
        mContext = context;
        mCallback = callback;
        prefManager = new PrefManager(mContext);
        IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
        try{
            JSONObject myJSON = identityManager.getConfiguration().optJsonObject("CognitoUserPool");
            final String COGNITO_POOL_ID = myJSON.getString("PoolId");
            final String COGNITO_CLIENT_ID = myJSON.getString("AppClientId");
            final String COGNITO_CLIENT_SECRET = myJSON.getString("AppClientSecret");
            final String REGION = myJSON.getString("Region");
            mCognitoUserPool = new CognitoUserPool(context, COGNITO_POOL_ID, COGNITO_CLIENT_ID, COGNITO_CLIENT_SECRET, Regions.fromName(REGION));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void signInUser(String userName, String userPassword){
        this.userName = userName;
        this.userPassword = userPassword;
        mCognitoUser = mCognitoUserPool.getUser(userName);
        mCognitoUser.getSessionInBackground(authenticationHandler);
    }

    public void signInGuest() {
        this.userName = "Guest";
        this.userPassword = "";
        prefManager.setUsername(userName);
        prefManager.setIsGuest(true);
        mCallback.onSignInSuccess();
    }
}
