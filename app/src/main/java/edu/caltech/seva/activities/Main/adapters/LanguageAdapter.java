package edu.caltech.seva.activities.Main.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import edu.caltech.seva.R;

public class LanguageAdapter extends BaseAdapter {

    private final Context context;
    private final String[] languages;

    public LanguageAdapter(Context context, String[] languages){
        this.context = context;
        this.languages = languages;
    }

    @Override
    public int getCount() {
        return languages.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.language_card,null);
        }

        TextView language_text = (TextView) view.findViewById(R.id.language_text);
        language_text.setText(languages[i]);
        return view;
    }
}
