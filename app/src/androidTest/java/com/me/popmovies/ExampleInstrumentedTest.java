package com.me.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.me.popmovies.data.MoviesContract;
import com.me.popmovies.data.MoviesDBHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.me.popmovies", appContext.getPackageName());
    }

    @Test
    public void insert_query() throws Exception {

        MoviesDBHelper dbHelper = new MoviesDBHelper(InstrumentationRegistry.getTargetContext());

        ContentValues cv = new ContentValues();
        cv.put(MoviesContract.COLUMN_TITLE, "spider");
        cv.put(MoviesContract.COLUMN_YEAR, "2017");
        cv.put(MoviesContract.COLUMN_RATE, "7.5");
        cv.put(MoviesContract.COLUMN_DURATION, "145min");
        cv.put(MoviesContract.COLUMN_OVERVIEW, "bla bla bla !");
        cv.put(MoviesContract.COLUMN_MOVIE_ID, "321612");
        cv.put(MoviesContract.COLUMN_POSTER_ID, "/sd45fd2f12sdf");

        assertNotEquals(null, cv);

        long id = dbHelper.getWritableDatabase().insert(MoviesContract.TABLE_NAME, null, cv);

        assertNotEquals(-1, id);

        //query -- wrong if the insertion went wrong
        Cursor data = dbHelper.getReadableDatabase().query(MoviesContract.TABLE_NAME,
                null,
                MoviesContract.COLUMN_MOVIE_ID + "=?",
                new String[]{"321612"},
                null,
                null,
                null);
        //converting the cursor to content values
        ContentValues cv1 = new ContentValues();
        if (data.moveToFirst()) {
            cv1.put(MoviesContract.COLUMN_TITLE, data.getString(data.getColumnIndex(MoviesContract.COLUMN_TITLE)));
            cv1.put(MoviesContract.COLUMN_YEAR, data.getString(data.getColumnIndex(MoviesContract.COLUMN_YEAR)));
            cv1.put(MoviesContract.COLUMN_RATE, data.getString(data.getColumnIndex(MoviesContract.COLUMN_RATE)));
            cv1.put(MoviesContract.COLUMN_DURATION, data.getString(data.getColumnIndex(MoviesContract.COLUMN_DURATION)));
            cv1.put(MoviesContract.COLUMN_OVERVIEW, data.getString(data.getColumnIndex(MoviesContract.COLUMN_OVERVIEW)));
            cv1.put(MoviesContract.COLUMN_MOVIE_ID, data.getString(data.getColumnIndex(MoviesContract.COLUMN_MOVIE_ID)));
            cv1.put(MoviesContract.COLUMN_POSTER_ID, data.getString(data.getColumnIndex(MoviesContract.COLUMN_POSTER_ID)));

            assertEquals(cv, cv1);
        }

        dbHelper.close();

    }
}
