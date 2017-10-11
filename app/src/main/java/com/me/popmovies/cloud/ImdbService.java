package com.me.popmovies.cloud;

import com.me.popmovies.IMDBMovie;
import com.me.popmovies.Movie;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Query;


    public interface ImdbService {

        final String BASE_URL = "http://www.myapifilms.com/";

        @GET("imdb/idIMDB?token=99eae789-eb89-4416-a5be-ab96b9391c4b")
        Call<IMDBMovie> getIMDBData(
                @Query("idIMDB") String imdb_id);

}
