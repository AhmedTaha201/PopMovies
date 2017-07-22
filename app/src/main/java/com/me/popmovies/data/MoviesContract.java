package com.me.popmovies.data;

import android.provider.BaseColumns;

/**
 * Created by ASMAA on 07/21/2017.
 * The database would be only one table and that is the favourite movies table
 */

public class MoviesContract implements BaseColumns {

    String LOG_TAG = getClass().getSimpleName();

    //Favourite movies Table name
    public static final String TABLE_NAME = "movies";

    //Column names
    public static final String _ID = BaseColumns._ID;
    //Movie title
    public static final String COLUMN_TITLE = "title";
    //Movie release date
    public static final String COLUMN_YEAR = "wear";
    //Movie average rate
    public static final String COLUMN_RATE = "rate";
    //Movie duration in minutes
    public static final String COLUMN_DURATION = "duration";
    //Movie plot summary
    public static final String COLUMN_OVERVIEW = "overview";
    //Movie reviews
    public static final String COLUMN_REVIEWS = "reviews";
    //The id of the movie at the TMDB.org site used for querying full movie data from the API
    //such as trailers and images
    public static final String COLUMN_MOVIE_ID = "movie_id";
    //Movie poster id
    /* The poster id is stored to load the image without querying
    the full data of the movie to just show it at the main activity
     */
    public static final String COLUMN_POSTER_ID  = "poster_id";
}
