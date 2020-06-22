package edu.caltech.seva.activities.Main.Fragments.Settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.adapters.LanguageAdapter;

/**
 * Represents the Settings fragment that allows the user to change written/audio language, and sign
 * out of the app.
 */
public class Language extends Fragment implements SettingsContract.View {
    private static final String SETTINGS_TYPE = "SETTINGS_TYPE";
    private SettingsChoice settings_type;
    private TextView top_text;

    public Language() {
        //should be empty
    }

    /**
     * Static constructor for creating the Language Fragment.
     *
     * @param choice Differentiates the written and audio language fragments
     * @return a Language fragment
     */
    public static Language newInstance(SettingsChoice choice) {
        Language fragment = new Language();
        Bundle bundle = new Bundle();
        bundle.putInt(SETTINGS_TYPE, choice.getValue());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.language_fragment, null);
        top_text = rootView.findViewById(R.id.top_text);
        Button moreButton = rootView.findViewById(R.id.more_button);
        Bundle arguments = getArguments();
        settings_type = arguments.getInt(SETTINGS_TYPE)==0? SettingsChoice.WRITTEN: SettingsChoice.AUDIO;
        switch (settings_type) {
            case WRITTEN:
                showWrittenSettings();
                break;
            case AUDIO:
                showAudioSettings();
                break;
            default:
                throw new RuntimeException("not a valid settings choice");
        }

        final String[] languages = getResources().getStringArray(R.array.languages);
        GridView gridView = rootView.findViewById(R.id.languageGrid);
        final LanguageAdapter adapter = new LanguageAdapter(getContext(), languages);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String lang_selected = languages[i];

                android.support.v7.app.AlertDialog.Builder confirm = new AlertDialog.Builder(getContext());
                confirm.setTitle(lang_selected);
                confirm.setMessage("Confirm " + lang_selected + " as the " + ((settings_type == SettingsChoice.WRITTEN) ? "written " : "audio ") + "language");
                confirm.setNegativeButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(), "Selected " + lang_selected, Toast.LENGTH_SHORT).show();
                    }
                });
                confirm.setPositiveButton("GO BACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                confirm.show();
            }
        });

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "More Languages..", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void showWrittenSettings() {
        getActivity().setTitle("Set Written Language");
        top_text.setText("What language do you \nwant to read?");
    }

    @Override
    public void showAudioSettings() {
        getActivity().setTitle("Set Audio Language");
        top_text.setText("What do you want to hear \naloud for help?");
    }
}
