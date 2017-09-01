package com.example.trio.whatchlist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ASUS on 31/08/2017.
 */

public class FavoriteDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 2;

    public FavoriteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TABLE = "CREATE TABLE "+ FavoriteContract.Entry.TABLE_NAME + " (" +
                FavoriteContract.Entry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoriteContract.Entry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                FavoriteContract.Entry.COLUMN_TITLE + " TEXT, " +
                FavoriteContract.Entry.COLUMN_POSTER + " TEXT, " +
                FavoriteContract.Entry.COLUMN_OVERVIEW + " TEXT, " +
                FavoriteContract.Entry.COLUMN_RATING + " REAL, " +
                FavoriteContract.Entry.COLUMN_RELEASE_DATE + " TEXT, " +
                FavoriteContract.Entry.COLUMN_BACKDROP + " TEXT " +
                "); ";
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ FavoriteContract.Entry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
