package com.project.mvgugaev.translator.cache;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

// It uses for cache

public class CacheDataBase {

    // Insert translate profile to db
    public static void insertCacheTranslate(SQLiteDatabase db, String tFrom, String tTo, String dict, String lang) {
        // Check same entries
        Cursor sameCacheEntry = getCacheTranslate(db, "TEXT_FROM = ? AND TEXT_TO = ? AND DICT = ? AND LANG = ?", (new String[] { tFrom, tTo, dict, lang }));

        // Delete same entries or delete if cache count > 200 entries
        if (sameCacheEntry.getCount() >= 1) {
            sameCacheEntry.moveToFirst();
            db.delete("CACHE", "_id=" + sameCacheEntry.getString(0), null);
        } else {
            Cursor secEntry = getCacheTranslate(db, null, null);
            if (secEntry.getCount() >= 200) {
                secEntry.moveToFirst();
                db.delete("CACHE", "_id=" + secEntry.getString(0), null);
            }
        }

        ContentValues newValues = new ContentValues(); // Create content pairs
        newValues.put("TEXT_FROM", tFrom);
        newValues.put("TEXT_TO", tTo);
        newValues.put("DICT", dict);
        newValues.put("LANG", lang);
        db.insert("CACHE", null, newValues);
    }

    // Get translate profile from cache
    public static ArrayList <String> getSecCacheTranslate(SQLiteDatabase db, String tFrom, String lang) {
        ArrayList<String> result = new ArrayList<>();

        // Get entry
        Cursor sameCacheEntry = getCacheTranslate(db, "TEXT_FROM = ? AND LANG = ?", (new String[] { tFrom, lang }));
        if (sameCacheEntry.getCount() >= 1) {
            sameCacheEntry.moveToFirst();
            result.add(sameCacheEntry.getString(2));
            result.add(sameCacheEntry.getString(3));
        } else {
            result = null; // Return null if entries array is empty
        }
        return result;
    }

    private static Cursor getCacheTranslate(SQLiteDatabase db, String whereClause, String[] whereArgs) {
        return db.query("CACHE", null, whereClause, whereArgs, null, null, null);
    }
}