package com.me.popmovies;

import android.util.Log;

/**
 * The object that would represent a movie and its data
 */
public class Movie {

    // The Title of the Movie
    private String mTitle;

    // Release Date of the Movie
    private String mReleaseDate;

    // The whole duration of the movie
    private String mDuration;

    // The Movie rate
    private String mRate;

    // The variable with the story outline
    private String mSummary;

    // The Id of the poster of the image that came from the server
    private String mImageResourceId;


    // The Constructor
    public Movie(String mTitle, String mReleaseDate, String mDuration, String mRate, String mSummary, String mImageResourceId) {
        this.mTitle = mTitle;
        this.mReleaseDate = mReleaseDate;
        this.mDuration = mDuration;
        this.mRate = mRate;
        this.mSummary = mSummary;
        this.mImageResourceId = mImageResourceId;
    }


    /**************
     * Getters
     **************/

    public String getmTitle() {
        return mTitle;
    }

    public String getmReleaseDate() {
        return mReleaseDate;
    }

    public String getmDuration() {
        return mDuration;
    }

    public String getmRate() {
        return mRate;
    }

    public String getmSummary() {
        return mSummary;
    }

    public String getmImageResourceId() {
        return mImageResourceId;
    }


    @Override
    public String toString() {

        String details = (getmTitle() + "&" + getmReleaseDate() + "&" + getmDuration() + "&" + getmRate() + "&" + getmSummary() + "&" + getmImageResourceId());
        Log.i("Movie.java", details);
        return details;
    }
}
