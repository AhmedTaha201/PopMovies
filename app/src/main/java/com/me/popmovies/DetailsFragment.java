package com.me.popmovies;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.me.popmovies.data.MoviesContract;
import com.me.popmovies.data.MoviesDBHelper;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {

    Movie currentMovie = null;


    public DetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Hiding the status bar and the action bar

        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);


        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        // Getting the intent that started the activity and then getting its data
        Intent detailsIntent = getActivity().getIntent();

        //TODO -- Use Android Parcelable instead of Java serializable
        if (detailsIntent != null && detailsIntent.hasExtra(getString(R.string.intent_key))) {
            currentMovie = (Movie) detailsIntent.getSerializableExtra(getString(R.string.intent_key));
        }

        // getting the movie data
        final String title = currentMovie.getmTitle();
        final String id = currentMovie.getmMovieID();
        final String date = currentMovie.getmReleaseDate();
        final String duration = currentMovie.getmDuration();
        final String rate = currentMovie.getmRate();
        final String summary = currentMovie.getmSummary();
        final String imgID = currentMovie.getmImageResourceId();
        String backdrop = currentMovie.getmBackDropResourceId();
        final String reviews = currentMovie.getmReviews();
        List<Trailer> trailers = currentMovie.getmTrailers();


        // Putting the data in its places

        //Title
        TextView titleTextView = (TextView) rootView.findViewById(R.id.movieTitle);
        titleTextView.setText(title);


        // Setting the text to be only the year instead of the whole date
        final TextView yearTextView = (TextView) rootView.findViewById(R.id.movieReleaseDate);
        String[] dateElements = date.split("-");
        final String year = dateElements[0];
        yearTextView.setText(year);

        //Setting an OnLongClickListener to show the full date
        yearTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (yearTextView.getText() == year) {
                    yearTextView.setText(date);
                } else if (yearTextView.getText() == date) {
                    yearTextView.setText(year);
                }
            }
        });

        //Duration
        TextView durationTextView = (TextView) rootView.findViewById(R.id.movieDuration);
        durationTextView.setText(duration + "min");

        //Rate
        TextView rateTextView = (TextView) rootView.findViewById(R.id.movieRate);
        rateTextView.setText(rate);
        TextView rateTextView1 = (TextView) rootView.findViewById(R.id.movieRate1);
        rateTextView1.setText(rate);

        //Summary
        TextView summaryTextView = (TextView) rootView.findViewById(R.id.movieStory);
        summaryTextView.setText(summary);

        ImageView posterView = (ImageView) rootView.findViewById(R.id.moviePoster);
        String imgUrl = "http://image.tmdb.org/t/p/w185" + imgID;
        Picasso.with(getActivity()).load(imgUrl).into(posterView);

        ImageView back_poster = (ImageView) rootView.findViewById(R.id.back_poster);
        imgUrl = "http://image.tmdb.org/t/p/w342" + backdrop;
        Picasso.with(getActivity()).load(imgUrl).into(back_poster);

        //Reviews
        final TextView reviewsTextView = (TextView) rootView.findViewById(R.id.reviews_text_view);
        reviewsTextView.setText(reviews);

        //set the reviews text view to change visibility state when the reviews label is clicked
        final TextView reviewsLabel = (TextView) rootView.findViewById(R.id.reviews_label);
        reviewsLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reviewsTextView.getVisibility() == View.VISIBLE) {
                    reviewsTextView.setVisibility(View.GONE);
                    reviewsLabel.setText(getString(R.string.reviews_label_plus));
                } else if (reviewsTextView.getVisibility() == View.GONE) {
                    reviewsTextView.setVisibility(View.VISIBLE);
                    reviewsLabel.setText(getString(R.string.reviews_label_minus));
                }
            }
        });


        if (null != trailers) {

            //Trailers list
            TrailerAdapter trailerAdapter = new TrailerAdapter(getActivity(), trailers);

            ExpandableHeightListView listView = (ExpandableHeightListView) rootView.findViewById(R.id.trailers_list_view);
            listView.setAdapter(trailerAdapter);
            // This actually does the magic -- expanding the list
            listView.setExpanded(true);
        }

        /* Implementing the logic of getting the rest of the favourite movie data -Trailers and backPoster- if we are online
         */
        if (trailers == null && MainFragment.isOnline(getActivity())) {
            new SingleMovieTask().execute(id);
        }


        //Setting the on-click-listener for the fav button
        ShineButton shineButton = (ShineButton) rootView.findViewById(R.id.shine_btn);
        shineButton.setBtnColor(Color.GRAY);
        shineButton.setBtnFillColor(Color.RED);
        shineButton.setShapeResource(R.raw.heart);
        final ShineButton fShineButton = shineButton;

        //Change the colorof the button if it`s one of the favourites
        if (isFavourite(id)) {
            shineButton.setChecked(true);
        }

        shineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 /* preparing the movie details in a contentValues object to insert
                 its a two-edged button for saving the movie as a fav and removing it if it`s already there
                 so we have to check if it`s in the database
                 if it`not we add it
                 if it`s there we remove it */
                String selection = MoviesContract.COLUMN_MOVIE_ID + "=?";
                String[] selectionArgs = new String[]{id};

                if (isFavourite(id)) {//The movie already exists we need to delete it
                    int deleted = new MoviesDBHelper(getActivity())
                            .getReadableDatabase()
                            .delete(MoviesContract.TABLE_NAME, selection, selectionArgs);
                    assert (deleted == 1);
                    fShineButton.setChecked(false);
                    Snackbar.make(rootView, "Deleted movie from favourites", Snackbar.LENGTH_SHORT).show();

                } else { //The movie isn`t there we need to add it
                    ContentValues cv = new ContentValues();
                    cv.put(MoviesContract.COLUMN_TITLE, title);
                    cv.put(MoviesContract.COLUMN_YEAR, date);
                    cv.put(MoviesContract.COLUMN_RATE, rate);
                    cv.put(MoviesContract.COLUMN_DURATION, duration);
                    cv.put(MoviesContract.COLUMN_OVERVIEW, summary);
                    cv.put(MoviesContract.COLUMN_REVIEWS, reviews);
                    cv.put(MoviesContract.COLUMN_MOVIE_ID, id);
                    cv.put(MoviesContract.COLUMN_POSTER_ID, imgID);

                    //Insert the move data
                    long addedID = new MoviesDBHelper(getActivity())
                            .getWritableDatabase()
                            .insert(MoviesContract.TABLE_NAME, null, cv);

                    assert (addedID != -1);
                    Snackbar.make(rootView, "Added movie to favourites", Snackbar.LENGTH_SHORT).show();
                    fShineButton.setChecked(true);
                }
            }
        });

        return rootView;
    }

    public class SingleMovieTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String movieId = params[0];
            return MainFragment.getMovieDetails(getActivity(), movieId);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //Trailers
            List<Trailer> trailers = MainFragment.extractTrailersListFromJsonResponse(s);
            TrailerAdapter trailerAdapter = new TrailerAdapter(getActivity(), trailers);
            ExpandableHeightListView listView = (ExpandableHeightListView) getActivity().findViewById(R.id.trailers_list_view);
            listView.setAdapter(trailerAdapter);
            // This actually does the magic -- expanding the list
            listView.setExpanded(true);

            //BackPoster
            try {
                JSONObject data = new JSONObject(s);
                String backPoster = data.getString("backdrop_path");
                backPoster = backPoster.replace("\\", "/");
                ImageView back_poster = (ImageView) getActivity().findViewById(R.id.back_poster);
                String imgUrl = "http://image.tmdb.org/t/p/w342" + backPoster;
                Picasso.with(getActivity()).load(imgUrl).into(back_poster);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    //Helper method to check if the movie is alredy in the database
    public boolean isFavourite(String movieId) {

        String selection = MoviesContract.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{movieId};

        //querying for that movie
        Cursor data = new MoviesDBHelper(getActivity())
                .getReadableDatabase()
                .query(MoviesContract.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        if (data.moveToFirst() || data.getCount() != 0) {//The movie already exists
            return true;
        }
        return false;
    }
}
