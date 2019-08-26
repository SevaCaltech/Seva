package edu.caltech.seva.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Set;

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;    //shared pref mode
    private static final String PREF_NAME = "seva-preferences";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String TOILETS = "toilets";
    private static final String UID = "uid";
    private static final String IS_GUEST = "isGuest";
    private static final String CURRENT_JOB = "currentJob";
    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void clearPrefs(){
        editor.clear();
        editor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public void setToilets(Set<String> toilets) {
        editor.putStringSet(TOILETS, toilets);
        editor.commit();
    }

    public void setUsername(String username){
        editor.putString(USERNAME, username);
        editor.commit();
    }

    public void setEmail(String email) {
        editor.putString(EMAIL, email);
        editor.commit();
    }

    public void setUid(String uid) {
        editor.putString(UID, uid);
        editor.commit();
    }

    public void setIsGuest(Boolean isGuest){
        editor.putBoolean(IS_GUEST, isGuest);
        editor.commit();
    }

    public void setCurrentJob(String currentJob) {
        editor.putString(CURRENT_JOB, currentJob);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() { return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);}
    public Set<String> getToilets() { return pref.getStringSet(TOILETS,null);}
    public String getUsername() { return pref.getString(USERNAME, "");}
    public String getEmail() { return pref.getString(EMAIL, "");}
    public String getUid() { return pref.getString(UID, "");}
    public Boolean isGuest() { return pref.getBoolean(IS_GUEST,false);}
    public String getCurrentJob() { return pref.getString(CURRENT_JOB, null);}
}
