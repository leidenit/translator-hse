package com.project.mvgugaev.translator;

import android.app.Application;
import android.database.Cursor;
import android.speech.tts.TextToSpeech;

import com.project.mvgugaev.translator.data.Parse;
import com.project.mvgugaev.translator.data.SQLdb;
import com.project.mvgugaev.translator.items.Lang;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

// Store data main class

public class MyApplication extends Application
{
    // Yandex api keys
    private static String yaApi = "trnsl.1.1.20170324T212643Z.15c6734a8167d489.3e99cc10e6d4e57a68732999545faabe54a66034";
    private static String yaDictApi = "dict.1.1.20170406T161015Z.d003f5cc0a54ce61.91bd38d9b1e287ecf10ca2e9af513d3259eb57d1";

    // Yandex api urls
    private static String yaTranslateUrl = "https://translate.yandex.net/api/v1.5/tr.json";
    private static String yaDictUrl = "https://dictionary.yandex.net/api/v1/dicservice.json";

    // State flags
    private static Boolean transactionFlag = false;
    private static Boolean internetConnection = false;
    private static Boolean showDict = true;
    private static Boolean useCash = true;

    //Text to Speech
    private static TextToSpeech ttf;
    private static ArrayList<Locale> avilibleLocale;
    private static Boolean TextToSpeechReadyFlag = false;

    // Langs and text
    private static String lastText = "";
    private static ArrayList<Lang> mainLangs;
    private static Lang langFrom = new Lang("Русский","ru");
    private static Lang langTo = new Lang("Английский","en");

    // Data base
    private static SQLdb mainDB;

    // Active page
    private static Integer tabNumber = 0; //default

    // Getters and setters
    public static Integer getTabNumber() { return tabNumber; }
    public static void setTabNumber(Integer tabNumber) { MyApplication.tabNumber = tabNumber; }

    public static Boolean getTextToSpeechReadyFlag() {
        return TextToSpeechReadyFlag;
    }
    public static void setTextToSpeechReadyFlag(Boolean textToSpeechReadyFlag) { TextToSpeechReadyFlag = textToSpeechReadyFlag;}

    public static ArrayList<Locale> getAvilibleLocale() { return avilibleLocale; }
    public static void setAvilibleLocale(ArrayList<Locale> avilibleLocale) { MyApplication.avilibleLocale = avilibleLocale; }

    public static TextToSpeech getTtf() { return ttf; }
    public static void setTtf(TextToSpeech ttf) { MyApplication.ttf = ttf; }

    public static Boolean getShowDict() { return showDict; }
    public static void setShowDict(Boolean showDict) { MyApplication.showDict = showDict; }

    public static Boolean getUseCash() { return useCash; }
    public static void setUseCash(Boolean useCash) { MyApplication.useCash = useCash; }

    public static Boolean getInternetConnection() { return internetConnection; }
    public static void setInternetConnection(Boolean internetConnection) { MyApplication.internetConnection = internetConnection;}

    public static Boolean getTransactionFlag() { return transactionFlag; }
    public static void setTransactionFlag(Boolean transactionFlag) { MyApplication.transactionFlag = transactionFlag; }

    public static String getLastText() { return lastText; }
    public static void setLastText(String lastText) {
        MyApplication.lastText = lastText;
    }

    public static SQLdb getMainDB() { return mainDB; }
    public static void setMainDB(SQLdb mainDB) { MyApplication.mainDB = mainDB; }

    public static Lang getLangFrom() { return langFrom; }
    public static void setLangFrom(Lang langFrom) { MyApplication.langFrom = langFrom; }

    public static Lang getLangTo() { return langTo;}
    public static void setLangTo(Lang langTo) { MyApplication.langTo = langTo; }

    public static ArrayList<Lang> getMainLangs() { return mainLangs; }
    public static void setMainLangs(ArrayList<Lang> mainLangs) { MyApplication.mainLangs = mainLangs;}

    public static String getYaDictUrl() {return yaDictUrl;}
    public static void setYaDictUrl(String yaDictUrl) {MyApplication.yaDictUrl = yaDictUrl;}

    public static String getYaTranslateUrl() { return yaTranslateUrl; }
    public static void setYaTranslateUrl(String yaTranslateUrl) { MyApplication.yaTranslateUrl = yaTranslateUrl;}

    public static String getYaDictApi() { return yaDictApi; }
    public static void setYaDictApi(String yaDictApi) { MyApplication.yaDictApi = yaDictApi;}

    public static String getYaApi() {
        return yaApi;
    }
    public static void setYaApi(String yaApi) {
        MyApplication.yaApi = yaApi;
    }

    // Get languages combination like: RU - EN
    public static String getStringLangs() {return (langFrom.getTransKey() + " - " + langTo.getTransKey()).toUpperCase();}

    // Find language by key
    public static Lang findLang(String key) {
        if(mainLangs != null)
            for(Lang current : mainLangs) {
                if(current.getTransKey().equals(key)) {
                    return current;
                }
            }
        return new Lang("Русский","ru");
    }

    // Set language state (use db)
    public static void setLangState() {
        if (MyApplication.getMainDB() != null) {
            ArrayList<Lang> upLangs = MyApplication.getMainLangs();
            Cursor dataCursor = MyApplication.getMainDB().getLangsHistoryData(true);
            ArrayList<Lang> Recentlangs = Parse.recentLangsGenerator(upLangs, dataCursor);
            Collections.reverse(Recentlangs);
            if (Recentlangs.size() >= 1)
                MyApplication.setLangFrom(Recentlangs.get(0));

            ArrayList<Lang> upLangs1 = MyApplication.getMainLangs();
            Cursor dataCursor1 = MyApplication.getMainDB().getLangsHistoryData(false);
            ArrayList<Lang> Recentlangs1 = Parse.recentLangsGenerator(upLangs1, dataCursor1);
            Collections.reverse(Recentlangs1);
            if (Recentlangs1.size() >= 1)
                MyApplication.setLangTo(Recentlangs1.get(0));
        }
    }
}