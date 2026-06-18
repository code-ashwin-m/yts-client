package com.example.ytsclient.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BookmarkStore extends SQLiteOpenHelper {
    private static final String DB_NAME = "bookmarks.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "bookmarked_movies";
    private static final String COL_MOVIE_ID = "movie_id";
    private static final String COL_CREATED_AT = "created_at";

    public BookmarkStore(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_MOVIE_ID + " INTEGER PRIMARY KEY, " +
                COL_CREATED_AT + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public synchronized void setBookmarked(int movieId, boolean bookmarked) {
        SQLiteDatabase db = getWritableDatabase();
        if (bookmarked) {
            ContentValues values = new ContentValues();
            values.put(COL_MOVIE_ID, movieId);
            values.put(COL_CREATED_AT, System.currentTimeMillis());
            db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } else {
            db.delete(TABLE, COL_MOVIE_ID + " = ?", new String[]{String.valueOf(movieId)});
        }
    }

    public synchronized boolean isBookmarked(int movieId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE, new String[]{COL_MOVIE_ID}, COL_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieId)}, null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    public synchronized List<Integer> allMovieIds() {
        List<Integer> ids = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE, new String[]{COL_MOVIE_ID}, null, null,
                null, null, COL_CREATED_AT + " DESC")) {
            while (cursor.moveToNext()) {
                ids.add(cursor.getInt(0));
            }
        }
        return ids;
    }
}
