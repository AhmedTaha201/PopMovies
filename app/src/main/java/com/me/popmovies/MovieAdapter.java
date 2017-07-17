package com.me.popmovies;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends BaseAdapter {
    private Context mContext;

    private List<Movie> mlistOfMovies;

    public MovieAdapter(Context c, List<Movie> listOfMovies) {
        mContext = c;
        mlistOfMovies = listOfMovies;
    }

    public int getCount() {
        return mlistOfMovies.size();
    }

    public Object getItem(int position) {
        return mlistOfMovies.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridItemView = convertView;
        if (gridItemView == null) {
            gridItemView = LayoutInflater.from(mContext).inflate(
                    R.layout.grid_item, parent, false);
        }

        Movie currentMovie = mlistOfMovies.get(position);

        ImageView imageView = (ImageView) gridItemView.findViewById(R.id.poster_view);
        String imgUrl = "http://image.tmdb.org/t/p/w185" + currentMovie.getmImageResourceId();
        Log.i("MovieAdapter.java", "Url  ---->  " + imgUrl);
        Picasso.with(mContext).load(imgUrl)
                .placeholder(R.color.movie_poster_place_holder)
                .into(imageView);


        TextView titleView = (TextView) gridItemView.findViewById(R.id.movie_title_text_view);
        titleView.setText(currentMovie.getmTitle());


        return gridItemView;
    }

}