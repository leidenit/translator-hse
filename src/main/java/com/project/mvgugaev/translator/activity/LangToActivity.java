package com.project.mvgugaev.translator.activity;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.project.mvgugaev.translator.MyApplication;
import com.project.mvgugaev.translator.R;
import com.project.mvgugaev.translator.adapters.LangToAdapter;
import com.project.mvgugaev.translator.data.Parse;
import com.project.mvgugaev.translator.items.Lang;

import java.util.ArrayList;

// Activity for set translate language

public class LangToActivity extends AppCompatActivity {
    protected ListView list;
    protected ArrayList <Lang> Recentlangs;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lang_to);

        // Init Ui elements
        list = (ListView) findViewById(R.id.lvMain);
        emptyView = (TextView) findViewById(R.id.emptyView);

        ArrayList <Lang> langs = new ArrayList<>();

        // Check lang mass
        if(MyApplication.getMainLangs() != null) {
            langs.addAll(MyApplication.getMainLangs());
            emptyView.setHeight(0);
            emptyView.setText("");
        }
        else
        {
            emptyView.setHeight(180);
            emptyView.setText(getString(R.string.empty_connection_lable));
        }

        Cursor dataCursor = MyApplication.getMainDB().getLangsHistoryData(false);
        Recentlangs = Parse.recentLangsGenerator(langs,dataCursor);

        LangToAdapter adapter = new LangToAdapter(this, Parse.langListGenerator(langs, Recentlangs, dataCursor));
        list.setAdapter(adapter);

        // Language click event listener
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Lang selected = (Lang) parent.getAdapter().getItem(position);
                MyApplication.setLangTo(selected);
                MyApplication.getMainDB().insertLangHistory(selected.getTransKey(),false);
                goBackEvent(view);
            }
        });
    }

    // End activity
    public void goBackEvent(View v) { super.onBackPressed(); }
}
