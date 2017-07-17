package com.me.popmovies;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    final String LOG_TAG = getClass().getSimpleName();

    final String BaseUrl = "https://api.themoviedb.org/3/movie/";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(getContext(), settingsActivity.class);
            startActivity(settingIntent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        updateData();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        // Inflate the layout for this fragment
        return rootView;
    }


    private void updateData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String moviesList = sharedPreferences.getString(getString(R.string.moviesList_key), getString(R.string.popular_movies_value));
        String sortTytpe = sharedPreferences.getString(getString(R.string.sort_by_key), getString(R.string.popularity_value));

        Log.e(LOG_TAG, "--->   " + moviesList + "   <---");
        Log.e(LOG_TAG, "--->   " + sortTytpe + "   <---");

        String UrlToQuery = BaseUrl + moviesList + "?";
        Uri.Builder uri = Uri.parse(UrlToQuery).buildUpon();
        uri.appendQueryParameter(getString(R.string.sort_by_key), sortTytpe)
                .appendQueryParameter(getString(R.string.api_key), getString(R.string.api_value));

        UrlToQuery = uri.build().toString();

        Log.e(LOG_TAG, "--->   " + UrlToQuery + "   <---");


        new MoviesTask().execute(UrlToQuery);
    }

    // A helper method to be called from OnPostExecute to update the UI with the data from the server
    private void UpdateUI(final List<Movie> movies) {

        MovieAdapter movieAdapter = new MovieAdapter(getContext(), movies);

        GridView gridView = (GridView) getActivity().findViewById(R.id.gridView);

        ProgressBar bar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);

        gridView.setEmptyView(bar);

        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(), movies.get(i).getmTitle(), Toast.LENGTH_SHORT).show();

                Intent detailIntent = new Intent(getContext(), DetailsActivity.class);
                detailIntent.putExtra(getString(R.string.intent_key), movies.get(i));
                startActivity(detailIntent);
            }
        });
    }


    private class MoviesTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(String... strings) {
            String QueryUrl = strings[0];

            List<Movie> movies = getMovies(QueryUrl);
            return movies;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            UpdateUI(movies);
        }
    }


    // Some helper methods to handle the query


    // Main method making use of all the other helper methods takes a url and returns a movies list
    private List<Movie> getMovies(String url) {

        return ExtractMoviesListFromJson(ReadFromStream(MakeHttpUrlConnection(url)));
    }

    /* {@link CreateUrl} method handles making a url object from a string url
     */
    public URL CreateUrl(String url) {
        URL urlObject = null;

        if (url != null) {
            try {
                urlObject = new URL(url);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "The url is malformed");
            }
        }

        return urlObject;
    }

    //MakeHttpUrlConnection method handles making the url connection using a ur object
    private InputStream MakeHttpUrlConnection(String url) {


        InputStream is = null;

        URL urlObject = CreateUrl(url);

        try {
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000 /* Milliseconds */);
            connection.setReadTimeout(15000 /* Milliseconds */);
            connection.connect();

            is = connection.getInputStream();

        } catch (ProtocolException e) {
            Log.e(LOG_TAG, "Error with the request method");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return is;

    }


    // ReadFromStream handles reading the stream of data from the connection and return a result string
    private String ReadFromStream(InputStream inputStream) {

        StringBuilder stringBuilder = new StringBuilder();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line = bufferedReader.readLine();

            while (line != null && line.length() != 0) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error with the BufferedReader");
        }

        return stringBuilder.toString();

    }


    // ExtractMoviesListFromJson method extracts movies information from the JSON respone
    private List<Movie> ExtractMoviesListFromJson(String jsonResponse) {

        List<Movie> movies = new ArrayList<Movie>();

        String poster;
        String summary;
        String releaseDate;
        String title;
        String rate;
        String id;
        String runTime;
        List<Trailer> trailers;
        String reviews;

        try {
            JSONObject resultsJsonObject = new JSONObject(jsonResponse);
            JSONArray results = resultsJsonObject.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {

                JSONObject currentMovie = results.getJSONObject(i);

                poster = currentMovie.getString("poster_path");
                poster = poster.replace("\\", "/");

                summary = currentMovie.getString("overview");
                releaseDate = currentMovie.getString("release_date");
                title = currentMovie.getString("original_title");
                rate = currentMovie.getString("vote_average");
                id = currentMovie.getString("id");

                //Extracting json response for a single movie to get runtime, trailers and reviews
                //It`s costing a lot ,but ...
                String jsonResponseString = getMovieDetails(id);

                //getting runtime
                runTime = extractRunTimeFromJson(jsonResponseString);

                //getting trailers list
                trailers = extractTrailersListFromJsonResponse(jsonResponseString);

                //getting reviews text
                reviews = extractReviewsFromJsonResponse(jsonResponseString);

                movies.add(new Movie(title, releaseDate, runTime, rate, summary, poster, trailers ,reviews));

            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error with extracting the list from the response");
        }

        return movies;

    }

    //Helper method to get single movie details from the ID
    // Runtime, Trailers and Reviews
    private String getMovieDetails(String id) {

        String url = "https://api.themoviedb.org/3/movie/" + id + "?api_key=" + getString(R.string.api_value) +
                "&append_to_response=videos,reviews";
        Log.d(LOG_TAG, "movie url ==> " + url);

        return ReadFromStream(MakeHttpUrlConnection(url));
    }

    //Helper method to get movie Runtime from json response -- single movie query with id
    private String extractRunTimeFromJson(String jsonResponseString) {
        String runTime = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonResponseString);
            runTime = jsonObject.getString("runtime");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return runTime;
    }

    //Helper method to get trailers list from json response -- single movie query with id
    private List<Trailer> extractTrailersListFromJsonResponse(String jsonResponseString) {
        List<Trailer> trailersList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponseString);
            JSONObject videos = jsonObject.getJSONObject("videos");
            JSONArray trailers = videos.getJSONArray("results");

            for (int i = 0; i < trailers.length(); i++) {
                JSONObject trailer = (JSONObject) trailers.get(i);
                String name = trailer.getString("name");
                String type = trailer.getString("type");
                String key = trailer.getString("key");

                Trailer newTrailer = new Trailer(name, type, key);
                trailersList.add(newTrailer);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return trailersList;
    }

    //Helper method to get reviews from the json response and return it as a string
    private String extractReviewsFromJsonResponse(String jsonResponseString){
        String reviews = "";
        StringBuilder builder = new StringBuilder();

        try {
            JSONObject object = new JSONObject(jsonResponseString);
            JSONObject reviewsObject = object.getJSONObject("reviews");
            JSONArray reviewsArray = reviewsObject.getJSONArray("results");

            for (int i =0; i < reviewsArray.length(); i++){

                JSONObject currentReview = (JSONObject) reviewsArray.get(i);
                builder.append("Author : ");
                String author = currentReview.getString("author");
                builder.append(author + "\n");
                String content = currentReview.getString("content");
                builder.append(content + "\n \n");
            }
            reviews = builder.toString();
            if (reviews == ""){
                return getString(R.string.no_reviews);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return reviews;
    }
}