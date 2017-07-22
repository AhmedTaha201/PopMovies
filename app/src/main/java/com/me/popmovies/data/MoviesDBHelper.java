package com.me.popmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class MoviesDBHelper extends SQLiteOpenHelper {

    String LOG_TAG = getClass().getSimpleName();

    public static final String DATABASE_NAME = "movies.db";

    public static final int DATABASE_VERSION = 1;

    //The SQlLite statement for creating the movies table
    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + MoviesContract.TABLE_NAME + " ("
            + MoviesContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MoviesContract.COLUMN_TITLE + " TEXT NOT NULL, "
            + MoviesContract.COLUMN_YEAR + " STRING, "
            + MoviesContract.COLUMN_RATE + " STRING, "
            + MoviesContract.COLUMN_DURATION + " STRING, "
            + MoviesContract.COLUMN_OVERVIEW + " STRING, "
            + MoviesContract.COLUMN_REVIEWS + " STRING, "
            + MoviesContract.COLUMN_MOVIE_ID + " STRING, "
            + MoviesContract.COLUMN_POSTER_ID + " STRING);";



    //The SQlLite statement for deleting the movies table
    public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + MoviesContract.TABLE_NAME;

    public MoviesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "create staement ==> " + SQL_CREATE_TABLE);
        db.execSQL(SQL_CREATE_TABLE);
        Log.d(LOG_TAG, "table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "delete statement ==> " + SQL_DELETE_TABLE);
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
        Log.d(LOG_TAG, "base upgraded");
    }
}
