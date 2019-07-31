package edu.caltech.seva.activities.Main.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Login.LoginActivity;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.helpers.DbHelper;
import edu.caltech.seva.helpers.PrefManager;

//the settings fragment which the user can change language/audio settings
public class Settings extends Fragment implements View.OnClickListener {

    private PrefManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_settings,null);
        getActivity().setTitle("Settings");
        LinearLayout language = (LinearLayout) rootView.findViewById(R.id.languageSettings);
        LinearLayout audio = (LinearLayout) rootView.findViewById(R.id.audioSettings);
        LinearLayout logout = (LinearLayout) rootView.findViewById(R.id.logout);

        language.setOnClickListener(this);
        audio.setOnClickListener(this);
        logout.setOnClickListener(this);

        prefManager = new PrefManager(getContext());
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.languageSettings:
                LanguageDialog languageDialog = new LanguageDialog();
                languageDialog.show(getFragmentManager(),"language_settings");
                break;
            case R.id.audioSettings:
                languageDialog = new LanguageDialog();
                languageDialog.show(getFragmentManager(),"language_settings");
                break;
            case R.id.logout:
                prefManager.clearPrefs();
                DbHelper dbHelper = new DbHelper(getContext());
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                dbHelper.clearNotifications(database);
                IdentityManager.getDefaultIdentityManager().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
                break;
        }
    }
}
