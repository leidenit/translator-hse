package com.project.mvgugaev.translator.adapters;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.project.mvgugaev.translator.MyApplication;
import com.project.mvgugaev.translator.R;
import com.project.mvgugaev.translator.items.Lang;

// Translate language adapter (use in ListView)

public class LangToAdapter extends ArrayAdapter <Lang> {
    private Context parentContext;
    private LayoutInflater lInflater;
    private ArrayList <Lang> objects; // All languages

    public LangToAdapter(Context context, ArrayList <Lang> langs) {
        super(context, R.layout.lang_row, langs);
        parentContext = context;
        objects = langs; // Set transferred languages
        lInflater = (LayoutInflater) parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Lang getItem(int position) {
        return objects.get(position); // Override get item method uses Lang objects
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Lang item = getItem(position); // Get Lang object by position
        View rowView; // Create empty View
        if (item.getTransKey().equals("full_h")) {
            rowView = lInflater.inflate(R.layout.lang_header, parent, false);
            ((TextView) rowView.findViewById(R.id.head_text)).setText(item.getLableText());
            rowView.setEnabled(false); // Set header disable
            rowView.setOnClickListener(null); // Block header click
        } else {
            // Choose a template depending on selected language
            rowView = lInflater.inflate(MyApplication.getLangTo().equals(item) ? R.layout.lang_row_picked : R.layout.lang_row, parent, false);
            ((TextView) rowView.findViewById(R.id.lang_text)).setText(item.getLableText());
        }
        return rowView;
    }
}