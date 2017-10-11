package com.me.popmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;


public class MovieRecyclerAdapter extends RecyclerView.Adapter<MovieRecyclerAdapter.viewHolder> {

    List<Movie> movieList;
    Context mContext;

    public MovieRecyclerAdapter(Context mContext, List<Movie> movieList) {
        this.mContext = mContext;
        this.movieList = movieList;
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, date, genres, rate, rateC, duration;

        public viewHolder(View itemView) {
            super(itemView);
            poster = (ImageView) itemView.findViewById(R.id.new_poster);
            title = (TextView) itemView.findViewById(R.id.new_title);
            date = (TextView) itemView.findViewById(R.id.new_date);
            genres = (TextView) itemView.findViewById(R.id.new_genres);
            rate = (TextView) itemView.findViewById(R.id.new_rate);
            rateC = (TextView) itemView.findViewById(R.id.new_rate_c);
            duration = (TextView) itemView.findViewById(R.id.new_duration);
        }
    }

    @Override
    public MovieRecyclerAdapter.viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_new, parent, false);
        viewHolder holder = new viewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MovieRecyclerAdapter.viewHolder holder, int position) {
        Movie currentMovie = movieList.get(position);

        //poster
        String imgUrl = "http://image.tmdb.org/t/p/w92" /* "w92", "w154", "w185", "w342", "w500", "w780", or "original" */
                + currentMovie.getmImageResourceId();
        Picasso.with(mContext)
                .load(imgUrl)
                .into(holder.poster);

        //title
        holder.title.setText(currentMovie.getmTitle());

        //date
        String[] dateElements = currentMovie.getmReleaseDate().split("-");
        final String year = dateElements[0];
        holder.date.setText(year);


        //genres
        holder.genres.setText(currentMovie.getmGenres());
        //rate
        holder.rate.setText(currentMovie.getmRate());
        //rateC
        holder.rateC.setText(currentMovie.getmRated()); /* As a placeHolder for now*/
        //duration
        holder.duration.setText(currentMovie.getmDuration() + " min");

    }

    @Override
    public int getItemCount() {
        if (movieList != null) {
            return movieList.size();
        }
        return 0;
    }

    public void updateAdapter(List<Movie> mlistOfMovies) {
        this.movieList = mlistOfMovies;

        //and call notifyDataSetChanged
        notifyDataSetChanged();
    }

}
