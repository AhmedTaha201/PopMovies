package com.me.popmovies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * The object that would represent a movie and its data
 */
public class Movie implements Serializable {

    // The Title of the Movie
    private String mTitle;

    //The id of the movie from the API
    private String mMovieID;

    // Release Date of the Movie
    private String mReleaseDate;

    // The whole duration of the movie
    private String mDuration;

    // The Movie rate
    private String mRate;

    //The movie genre IDs
    private String mGenres;

    //The movie rating
    private String mRated;

    // The variable with the story outline
    private String mSummary;

    // The Id of the poster of the image that came from the server
    private String mImageResourceId;

    //The id of the backdrop that came from the server
    private String mBackDropResourceId;

    //Trailer list
    private List<Trailer> mTrailers;

    //Reviews string
    private String mReviews;


    // The Constructor
    public Movie(String mTitle,String mMovieID, String mReleaseDate, String mDuration,
                 String mRate,String mGenres, String mRated, String mSummary, String mImageResourceId,String mBackDropResourceId, List<Trailer> mTrailers, String mReviews) {
        this.mTitle = mTitle;
        this.mMovieID = mMovieID;
        this.mReleaseDate = mReleaseDate;
        this.mDuration = mDuration;
        this.mRate = mRate;
        this.mGenres = mGenres;
        this.mRated = mRated;
        this.mSummary = mSummary;
        this.mImageResourceId = mImageResourceId;
        this.mBackDropResourceId = mBackDropResourceId;
        this.mTrailers = mTrailers;
        this.mReviews = mReviews;
    }


    /**************
     * Getters
     **************/

    public String getmTitle() {
        return mTitle;
    }

    public String getmMovieID() {return this.mMovieID;}

    public String getmReleaseDate() {return mReleaseDate;}

    public String getmDuration() {
        return mDuration;
    }

    public String getmRate() {
        return mRate;
    }

    public String getmGenres() {return this.mGenres;}

    public String getmRated() {return this.mRated;}

    public String getmSummary() {
        return mSummary;
    }

    public String getmImageResourceId() {
        return mImageResourceId;
    }

    public List<Trailer> getmTrailers() {return this.mTrailers;}

    public String getmReviews() {return this.mReviews;}

    public String getmBackDropResourceId() {return this.mBackDropResourceId;}

    public void setmRated(String mRated) {
        this.mRated = mRated;
    }
}
