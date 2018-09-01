package edu.caltech.seva.models;

import android.app.Application;

import java.util.ArrayList;

public class UserData extends Application {
    public ArrayList<String> toilets;
    public boolean isFirstTime;
    public UserData() {
        toilets = new ArrayList<>();
        isFirstTime = true;
    }
}
