package com.project.mvgugaev.translator.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.project.mvgugaev.translator.MyApplication;
import com.project.mvgugaev.translator.R;

// Preferences fragment

public class PreferencesFragment extends Fragment {
    private Switch switchCache;
    private Switch switchDict;

    public PreferencesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_options, container, false);

        // Set up ui elements
        switchCache = (Switch) fragmentView.findViewById(R.id.switch_cache);
        switchDict = (Switch) fragmentView.findViewById(R.id.switch_dict);

        // Set switch checked status
        switchCache.setChecked(MyApplication.getUseCash());
        switchDict.setChecked(MyApplication.getShowDict());

        // Switches event listeners
        switchCache.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyApplication.setUseCash(isChecked);
            }
        });

        switchDict.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyApplication.setShowDict(isChecked);
            }
        });

        return fragmentView;
    }
}