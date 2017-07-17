package com.me.popmovies;

import java.io.Serializable;

/**
 * Created by Ahmed on 07/17/2017.
 */

/**
 *Trailer class for movies
 */

public class Trailer implements Serializable {

    /*
    name : The name of the trailer
    type : ex:comicCon trailer, etc
     */
    public String name, type, youtube_id;

    public static final String youtube_base_url = "https://www.youtube.com/watch?v=";

    public Trailer(String name, String type, String youtube_id){

        this.name = name;
        this.type = type;
        this.youtube_id = youtube_id;
    }

    //Getters
    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getYoutube_id() {
        return this.youtube_id;
    }

    //Helper method to build the trailer youtube url
    public String trailerUrlBuild(){
        String url = youtube_base_url + getYoutube_id();
        return url;
    }
}
