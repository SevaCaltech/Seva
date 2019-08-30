package edu.caltech.seva.activities.Main.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.activities.Main.adapters.LanguageAdapter;

public class Language extends Fragment {
    private static final String SETTINGS_TYPE = "SETTINGS_TYPE";
    private static final int WRITTEN_SETTINGS = 0;
    private static final int AUDIO_SETTINGS = 1;
    private int settings_type;

    public Language(){

    }

    public static Language newInstance(int settings_type){
        Language fragment = new Language();
        Bundle bundle = new Bundle();
        bundle.putInt(SETTINGS_TYPE, settings_type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.language_fragment,null);
        TextView top_text = (TextView) rootView.findViewById(R.id.top_text);
        Button moreButton = (Button) rootView.findViewById(R.id.more_button);

        Bundle arguments = getArguments();
        settings_type = arguments.getInt(SETTINGS_TYPE);
        if(settings_type == WRITTEN_SETTINGS) {
            getActivity().setTitle("Set Written Language");
            top_text.setText("What language do you \nwant to read?");
        }
        else {
            getActivity().setTitle("Set Audio Language");
            top_text.setText("What do you want to hear \naloud for help?");
        }

        final String[] languages = getResources().getStringArray(R.array.languages);
        GridView gridView = (GridView)rootView.findViewById(R.id.languageGrid);
        final LanguageAdapter adapter = new LanguageAdapter(getContext(),languages);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String lang_selected = languages[i];

                android.support.v7.app.AlertDialog.Builder confirm = new AlertDialog.Builder(getContext());
                confirm.setTitle(lang_selected);
                confirm.setMessage("Confirm " + lang_selected + " as the " + ((settings_type == WRITTEN_SETTINGS)? "written ": "audio ") + "language");
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
}
