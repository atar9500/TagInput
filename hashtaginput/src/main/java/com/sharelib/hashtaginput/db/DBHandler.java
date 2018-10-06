package com.sharelib.hashtaginput.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sharelib.hashtaginput.structure.HashTag;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DBHandler {

    /**
     * Instance
     */
    private static DBHandler mHandler;

    public static DBHandler getInstance(Context context) {
        if (mHandler == null) {
            mHandler = new DBHandler(context);
        }
        return mHandler;
    }

    /**
     * Handler
     */
    private DBHelper mDBHelper;

    /**
     * Constructor
     */
    private DBHandler(Context context) {
        mDBHelper = new DBHelper(context.getApplicationContext());
    }

    /**
     * DBHandler Methods
     */
    public void syncHashTags(String jid, String mid, List<HashTag> hashTags) {
        // First, we update the timestamp to all the hashtags and sort them by timestamp
        for (HashTag hashTag : hashTags) {
            hashTag.setTimestamp(System.currentTimeMillis());
        }
        sortHashTags(hashTags);

        // Third, we try to update the row with the same jid and mid
        ContentValues htValues = new ContentValues();
        htValues.put(DBHelper.HASHTAGS_COL, hashTagsToJson(hashTags));
        String htSelection = DBHelper.JID_COL + " = ? AND " + DBHelper.MID_COL + " = ?";
        String[] htSelectionArgs = new String[]{jid, mid};
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            int affectedRows = db.update(DBHelper.HASHTAGS_TABLE, htValues,
                    htSelection, htSelectionArgs);

            // If we couldn't find a row with the same jid and mid, we'll add
            // the rest of the values and create a new one
            if (affectedRows == 0) {
                htValues.put(DBHelper.JID_COL, jid);
                htValues.put(DBHelper.MID_COL, mid);
                db.insertOrThrow(DBHelper.HASHTAGS_TABLE, null, htValues);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        // After updating the HashTags DB, we'll need to do the same with Predictions DB

        // First, we'll get the existing HashTags and update our new HashTags
        List<HashTag> existingHashTags = getHashTags(DBHelper.PREDICTIONS_TABLE, jid, null);
        if (existingHashTags != null) {
            for (HashTag existHashTag : existingHashTags) {
                for (HashTag newHashTag : hashTags) {
                    if (existHashTag.equals(newHashTag)) {
                        existingHashTags.remove(existHashTag);
                    }
                }
            }
            hashTags.addAll(existingHashTags);
        }
        sortHashTags(hashTags);

        // Second, we try to update the row with the same jid
        ContentValues predValues = new ContentValues();
        predValues.put(DBHelper.PREDICTIONS_TABLE, hashTagsToJson(hashTags));
        String predSelection = DBHelper.JID_COL + " = ?";
        String[] predSelectionArgs = new String[]{jid};
        try {
            int affectedRows = db.update(DBHelper.PREDICTIONS_TABLE, predValues,
                    predSelection, predSelectionArgs);
            if (affectedRows == 0) {
                htValues.put(DBHelper.JID_COL, jid);
                db.insertOrThrow(DBHelper.PREDICTIONS_TABLE, null, htValues);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public List<HashTag> getHashTags(String table, String jid, String mid) {
        StringBuilder selection = new StringBuilder(DBHelper.JID_COL + " = ?");
        if (mid != null) {
            selection.append(" AND " + DBHelper.MID_COL + " = ?");
        }
        List<String> selectionArgsBuilder = new ArrayList<>();
        selectionArgsBuilder.add(jid);
        if (mid != null) {
            selectionArgsBuilder.add(mid);
        }
        String[] selectionArgs = selectionArgsBuilder.toArray(new String[0]);
        String[] projection = new String[]{DBHelper.HASHTAGS_COL};
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.query(table, projection, selection.toString(),
                selectionArgs, null, null, Integer.toString(1));
        try {
            if (cursor != null && cursor.moveToFirst()) {
                String hashTagsJson = cursor.getString(cursor.getColumnIndex(DBHelper.HASHTAGS_COL));
                return jsonToHashTags(hashTagsJson);
            } else {
                return null;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    private String hashTagsToJson(List<HashTag> hashTags) {
        return new Gson().toJson(hashTags);
    }

    private ArrayList<HashTag> jsonToHashTags(String hashTagsJson) {
        Type type = new TypeToken<ArrayList<HashTag>>() {
        }.getType();
        ArrayList<HashTag> hashTags = new Gson().fromJson(hashTagsJson, type);
        sortHashTags(hashTags);
        return hashTags;
    }

    private void sortHashTags(List<HashTag> hashTags) {
        Collections.sort(hashTags, new Comparator<HashTag>() {
            @Override
            public int compare(HashTag o1, HashTag o2) {
                Long ts1 = o1.getTimestamp();
                Long ts2 = o2.getTimestamp();
                return ts1.compareTo(ts2) * -1;
            }
        });
    }

}
