package com.sharelib.hashtaginput.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

public final class DBHelper extends SQLiteOpenHelper {

    /**
     * Constants
     */
    private static final String TAG = DBHelper.class.getSimpleName();

    private static final String DB_NAME = "hash_tag_input_db";
    private static final int DB_VERSION = 1;

    public static final String HASHTAGS_TABLE = "hashtag_table";
    public static final String PREDICTIONS_TABLE = "predictions_table";

    public static final String ID_COL = "_id";
    public static final String HASHTAGS_COL = "hashtags";
    public static final String JID_COL = "jid_col";
    public static final String MID_COL = "mid_col";

    /**
     * Constructor
     */
    DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * SQLiteOpenHelper Methods
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createHashTags = "CREATE TABLE IF  NOT EXISTS " + HASHTAGS_TABLE +
                "(" + ID_COL + " INTEGER PRIMARY KEY, " + HASHTAGS_COL + " TEXT, " +
                JID_COL + " TEXT, " + MID_COL + " TEXT " + ")";
        try {
            db.execSQL(createHashTags);
            Log.i(TAG, "HashTags DB table created");
        } catch (SQLiteException e){
            e.getMessage();
        }

        String createPredictions = "CREATE TABLE IF  NOT EXISTS " + PREDICTIONS_TABLE +
                "(" + ID_COL + " INTEGER PRIMARY KEY, " + HASHTAGS_COL + " TEXT, " +
                MID_COL + " TEXT " + ")";
        try {
            db.execSQL(createPredictions);
            Log.i(TAG, "Predictions DB table create");
        } catch (SQLiteException e){
            e.getMessage();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
