package edu.caltech.seva.activities.Main.Fragments.Settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Objects;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.helpers.PrefManager;

/**
 * The settings fragment which the user can change language/audio settings, and sign out of the app.
 */
public class Settings extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SettingsPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_settings, null);
        getActivity().setTitle("Settings");
        ((MainActivity) Objects.requireNonNull(getActivity())).setCurrentFragmentTag("SETTINGS");
        PrefManager prefManager = new PrefManager(getContext());
        presenter = new SettingsPresenter(prefManager);


        LinearLayout language = rootView.findViewById(R.id.languageSettings);
        LinearLayout audio = rootView.findViewById(R.id.audioSettings);
        LinearLayout logout = rootView.findViewById(R.id.logout);
        Switch smsSwitch = rootView.findViewById(R.id.sms_switch);
        Switch pushSwitch = rootView.findViewById(R.id.push_switch);

        smsSwitch.setChecked(prefManager.getSendSms());
        pushSwitch.setChecked(prefManager.getSendPush());

        smsSwitch.setOnCheckedChangeListener(this);
        pushSwitch.setOnCheckedChangeListener(this);
        language.setOnClickListener(this);
        audio.setOnClickListener(this);
        logout.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        String fragment_tag = "";

        switch (view.getId()) {
            case R.id.languageSettings:
                fragment = Language.newInstance(SettingsChoice.WRITTEN);
                fragment_tag = "written_language";
                break;
            case R.id.audioSettings:
                fragment = Language.newInstance(SettingsChoice.AUDIO);
                fragment_tag = "audio_language";
                break;
            case R.id.logout:
                ((MainActivity) getActivity()).logout();
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.screen_area, fragment, fragment_tag);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sms_switch:
                presenter.handleNotificationSettings(SettingsChoice.SMS, isChecked);
                break;
            case R.id.push_switch:
                presenter.handleNotificationSettings(SettingsChoice.PUSH, isChecked);
                break;
        }
    }
}
