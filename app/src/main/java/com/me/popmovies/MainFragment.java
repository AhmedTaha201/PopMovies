package com.me.popmovies;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.me.popmovies.data.MoviesContract;
import com.me.popmovies.data.MoviesDBHelper;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

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

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    final String LOG_TAG = getClass().getSimpleName();

    final String BaseUrl = "https://api.themoviedb.org/3/movie/";

    ProgressBar bar;

    TextView textView;

    MoviesDBHelper dbHelper;
    SQLiteDatabase db;

    String moviesList;

    SharedPreferences sharedPreferences;

    GridView gridView;

    int position = -1;

    MaterialSearchView searchView = null;

    BaseAdapter resultAdapter = null;

    //public boolean to check if we should return the search results or the popular ,top rated, ... movies
    //we change it to false in settings activity and true in search submit
    public static boolean saveSearchResults = false;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        if (searchView != null) {
            searchView.setMenuItem(item);
        }
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
        dbHelper = new MoviesDBHelper(getActivity());
        db = dbHelper.getReadableDatabase();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        moviesList = sharedPreferences.getString(getString(R.string.moviesList_key), getString(R.string.popular_movies_value)).toLowerCase();

        super.onStart();
        if (resultAdapter != null && saveSearchResults == true ) {
            gridView.setAdapter(resultAdapter);
        } else {
            updateData();
        }

        if (-1 != position) {
            gridView.setSelection(position);
        }

    }

    //Helper method to show erreo hints like no connection or no fav. movies
    //case id is the number matching the case (0 is no connection, 1 is no API_KEY and 2 is no favourited movies
    public static final int NO_INTENET_ID = 0;
    public static final int NO_API_ID = 1;
    public static final int NO_FAV_ID = 2;
    public static final int SEARCHING_ID = 3;

    public void showHint(int caseID) {
        //getting the progress bar and the no-internet text to show the text and hide the progress bar
        //when there is no internet connection

        bar.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);

        switch (caseID) {
            case NO_INTENET_ID:
                textView.setText(getString(R.string.no_internet_text));
                break;
            case NO_API_ID:
                textView.setText(getString(R.string.no_api));
                break;
            case NO_FAV_ID:
                textView.setText(getString(R.string.no_favourites));
                break;
            case SEARCHING_ID:
                textView.setText(getString(R.string.search_text));
                break;
            default:
                textView.setText("");
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        bar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        textView = (TextView) rootView.findViewById(R.id.no_internet_no_api_no_fav);

        searchView = (MaterialSearchView) rootView.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query == "") {
                    Snackbar.make(rootView, getString(R.string.search_empty_query), Snackbar.LENGTH_SHORT).show();
                    return true;
                } else {
                    if (isOnline(getActivity())) {
                        clearAdapter(SEARCHING_ID);

                        // Turned into TRUE to savethe results for the back press from the details of a single searched movie
                        saveSearchResults = true;

                        String searchUrl = "http://api.themoviedb.org/3/search/movie?api_key="
                                + getString(R.string.api_value)
                                + "&query=" + query;

                        new MoviesTask().execute(searchUrl);
                        return false;
                    } else {
                        Snackbar.make(rootView, getString(R.string.search_connect_internet), Snackbar.LENGTH_SHORT).show();
                        return true;
                    }
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        showHint(-1);
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void updateData() {

        gridView = (GridView) getActivity().findViewById(R.id.gridView);

        moviesList = sharedPreferences.getString(getString(R.string.moviesList_key), getString(R.string.popular_movies_value)).toLowerCase();
        String sortTytpe = sharedPreferences.getString(getString(R.string.sort_by_key), getString(R.string.popularity_value));

        Log.e(LOG_TAG, "--->   " + moviesList + "   <---");
        Log.e(LOG_TAG, "--->   " + sortTytpe + "   <---");

        String api_key = getActivity().getString(R.string.api_value);

        if (api_key.equalsIgnoreCase("API_KEY")) {
            showHint(NO_API_ID);

        } else if (!isOnline(getActivity()) && !moviesList.equalsIgnoreCase(getString(R.string.fav_movies_value))) {
            clearAdapter(NO_INTENET_ID);

        } else if (moviesList.equalsIgnoreCase(getString(R.string.fav_movies_value))) {
            //getting the base adapter to clear it for

            /* If we went from Favourite to Now Playing as an example, the adapter won`t cast into Baseadapter
                because it`s a cursor adapter and there would be an error
                 */
            clearAdapter(-1);

            final Cursor fav_movies_cursor = db.query(MoviesContract.TABLE_NAME, null, null, null, null, null, null);

            if (fav_movies_cursor.getCount() == 0) {//Empty cursor - No favourites
                showHint(NO_FAV_ID);
            } else {// there is some favourited movies
                showHint(-1); // To show an empty string instead of a warning

                MovieCursorAdapter movieAdapter = new MovieCursorAdapter(getActivity());
                movieAdapter.swapCursor(fav_movies_cursor);

                gridView = (GridView) getActivity().findViewById(R.id.gridView);

                gridView.setAdapter(movieAdapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        //getting the clicked item position for restoring it later
                        position = i;
                        /*
                        When there is no internet connection we would only read the data from the database and pass
                        it as a Movie object, but if there is internet connection we load the rest of the data
                         */
                        //Getting the data of the clicked movie and putting it in a movie object to send with the intent
                        Movie currentMovie = null;
                        if (fav_movies_cursor.moveToPosition(i)) {
                            currentMovie = new Movie(
                                    fav_movies_cursor.getString(fav_movies_cursor.getColumnIndex(MoviesContract.COLUMN_TITLE)),
                                    fav_movies_cursor.getString(fav_movies_cursor.getColumnIndex(MoviesContract.COLUMN_MOVIE_ID)),
                                    fav_movies_cursor.getString(fav_movies_cursor.getColumnIndex(MoviesContract.COLUMN_YEAR)),
                                    fav_movies_cursor.getString(fav_movies_cursor.getColumnIndex(MoviesContract.COLUMN_DURATION)),
                                    fav_movies_cursor.getString(fav_movies_cursor.getColumnIndex(MoviesContract.COLUMN_RATE)),
                                    fav_movies_cursor.getString(fav_movies_cursor.getColumnIndex(MoviesContract.COLUMN_OVERVIEW)),
                                    fav_movies_cursor.getString(fav_movies_cursor.getColumnIndex(MoviesContract.COLUMN_POSTER_ID)),
                                    null,
                                    null,
                                    fav_movies_cursor.getString(fav_movies_cursor.getColumnIndex(MoviesContract.COLUMN_REVIEWS)));
                        }
                        if (currentMovie != null) {
                            Intent detailIntent = new Intent(getContext(), DetailsActivity.class);
                            detailIntent.putExtra(getString(R.string.intent_key), currentMovie);
                            startActivity(detailIntent);
                        }

                    }
                });

            }
        } else if (isOnline(getActivity())) {
            showHint(-1);
            //getting the CursorAdapter to clear it for new data
            if (isListChanged() && moviesList.equalsIgnoreCase(getString(R.string.fav_movies_value))) {
                /* If we went from Popular to Top Rated as an example, the adapter won`t cast into Cursor adapter
                because it`s a base adapter and there would be an error
                 */
                clearAdapter(-1);
            }

            //Show the progress bar and prepare for the data
            clearAdapter(-1);
            bar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);

            String UrlToQuery = BaseUrl + moviesList + "?";
            Uri.Builder uri = Uri.parse(UrlToQuery).buildUpon();
            uri.appendQueryParameter(getString(R.string.sort_by_key), sortTytpe)
                    .appendQueryParameter(getString(R.string.api_key), getString(R.string.api_value));

            UrlToQuery = uri.build().toString();

            Log.e(LOG_TAG, "--->   " + UrlToQuery + "   <---");


            new MoviesTask().execute(UrlToQuery);


        }
    }

    // A helper method to be called from OnPostExecute to update the UI with the data from the server
    private void UpdateUI(final List<Movie> movies) {

        MovieBaseAdapter movieAdapter = new MovieBaseAdapter(getContext(), movies);

        GridView gridView = (GridView) getActivity().findViewById(R.id.gridView);

        ProgressBar bar = (ProgressBar) getActivity().findViewById(R.id.progress_bar);

        gridView.setEmptyView(bar);

        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(), movies.get(i).getmTitle(), Toast.LENGTH_SHORT).show();

                //getting the clicked item position for restoring it later
                position = i;

                Intent detailIntent = new Intent(getContext(), DetailsActivity.class);
                detailIntent.putExtra(getString(R.string.intent_key), movies.get(i));
                startActivity(detailIntent);
            }
        });
    }


    //An AsyncTask to get all the movies
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
    public static URL CreateUrl(String url) {
        URL urlObject = null;

        if (url != null) {
            try {
                urlObject = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return urlObject;
    }

    //MakeHttpUrlConnection method handles making the url connection using a ur object
    private static InputStream MakeHttpUrlConnection(String url) {


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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return is;

    }


    // ReadFromStream handles reading the stream of data from the connection and return a result string
    private static String ReadFromStream(InputStream inputStream) {

        StringBuilder stringBuilder = new StringBuilder();
        if (inputStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line = bufferedReader.readLine();

                while (line != null && line.length() != 0) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        String backPoster;
        List<Trailer> trailers;
        String reviews;

        try {
            JSONObject resultsJsonObject = new JSONObject(jsonResponse);
            JSONArray results = resultsJsonObject.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {

                JSONObject currentMovie = results.getJSONObject(i);

                poster = currentMovie.getString("poster_path");
                poster = poster.replace("\\", "/");

                backPoster = currentMovie.getString("backdrop_path");
                backPoster = backPoster.replace("\\", "/");

                summary = currentMovie.getString("overview");
                releaseDate = currentMovie.getString("release_date");
                title = currentMovie.getString("original_title");
                rate = currentMovie.getString("vote_average");
                id = currentMovie.getString("id");

                //Extracting json response for a single movie to get runtime, trailers and reviews
                //It`s costing a lot ,but ...
                String jsonResponseString = getMovieDetails(getActivity(), id);

                //getting runtime
                runTime = extractRunTimeFromJson(jsonResponseString);

                //getting trailers list
                trailers = extractTrailersListFromJsonResponse(jsonResponseString);

                //getting reviews text
                reviews = extractReviewsFromJsonResponse(jsonResponseString);

                movies.add(new Movie(title, id, releaseDate, runTime, rate, summary, poster, backPoster, trailers, reviews));

            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error with extracting the list from the response");
        }

        return movies;

    }

    //Helper method to get single movie details from the ID
    // Runtime, Trailers and Reviews
    public static String getMovieDetails(Context context, String id) {

        String url = "https://api.themoviedb.org/3/movie/" + id + "?api_key=" + context.getResources().getString(R.string.api_value) +
                "&append_to_response=videos,reviews";


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
    public static List<Trailer> extractTrailersListFromJsonResponse(String jsonResponseString) {
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
    private String extractReviewsFromJsonResponse(String jsonResponseString) {
        String reviews = "";
        StringBuilder builder = new StringBuilder();

        try {
            JSONObject object = new JSONObject(jsonResponseString);
            JSONObject reviewsObject = object.getJSONObject("reviews");
            JSONArray reviewsArray = reviewsObject.getJSONArray("results");

            for (int i = 0; i < reviewsArray.length(); i++) {

                JSONObject currentReview = (JSONObject) reviewsArray.get(i);
                builder.append("Author : ");
                String author = currentReview.getString("author");
                builder.append(author + "\n");
                String content = currentReview.getString("content");
                builder.append(content + "\n \n");
            }
            reviews = builder.toString();
            if (reviews == "") {
                return getString(R.string.no_reviews);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return reviews;
    }

    //Helper method to check if there is internet connection or not
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    //Helper method to listen for movies preference change
    public boolean isListChanged() {
        String newList = sharedPreferences.getString(getString(R.string.moviesList_key), getString(R.string.popular_movies_value)).toLowerCase();
        if (moviesList.equalsIgnoreCase(newList)) {
            return true;
        }
        return false;
    }

    //Helper method to clear the adapter to show an empty GridView
    public void clearAdapter(int caseID) {
        GridView gridView = (GridView) getActivity().findViewById(R.id.gridView);

        //Show the progress bar and prepare for the data
        try {
            MovieBaseAdapter adapter = (MovieBaseAdapter) gridView.getAdapter();
            if (adapter != null) {
                adapter.updateAdapter(null);//to clear the grid view for the moment
            }
            showHint(caseID);
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Casting Problem");
            e.printStackTrace();
        }

        try {
            MovieCursorAdapter adapter = (MovieCursorAdapter) gridView.getAdapter();
            if (adapter != null) {
                adapter.swapCursor(null);//updating the base adapter with null data to clear the GridView
            }
            showHint(caseID);
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Casting Problem");
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (saveSearchResults) {
            resultAdapter = (BaseAdapter) gridView.getAdapter();
        }
    }
}