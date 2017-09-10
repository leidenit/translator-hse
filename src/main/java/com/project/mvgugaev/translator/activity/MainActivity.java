package com.project.mvgugaev.translator.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ViewFlipper;

import com.project.mvgugaev.translator.MyApplication;
import com.project.mvgugaev.translator.R;
import com.project.mvgugaev.translator.data.SQLdb;
import com.project.mvgugaev.translator.fragments.MainHistoryFragment;
import com.project.mvgugaev.translator.fragments.MainTabListFragment;
import com.project.mvgugaev.translator.fragments.MainTranslateFragment;
import com.project.mvgugaev.translator.items.Tab;

import java.lang.reflect.Field;

// Main activity

public class MainActivity extends FragmentActivity {
    private TextToSpeech ttfObject;
    private ViewFlipper viewFlipper;
    private final int REQUEST_PERMISSION_CODE = 1;

    // Bottom navigation click listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewFlipper.setDisplayedChild(0);
                    break;

                case R.id.navigation_dashboard:
                    viewFlipper.setDisplayedChild(1);
                    getSupportFragmentManager().findFragmentById(R.id.tabs_frsgment).onResume();
                    break;

                case R.id.navigation_notifications:
                    viewFlipper.setDisplayedChild(2);
                    getSupportFragmentManager().findFragmentById(R.id.history_fragment).onResume();
                    break;

                case R.id.navigation_options:
                    viewFlipper.setDisplayedChild(3);
                    hideSoftInput();
                    break;
            }
            MyApplication.setTabNumber(viewFlipper.getDisplayedChild());
            return true;
        }

    };

    // Hide keyboard (uses for options)
    public boolean hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(viewFlipper.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set permission
        setPermission();

        // Set sql db
        MyApplication.setMainDB(new SQLdb(getApplicationContext(),false));

        // Init layout
        setContentView(R.layout.activity_main);

        // Init flipper
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        viewFlipper.setDisplayedChild(MyApplication.getTabNumber());

        // Set bottom navigation
        ((BottomNavigationView) findViewById(R.id.navigation)).setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        disableShiftMode(((BottomNavigationView) findViewById(R.id.navigation)));

        // Set ttf (Text To Speech)
        if(MyApplication.getTtf() == null)
            ttfObject = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    MyApplication.setTextToSpeechReadyFlag(true);
                    MyApplication.setTtf(ttfObject);
                    ((MainTranslateFragment) getSupportFragmentManager().findFragmentById(R.id.translate_fragment)).setVolState();
                }
            }
        });
    }

    // Set translate (use transition)
    public void setTransProfile(Tab object) {
        ((BottomNavigationView) findViewById(R.id.navigation)).setSelectedItemId(R.id.navigation_home);
        MyApplication.getMainDB().insertLangHistory(object.getLangs().replace(" ", "").split("-")[0].toLowerCase(),true);
        MyApplication.getMainDB().insertLangHistory(object.getLangs().replace(" ", "").split("-")[1].toLowerCase(),false);
        ((MainTranslateFragment) getSupportFragmentManager().findFragmentById(R.id.translate_fragment)).transitionRequest();
    }

    // Dialogs generator
    private void deleteDialogGenerator(String Title, String subTitle, final String deleteFlag,final Boolean flag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Title);
        builder.setMessage(subTitle);
        builder.setCancelable(true);
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyApplication.getMainDB().deleteAllTabs(deleteFlag);
                if(flag)
                    ((MainHistoryFragment) getSupportFragmentManager().findFragmentById(R.id.history_fragment)).setListViewData();
                else
                    ((MainTabListFragment) getSupportFragmentManager().findFragmentById(R.id.tabs_frsgment)).setListViewData();

                dialog.dismiss(); // Close dialog
            }
        });

        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() { // Кнопка ОК
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();  // Close dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show(); // Show created dialog
    }

    // Delete history action
    public void historyDeleteAction() {
        deleteDialogGenerator(getString(R.string.history_dialog_header),getString(R.string.dialog_subtitle),"1", true);
    }

    // Delete saved tabs action
    public void tabsDeleteAction() {
        deleteDialogGenerator(getString(R.string.stabs_dialog_header),getString(R.string.dialog_subtitle),"2", false);
    }

    @Override
    public void onResume(){
        super.onResume();
        ((MainTranslateFragment) getSupportFragmentManager().findFragmentById(R.id.translate_fragment)).transitionRequest();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MyApplication.getMainDB().close();
    }

    @Override
    protected void onDestroy() {
        if(ttfObject != null) {
            ttfObject.stop();
            ttfObject.shutdown();
        }
            MyApplication.setTtf(null);
        super.onDestroy();
    }

    // Disable bottom navigation shift mode
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);

                //noinspection RestrictedApi
                item.setShiftingMode(false);

                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }

    // Check (set) permission
    private void setPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE))
                Log.e("Init","Network state permission");
            else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, REQUEST_PERMISSION_CODE);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET))
                Log.e("Init","Network permission");
            else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_PERMISSION_CODE);
    }
}
