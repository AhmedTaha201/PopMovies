package com.me.popmovies;


import android.content.BroadcastReceiver;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.me.popmovies.cloud.ImdbService;
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
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

    //internetReceiver receiver;

    String moviesList;

    SharedPreferences sharedPreferences;

    RecyclerView recyclerView;

    int position = -1;

    MaterialSearchView searchView = null;

    RecyclerView.Adapter resultAdapter = null;

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
        bar.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
        dbHelper = new MoviesDBHelper(getActivity());
        db = dbHelper.getReadableDatabase();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        moviesList = sharedPreferences.getString(getString(R.string.moviesList_key), getString(R.string.popular_movies_value)).toLowerCase();

        super.onStart();
        if (resultAdapter != null && saveSearchResults == true && !moviesList.equalsIgnoreCase(getString(R.string.fav_movies_value))
                || resultAdapter != null && !isOnline(getActivity()) && !moviesList.equalsIgnoreCase(getString(R.string.fav_movies_value))) {
            recyclerView.setAdapter(resultAdapter);
        } else {
            updateData();
            bar.setVisibility(View.GONE);
        }

        if (-1 != position) {
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            manager.scrollToPositionWithOffset(position, 50);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        //We save the adapter anyway , if it`s a serarch we need the data again and if there is no internet connection
        //we need it temporarily
        try {
            resultAdapter = (MovieRecyclerAdapter) recyclerView.getAdapter();
        } catch (Exception e) {
            resultAdapter = (MovieRecyclerCursorAdapter) recyclerView.getAdapter();
        }

        //Registering the receiver
        //Todo -- Activate the receiver
/*
        receiver = new internetReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getActivity().registerReceiver(receiver, filter);
*/
    }

    //Helper method to show erreo hints like no connection or no fav. movies
    //case id is the number matching the case (0 is no connection, 1 is no API_KEY, 2 is no favourited movies and 3 is searching
    public static final int NO_INTENET_ID = 0;
    public static final int NO_API_ID = 1;
    public static final int NO_FAV_ID = 2;
    public static final int SEARCHING_ID = 3;
    public static final int CLEAR = 100; /* To be used with show hint to ONLY clear the textview
     and NOT TO be used with the clear adapter method*/

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
                        bar.setVisibility(View.GONE);
                        clearAdapter(SEARCHING_ID);

                        // Turned into TRUE to savethe results for the back press from the details of a single searched movie
                        saveSearchResults = true;

                        String searchUrl = "http://api.themoviedb.org/3/search/movie?api_key="
                                + getString(R.string.api_value)
                                + "&query=" + query;

                        new MoviesTask().execute(searchUrl);
                        showHint(CLEAR);
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

        recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);

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

                MovieRecyclerCursorAdapter movieAdapter = new MovieRecyclerCursorAdapter(getActivity(), fav_movies_cursor);
                movieAdapter.swapCursor(fav_movies_cursor);

                recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
                recyclerView.setAdapter(movieAdapter);

                ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int i, View v) {
                        //getting the clicked item position for restoring it later
                        position = i;

                        /*When there is no internet connection we would only read the data from the database and pass
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
                                    fav_movies_cursor.getString(fav_movies_cursor.getColumnIndex(MoviesContract.COLUMN_GENRES)),
                                    null, /*Rating*/
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

        MovieRecyclerAdapter movieAdapter = new MovieRecyclerAdapter(getActivity(), movies);
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setAdapter(movieAdapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int i, View v) {
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

        final List<Movie> movies = new ArrayList<Movie>();

        String poster;
        String summary;
        String releaseDate;
        String title;
        String rate;
        String genres;
        String imdb_id;
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

                JSONArray genresArray = currentMovie.getJSONArray("genre_ids");
                String[] genresA = new String[genresArray.length()];
                for (int j = 0; j < genresArray.length(); j++) {
                    genresA[j] = String.valueOf(genresArray.get(j));
                }
                genres = getGenreString(genresA);


                //Extracting json response for a single movie to get runtime, trailers and reviews
                //It`s costing a lot ,but ...
                String jsonResponseString = getMovieDetails(getActivity(), id);

                //getting runtime
                runTime = extractRunTimeFromJson(jsonResponseString);

                //getting IMDB_id
                imdb_id = extractIMDB_id_fromJsonResponse(jsonResponseString);

                //getting trailers list
                trailers = extractTrailersListFromJsonResponse(jsonResponseString);

                //getting reviews text
                reviews = extractReviewsFromJsonResponse(jsonResponseString);

                //Getting
                final Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://www.myapifilms.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                final String[] rate_c = new String[1];

                ImdbService service = retrofit.create(ImdbService.class);
                Call<IMDBMovie> call = service.getIMDBData(imdb_id);
                final int finalI = i;
                call.enqueue(new Callback<IMDBMovie>() {
                    @Override
                    public void onResponse(Call<IMDBMovie> call, Response<IMDBMovie> response) {
                        Toast.makeText(getActivity(), "GOT IMDB data", Toast.LENGTH_SHORT).show();
                        IMDBMovie m = response.body();
                        rate_c[0] = m.getRated();
                        movies.get(finalI).setmRated(m.getRated());
                    }

                    @Override
                    public void onFailure(Call<IMDBMovie> call, Throwable t) {
                        // Toast.makeText(getActivity(), "Failed to fetch IMDB data", Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, "Failed to fetch IMDB data");

                    }

                });

                movies.add(new Movie(title, imdb_id, releaseDate, runTime, rate, genres, rate_c[0], summary,poster,backPoster
                ,trailers, reviews));

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

    //Helper Method to get Imdb_id
    private String extractIMDB_id_fromJsonResponse(String jsonResponseString) {
        String imdb_id = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonResponseString);
            imdb_id = jsonObject.getString("imdb_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imdb_id;

    }

    //Helper method to get the rest of the movie details from {OMDB API} which returns IMDB data
    /*
    * It has a daily limit of 2000 requests
    * And it is a little bit retarded */
    public static String getOMDBData(String jsonResponse) {
        String rate_c = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray movies = data.getJSONArray("movies");
            JSONObject movie = (JSONObject) movies.get(0);

            rate_c = movie.getString("rated");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rate_c;
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
//        getActivity().unregisterReceiver(receiver);
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
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);

        //Show the progress bar and prepare for the data
        try {
            MovieRecyclerAdapter adapter = (MovieRecyclerAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.updateAdapter(null);//to clear the grid view for the moment
            }
            showHint(caseID);
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Casting Problem");
            e.printStackTrace();
        }

        try {
            MovieRecyclerCursorAdapter adapter = (MovieRecyclerCursorAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.swapCursor(null);//updating the base adapter with null data to clear the GridView
            }
            showHint(caseID);
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Casting Problem");
            e.printStackTrace();
        }
    }

    //Broadcast Receiver for listening to the internet connection and if it`s connected we update the data
    public class internetReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isOnline(context)) {
                updateData();
            } else {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() { //Waiting for 2 sec before delivering the bad news
                        Snackbar.make(getView(), getString(R.string.lost_connection_text), Snackbar.LENGTH_SHORT).show();
                    }
                }, 2000);
            }
        }
    }

    //Helper method to get genres as one formated string
    public static String getGenreString(String[] genres) {
        StringBuilder builder = new StringBuilder();
        if (genres != null) {
            for (int i = 0; i < genres.length; i++) {
                int genreID = Integer.parseInt(genres[i]);
                String genreString = getGenre(genreID);
                if (!TextUtils.isEmpty(genreString)) {
                    builder.append(genreString);
                    if (i < genres.length - 1) {
                        builder.append(" | ");
                    }
                }
            }
        }
        return builder.toString();
    }

    //Helper method to get the corresponding genre from the genre id
    public static String getGenre(int id) {
        switch (id) {
            case 28:
                return "Action";
            case 12:
                return "Adventure";
            case 16:
                return "Animation";
            case 35:
                return "Comedy";
            case 80:
                return "Crime";
            case 99:
                return "Documentary";
            case 18:
                return "Drama";
            case 10751:
                return "Family";
            case 14:
                return "Fantasy";
            case 36:
                return "History";
            case 27:
                return "Horror";
            case 10402:
                return "Music";
            case 9648:
                return "Mystery";
            case 10749:
                return "Romance";
            case 878:
                return "Science Fiction";
            case 10770:
                return "TV Movie";
            case 53:
                return "Thriller";
            case 10752:
                return "War";
            case 37:
                return "Western";
            default:
                return "";
        }
    }
}