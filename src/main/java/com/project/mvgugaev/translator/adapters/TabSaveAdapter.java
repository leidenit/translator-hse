package com.project.mvgugaev.translator.adapters;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.project.mvgugaev.translator.activity.MainActivity;
import com.project.mvgugaev.translator.MyApplication;
import com.project.mvgugaev.translator.R;
import com.project.mvgugaev.translator.items.Tab;

// Saved tabs adapter (use in ListView)

public class TabSaveAdapter extends ArrayAdapter <Tab> implements View.OnClickListener, Filterable {
    private ArrayList <Tab> defaultState = new ArrayList <>(); // All tabs
    private Context parentContext;
    private LayoutInflater lInflater;
    private ArrayList <Tab> objects;
    private SaveFilter filter;

    public TabSaveAdapter(Context context, ArrayList <Tab> tabs) {
        super(context, R.layout.tab_row, tabs);
        parentContext = context;
        objects = tabs;
        Collections.reverse(objects);
        defaultState.addAll(objects); // All default state
        lInflater = (LayoutInflater) parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Tab getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onClick(View v) {
        Tab p = getProduct((Integer) v.getTag());
        MyApplication.setLastText(p.gettFrom()); // Set translate text
        MyApplication.setTransactionFlag(true);  // Allow to use transaction
        ((MainActivity) parentContext).setTransProfile(p); //Set profile and go to translate
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = lInflater.inflate(R.layout.tab_row, parent, false); //Set view with template
        final Button tabButton = ((Button) rowView.findViewById(R.id.tab_btn)); //Left-side tab button
        LinearLayout layout = ((LinearLayout) rowView.findViewById(R.id.click_layout));
        Tab item = getProduct(position);

        tabButton.setBackgroundResource(R.drawable.ic_to_tabs_picked); //Set default saved tab template

        // Set data to template elements
        ((TextView) rowView.findViewById(R.id.text_from)).setText(item.gettFrom().length() > 15 ? item.gettFrom().substring(0, 15) + ".." : item.gettFrom());
        ((TextView) rowView.findViewById(R.id.text_to)).setText(item.gettTo().length() > 15 ? item.gettTo().substring(0, 15) + ".." : item.gettTo());
        ((TextView) rowView.findViewById(R.id.lang)).setText(item.getLangs());

        rowView.setOnClickListener(this);
        rowView.setTag(position);

        // Set left-side tab button onClick event
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tab object = getItem(position);
                tabButton.setBackgroundResource(object.getFlag().equals("3") ? R.drawable.ic_to_tabs : R.drawable.ic_to_tabs_picked);
                MyApplication.getMainDB().changeTabStatus(object.getFlag().equals("3") ? "1" : "3", object.getId());
                object.setFlag(object.getFlag().equals("3") ? "1" : "3");
            }
        });
        return rowView;
    }

    @Override
    public Filter getFilter() {  // Override default filter
        if (filter == null) {
            filter = new SaveFilter();
        }
        return filter;
    }

    // Filter by textFrom (item.gettFrom() - original text)
    private class SaveFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // Filter logic
            if (constraint == null || constraint.length() == 0) {
                results.values = defaultState;
                results.count = defaultState.size();
            } else {
                ArrayList<Tab> nPlanetList = new ArrayList <> ();

                for (Tab item: defaultState) {
                    if (item.gettFrom().contains(constraint.toString()))
                        nPlanetList.add(item);
                }
                results.values = nPlanetList;
                results.count = nPlanetList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
                objects = (ArrayList <Tab> ) results.values;
                notifyDataSetChanged();
        }
    }

    private Tab getProduct(int position) {
        return getItem(position);
    }
}