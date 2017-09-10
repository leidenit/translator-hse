package com.project.mvgugaev.translator.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.project.mvgugaev.translator.activity.MainActivity;
import com.project.mvgugaev.translator.MyApplication;
import com.project.mvgugaev.translator.R;
import com.project.mvgugaev.translator.adapters.TabSaveAdapter;
import com.project.mvgugaev.translator.items.Tab;
import java.util.ArrayList;

// Saved tabs fragment

public class MainTabListFragment extends Fragment {
    private ArrayList<Tab> tabs = new ArrayList<>();
    private FrameLayout delete_btn;
    private ListView list;
    private TabSaveAdapter adapter;
    private EditText input;

    // Search event listener
    protected TextWatcher yourTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.getFilter().filter(input.getText());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private static View fragmentView;

    public MainTabListFragment() {}

    // Delete saved tabs action
    private void deleteAction() { ((MainActivity) getActivity()).tabsDeleteAction(); }

    // Generate tabs array
    private void fillData(Cursor cursor) {
        tabs.clear();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            tabs.add(new Tab(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
            cursor.moveToNext();
        }
    }

    // Set data to listView (uses TabHistoryAdapter)
    public void setListViewData() {

        //Set tabs if database is active
        if(MyApplication.getMainDB() != null)
            fillData(MyApplication.getMainDB().getTabsData("2"));

        // Create adapter and set to ListView
        adapter = new TabSaveAdapter(fragmentView.getContext(), tabs);
        list.setAdapter(adapter);

        //Filter data
        adapter.getFilter().filter(input.getText());

        // Set visibility of delete button
        delete_btn.setVisibility(tabs.isEmpty() ? View.INVISIBLE:View.VISIBLE);
    }

    // Show transition
    @Override
    public void onResume(){
        super.onResume();
        setListViewData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Create view
        fragmentView = inflater.inflate(R.layout.fragment_main_tabs, container, false);

        // Init element
        list = (ListView) fragmentView.findViewById(R.id.tabs_list);
        delete_btn = (FrameLayout) fragmentView.findViewById(R.id.delete_btn);
        input = ((EditText) fragmentView.findViewById(R.id.editText4));

        // Move mail_layer to front
        fragmentView.findViewById(R.id.main_layer).setZ(10);
        fragmentView.findViewById(R.id.search_layer).setZ(5);

        // Set ListView dat
        setListViewData();

        // Set delete button onClick listener
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                deleteAction();
            }
        });

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Restart search input change event
        input.addTextChangedListener(yourTextWatcher);
    }
}