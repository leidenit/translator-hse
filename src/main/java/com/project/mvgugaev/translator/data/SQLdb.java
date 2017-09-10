package com.project.mvgugaev.translator.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// It uses for work with default sql database

public class SQLdb extends SQLiteOpenHelper {
    // Db fields/names/requests constants
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME_LHISTORY = "LANG";
    private static final String TABLE_NAME_LHISTORY_TO = "LANG_TO";
    private static final String COLUMN_LANG = "LANG_C";
    private static final String TABLE_NAME_TABS = "TABS";
    private static final String TABLE_NAME_CACHE_TRANSLATE = "CACHE";
    private static final String COLUMN_TEXT_INPUT = "INPUT";
    private static final String COLUMN_TEXT_OUTPUT = "OUTPUT";
    private static final String COLUMN_TEXT_LANG = "LANG";
    private static final String COLUMN_TEXT_TAB_FLAG = "TAB_FLAG";
    private final static String DB_NAME = "store.db";
    private static final String CREATE_TABLE_TAB = "CREATE TABLE " + TABLE_NAME_TABS + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TEXT_INPUT + " TEXT, " + COLUMN_TEXT_OUTPUT + " TEXT," + COLUMN_TEXT_LANG + " TEXT," + COLUMN_TEXT_TAB_FLAG + " INTEGER DEFAULT 1);";
    private static final String CREATE_TABLE_CACHE_TRANSLATE = "CREATE TABLE " + TABLE_NAME_CACHE_TRANSLATE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, TEXT_FROM TEXT,  TEXT_TO TEXT, DICT TEXT, LANG TEXT);";

    private static SQLiteDatabase db;
    private static final String[] tables = {
            TABLE_NAME_TABS,
            TABLE_NAME_LHISTORY,
            TABLE_NAME_LHISTORY_TO,
            TABLE_NAME_CACHE_TRANSLATE
    };
    private static final String[] create_tables = {
            CREATE_TABLE_CACHE_TRANSLATE,
            CREATE_TABLE_TAB,
            createLangHistoryTable(TABLE_NAME_LHISTORY),
            createLangHistoryTable(TABLE_NAME_LHISTORY_TO)
    };

    public SQLdb(Context context, boolean refreshTag) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
        if (refreshTag) {  // Database refresh (uses in debug, not for release)
            dropTable();
            onCreate(db);
        }
    }

    // Get Tabs by flag
    public Cursor getTabsData(String flag) {
        return getDbData(TABLE_NAME_TABS, "TAB_FLAG = " + flag + " OR TAB_FLAG = 3");
    }

    // Custom getter
    private Cursor getDbData(String tableName, String whereClause) {
        return db.query(tableName, null, whereClause, null, null, null, null);
    }

    // Custom create
    private static String createLangHistoryTable(String tableName) {
        return "CREATE TABLE " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_LANG + " TEXT);";
    }

    // Change Tab flag (status)
    public void changeTabStatus(String tabFlag, String id) {
        db.execSQL("UPDATE " + TABLE_NAME_TABS + " SET " + COLUMN_TEXT_TAB_FLAG + " = '" + tabFlag + "' WHERE _id = " + id);
    }

    // Change history of languages
    public Cursor getLangsHistoryData(Boolean part) {
        return getDbData(part ? TABLE_NAME_LHISTORY : TABLE_NAME_LHISTORY_TO, null);
    }

    // Add Tab (flag - key of status)
    public void insertTab(String textFtom, String textTo, String lang, String flag) {
        Cursor cursor = db.rawQuery("SELECT  * FROM " + TABLE_NAME_TABS + " sqlite_sequence", null);

        cursor.moveToLast();
        if (cursor.getCount() >= 1)
            if (!(cursor.getString(1).equals(textFtom) && cursor.getString(2).equals(textTo) && cursor.getString(3).equals(lang))) {
                insertTabFinal(textFtom, textTo, lang, flag, db);
            } else {
                if (!cursor.getString(4).equals(flag)) {
                    changeTabStatus("3", cursor.getString(0));
                }
            } else
            insertTabFinal(textFtom, textTo, lang, flag, db);
        cursor.close();
    }

    // Full Tab clean (if tabs - (save + history) -> make it normal)
    public void deleteAllTabs(String flag) {
        db.delete(TABLE_NAME_TABS, COLUMN_TEXT_TAB_FLAG + "=" + flag, null);
        db.execSQL("UPDATE " + TABLE_NAME_TABS + " SET " + COLUMN_TEXT_TAB_FLAG + " = '" + (flag.equals("2") ? "1" : "2") + "' WHERE " + COLUMN_TEXT_TAB_FLAG + " = 3");
    }

    // Simple insert Tab (final stage)
    private void insertTabFinal(String textFtom, String textTo, String lang, String flag, SQLiteDatabase db) {
        ContentValues newValues = new ContentValues();
        newValues.put(COLUMN_TEXT_LANG, lang);
        newValues.put(COLUMN_TEXT_INPUT, textFtom);
        newValues.put(COLUMN_TEXT_OUTPUT, textTo);
        newValues.put(COLUMN_TEXT_TAB_FLAG, flag);
        db.insert(TABLE_NAME_TABS, null, newValues);
    }

    // Add language history (delete last if count > 5 or move to first if all-list contains same entry)
    public void insertLangHistory(String langKey, Boolean part) {
        String tableName = part ? TABLE_NAME_LHISTORY : TABLE_NAME_LHISTORY_TO;

        Cursor sameObjCursor = getDbData(tableName, COLUMN_LANG +"='" + langKey + "'");
        if (sameObjCursor.getCount() >= 1) {
            sameObjCursor.moveToFirst();
            db.delete(tableName, "_id=" + sameObjCursor.getString(0), null);
        } else {
            Cursor historyCursor = getLangsHistoryData(part);
            historyCursor.moveToFirst();
            if (historyCursor.getCount() >= 5) {
                db.delete(tableName, "_id=" + historyCursor.getString(historyCursor.getColumnIndex("_id")), null);
            }
        }

        ContentValues newValues = new ContentValues();
        newValues.put(COLUMN_LANG, langKey);
        db.insert(tableName, null, newValues);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String item: create_tables) {
            db.execSQL(item);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable();
        onCreate(db);
    }

    // Delete all tables
    private void dropTable() {
        for (String item: tables)
            db.execSQL("DROP TABLE IF EXISTS " + item);
    }
}