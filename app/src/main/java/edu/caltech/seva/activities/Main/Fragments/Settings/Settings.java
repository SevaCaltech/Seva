package edu.caltech.seva.activities.Main.Fragments.Settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Objects;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;

/**
 * The settings fragment which the user can change language/audio settings, and sign out of the app.
 */
public class Settings extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_settings, null);
        getActivity().setTitle("Settings");
        ((MainActivity) Objects.requireNonNull(getActivity())).setCurrentFragmentTag("SETTINGS");

        LinearLayout language = rootView.findViewById(R.id.languageSettings);
        LinearLayout audio = rootView.findViewById(R.id.audioSettings);
        LinearLayout logout = rootView.findViewById(R.id.logout);

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
                fragment = Language.newInstance(0);
                fragment_tag = "written_language";
                break;
            case R.id.audioSettings:
                fragment = Language.newInstance(1);
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
}
