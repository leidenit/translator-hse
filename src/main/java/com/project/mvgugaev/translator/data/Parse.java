package com.project.mvgugaev.translator.data;

import android.database.Cursor;

import com.project.mvgugaev.translator.MyApplication;
import com.project.mvgugaev.translator.items.Lang;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

// It uses for generate specific lists and other collections manipulations

public class Parse {
    // Generate Array of resent languages by keys and sort
    public static ArrayList<Lang> recentLangsGenerator(ArrayList<Lang> mainList, Cursor dataCursor) {
        ArrayList<Lang> recentlangs = new ArrayList<>();
        ArrayList<Lang> recentlangsFinal = new ArrayList<>();

        // Generate array
        if(mainList!= null)
            for (Lang object: mainList) {
                dataCursor.moveToFirst();
                while (!dataCursor.isAfterLast()) {
                    if (dataCursor.getString(1).equals(object.getTransKey())) {
                        recentlangs.add(object);
                    }
                    dataCursor.moveToNext();
                }
            }

        dataCursor.moveToFirst();

        // Sort array
        while (!dataCursor.isAfterLast()) {
            for (Lang object: recentlangs) {
                if (dataCursor.getString(1).equals(object.getTransKey())) {
                    recentlangsFinal.add(object);
                }
            }
            dataCursor.moveToNext();
        }
        return recentlangsFinal;
    }

    // Generate Array of languages for languages adapters (contains headers)
    public static ArrayList<Lang> langListGenerator(ArrayList<Lang> mainList, ArrayList<Lang> Recentlangs, Cursor dataCursor) {
        for (Iterator<Lang> item = mainList.iterator(); item.hasNext();) {
            dataCursor.moveToFirst();
            Lang obj = item.next();
            while (!dataCursor.isAfterLast()) {
                if (dataCursor.getString(1).equals(obj.getTransKey())) {
                    item.remove();
                }
                dataCursor.moveToNext();
            }
        }

        // Sort all lang by name
        Collections.sort(mainList, new Comparator<Lang>() {
            @Override
            public int compare(Lang o1, Lang o2) {
                return o1.getLableText().compareTo(o2.getLableText());
            }
        });

        Collections.reverse(Recentlangs); // Reverse recent langs
        if(mainList.size() > 0)
            mainList.add(0, new Lang("Все языки", "full_h")); // Add all language header
        mainList.addAll(0, Recentlangs); // Add recent languages
        if (!Recentlangs.isEmpty())
            mainList.add(0, new Lang("Последние языки", "full_h")); // Add recent languages header

        return mainList;
    }

    // Check language for ttf (Text to speech)
    public static Boolean checkLangTtf(String localeCode) {
        if (MyApplication.getTtf() != null) {
            Locale locale = new Locale(localeCode);
            int result = MyApplication.getTtf().setLanguage(locale);
            return !(result == MyApplication.getTtf().LANG_MISSING_DATA || result == MyApplication.getTtf().LANG_NOT_SUPPORTED);
        }
        return false;
    }
}