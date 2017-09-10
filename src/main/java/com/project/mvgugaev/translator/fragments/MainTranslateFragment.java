package com.project.mvgugaev.translator.fragments;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.project.mvgugaev.translator.activity.FullscreenActivity;
import com.project.mvgugaev.translator.activity.LangFromActivity;
import com.project.mvgugaev.translator.activity.LangToActivity;
import com.project.mvgugaev.translator.MyApplication;
import com.project.mvgugaev.translator.R;
import com.project.mvgugaev.translator.YandexApiCore;
import com.project.mvgugaev.translator.cache.CacheDataBase;
import com.project.mvgugaev.translator.data.Parse;
import com.project.mvgugaev.translator.items.Lang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

// Translate fragment (main translate logic)

public class MainTranslateFragment extends Fragment {
    private TextView fullSizeBtn;
    private TextView langFrom;
    private TextView langTo;
    private TextView translateResult;
    private TextView dictResult;
    private Button сleanTextButton;
    private Button changeLanguageButton;
    private Button toTabsBtn;
    private Button shareBtn;
    private Button speakTextFromButton;
    private Button speakTextToButton;
    private EditText inputField;
    private YandexApiCore loadYaApi = new YandexApiCore(); // Core Yandex api translate/dict
    private String mainText = "";

    //Custom event listeners
    private TextWatcher textWatch;

    public MainTranslateFragment() {}

    // Set visibility state of main control components
    private void setControllButtonVisibility(int state) {
        сleanTextButton.setVisibility(state);
        toTabsBtn.setVisibility(state);
        fullSizeBtn.setVisibility(state);
        shareBtn.setVisibility(state);
        speakTextFromButton.setVisibility(state);
        speakTextToButton.setVisibility(state);
    }

    // Set state of speech buttons
    public void setVolState() {
        if(MyApplication.getTextToSpeechReadyFlag() && speakTextFromButton != null && speakTextToButton != null) {
            if (Parse.checkLangTtf(MyApplication.getLangFrom().getTransKey())) {
                speakTextFromButton.setBackgroundResource(R.drawable.ic_volume_on);
                speakTextFromButton.setClickable(true);
            } else {
                speakTextFromButton.setClickable(false);
                speakTextFromButton.setBackgroundResource(R.drawable.ic_volume_off);
            }

            if (Parse.checkLangTtf(MyApplication.getLangTo().getTransKey())) {
                speakTextToButton.setBackgroundResource(R.drawable.ic_volume_on);
                speakTextToButton.setClickable(true);
            } else {
                speakTextToButton.setBackgroundResource(R.drawable.ic_volume_off);
                speakTextToButton.setClickable(false);
            }
        }
    }

    // Set(reset) event listeners
    private void setEventsListeners() {

        // Full screen button event listener
        fullSizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent fullScreenActivity = new Intent(getActivity(), FullscreenActivity.class);
                fullScreenActivity.putExtra("fullscreen_text", translateResult.getText().toString());
                startActivity(fullScreenActivity); // Start full screen activity
            }
        });

        // Clean text button event listener
        сleanTextButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                inputField.setText("");
                dictResult.setText("");
            }
        });

        // Lang from button event listener
        langFrom.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(MyApplication.getMainLangs() == null)
                    (new MainTranslateFragment.GetLangAsyncTask()).execute();

                startActivity(new Intent(getActivity(), LangFromActivity.class));
            }
        });

        // Lang to button event listener
        langTo.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(MyApplication.getMainLangs() == null)
                    (new MainTranslateFragment.GetLangAsyncTask()).execute();

                startActivity(new Intent(getActivity(), LangToActivity.class));
            }
        });

        // Save translate button event listener
        toTabsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                MyApplication.getMainDB().insertTab(inputField.getText().toString(), translateResult.getText().toString(), MyApplication.getStringLangs(), "2");
            }
        });

        // Share button click event listener
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                System.out.println("Share");
                shareTranslate();
            }
        });

        // Swipe button event listener
        changeLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                swipeLang();
            }
        });

        speakTextFromButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Locale currentLocale = new Locale(MyApplication.getLangFrom().getTransKey());
                if(currentLocale != null) {
                    MyApplication.getTtf().setLanguage(currentLocale);
                    MyApplication.getTtf().speak(inputField.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        speakTextToButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Locale currentLocale = new Locale(MyApplication.getLangTo().getTransKey());
                if(currentLocale != null) {
                    MyApplication.getTtf().setLanguage(currentLocale);
                    String result = translateResult.getText().toString();
                    MyApplication.getTtf().speak(result, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        // Main text input reident listener
        inputField.removeTextChangedListener(textWatch);
        inputField.addTextChangedListener(textWatch);

        // Main text input focus event listener
        inputField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!inputField.getText().toString().equals("") && MyApplication.getInternetConnection() && inputField.getText().toString().replace(" ","").length() >= 1)
                        MyApplication.getMainDB().insertTab(inputField.getText().toString(), translateResult.getText().toString(), MyApplication.getStringLangs(), "1");
                }
            }
        });
    }


    private void initEventListeners()
    {
        if(textWatch == null)
            textWatch = new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (inputField.getText().toString().replace(" ","").length() >= 1) {
                        mainText = inputField.getText().toString();
                        (new TranslateTask()).execute();
                        setControllButtonVisibility(View.VISIBLE);
                    } else {
                        setControllButtonVisibility(View.INVISIBLE);
                        translateResult.setText("");
                        dictResult.setText("");
                    }

                    // Set state of speech button
                    setVolState();

                    // Transition
                    if (!MyApplication.getTransactionFlag())
                        MyApplication.setLastText(inputField.getText().toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void afterTextChanged(Editable s) { }
            };
    }

    // Swipe languages
    public void swipeLang() {
        Lang buff = MyApplication.getLangFrom();
        MyApplication.setLangFrom(MyApplication.getLangTo());
        MyApplication.setLangTo(buff);
        setState();
    }

    // Share translate
    public void shareTranslate() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "(" + MyApplication.getLangFrom().getLableText() + ") " + inputField.getText().toString() + " --> "
                + translateResult.getText().toString() + " (" + MyApplication.getLangTo().getLableText() + ")");
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_text)));
    }

    // Init (reinit elements)
    private void initUiElements(View mainView) {
            langFrom = (TextView) mainView.findViewById(R.id.lang_from);
            langTo = (TextView) mainView.findViewById(R.id.lang_to);
            fullSizeBtn = (TextView) mainView.findViewById(R.id.full_size_btn);
            translateResult = (TextView) mainView.findViewById(R.id.trans_result);
            dictResult = (TextView) mainView.findViewById(R.id.dict_result);

            inputField = (EditText) mainView.findViewById(R.id.input_text);
            сleanTextButton = (Button) mainView.findViewById(R.id.clean_btn);
            changeLanguageButton = (Button) mainView.findViewById(R.id.change_lang_btn);
            toTabsBtn = (Button) mainView.findViewById(R.id.to_tabs_btn);
            shareBtn = (Button) mainView.findViewById(R.id.share_button);

            speakTextFromButton = (Button) mainView.findViewById(R.id.vol_from);
            speakTextToButton = (Button) mainView.findViewById(R.id.vol_to);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (MyApplication.getMainLangs() == null)
            (new MainTranslateFragment.GetLangAsyncTask()).execute();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_main_translate, container, false);

        // Init (reinit ui elements)
        initUiElements(mainView);

        // Set control elements visibility
        setControllButtonVisibility(View.INVISIBLE);

        // Init custom listeners objects
        initEventListeners();

        // Set event listeners
        setEventsListeners();
        return mainView;
    }

    // Show transition
    public void transitionRequest()
    {
        MyApplication.setLangState();
        setState();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // Set saved/transition state
    private void setState() {
        langFrom.setText(MyApplication.getLangFrom().getLableText().length() > 12 ? MyApplication.getLangFrom().getLableText().substring(0, 11) + ".." : MyApplication.getLangFrom().getLableText());
        langTo.setText(MyApplication.getLangTo().getLableText().length() > 12 ? MyApplication.getLangTo().getLableText().substring(0, 11) + ".." : MyApplication.getLangTo().getLableText());

        // Set text (depend on transition from another activity(tabs/history))
        if (mainText.equals("") || MyApplication.getTransactionFlag()) {
            inputField.setText(MyApplication.getLastText());
            mainText = inputField.getText().toString();
        } else {
            inputField.setText(mainText);
        }

        // End transition
        MyApplication.setTransactionFlag(false);

        // Set selection to the end
        inputField.setSelection(inputField.getText().length());
    }


    // Async task for download language-list
    private class GetLangAsyncTask extends AsyncTask < Void, Void, Void > {
        @Override
        protected Void doInBackground(Void...params) {
            try {
                loadYaApi.setLangArrayList(getActivity());
                MyApplication.setInternetConnection(true);
            } catch (IOException e) {
                MyApplication.setInternetConnection(false);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            setState();
        }
    }

    // Async task for translate text
    private class TranslateTask extends AsyncTask <Void,Void,Void> {
        private String post_resp = "";
        private String dict_resp = "";
        private SQLiteDatabase readDataBase = MyApplication.getMainDB().getWritableDatabase();
        private String asyncTranslateText;     // Translated text

        @Override
        protected Void doInBackground(Void...params) {
            ArrayList<String> cacheResult = null;
            asyncTranslateText = mainText;
            try {
                cacheResult = CacheDataBase.getSecCacheTranslate(readDataBase, mainText, MyApplication.getLangFrom().getTransKey() + "-" + MyApplication.getLangTo().getTransKey());
            } catch (SQLiteException e) { e.printStackTrace();}
            if (cacheResult == null || !MyApplication.getUseCash()) {
                try {
                    if (asyncTranslateText.replace(" ","").length() >= 1) {
                        this.post_resp = loadYaApi.translateText(mainText, MyApplication.getLangFrom().getTransKey(), MyApplication.getLangTo().getTransKey());
                        if (mainText.split(" +").length <= 3) {
                            this.dict_resp = loadYaApi.getDict(mainText, MyApplication.getLangFrom().getTransKey(), MyApplication.getLangTo().getTransKey());
                        }
                        MyApplication.setInternetConnection(true);
                    }
                } catch (IOException e) {
                    MyApplication.setInternetConnection(false);
                    this.post_resp = "['" + getString(R.string.empty_connection_lable) + "']";
                }
            } else {
                this.post_resp = cacheResult.get(0);
                this.dict_resp = cacheResult.get(1);
                MyApplication.setInternetConnection(true);
            }
            return null;
        }

        // After async work
        @Override
        protected void onPostExecute(Void result) {
            if (asyncTranslateText.replace(" ","").length() >= 1)
                translateResult.setText(this.post_resp.length() >= 4 ? this.post_resp.substring(2, this.post_resp.length() - 2).replace("\\n", "\n") : this.post_resp.replace("\\n", "\n"));

            // Show dict if option is available
            if(MyApplication.getShowDict())
                dictResult.setText(Html.fromHtml(this.dict_resp));
            else
                dictResult.setText("");

            if (MyApplication.getInternetConnection() && MyApplication.getUseCash() && asyncTranslateText.replace(" ","").length() >= 1) {
                CacheDataBase.insertCacheTranslate(readDataBase, asyncTranslateText, this.post_resp, this.dict_resp, MyApplication.getLangFrom().getTransKey() + "-" + MyApplication.getLangTo().getTransKey());
            }
        }

    }
}