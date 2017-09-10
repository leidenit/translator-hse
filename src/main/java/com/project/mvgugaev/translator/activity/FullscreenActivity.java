package com.project.mvgugaev.translator.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.project.mvgugaev.translator.R;

// Full screen translated result activity

public class FullscreenActivity extends AppCompatActivity {
    private TextView mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set fullscreen params
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen);
        mContentView = (TextView) findViewById(R.id.fullscreen_content);
        mContentView.setText(getIntent().getStringExtra("fullscreen_text"));

        // Resize text
        if(getIntent().getStringExtra("fullscreen_text").split(" +").length > 10)
        {
            mContentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }
        else if(getIntent().getStringExtra("fullscreen_text").split(" +").length > 5)
        {
            mContentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        }
        hide();
    }

    // Hide android ui elements
    private void hide() {
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    // Close activity
    public void closeFullScreen(View w)
    {
        this.finish();
    }
}
