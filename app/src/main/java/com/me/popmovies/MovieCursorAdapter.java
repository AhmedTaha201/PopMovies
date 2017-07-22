package com.me.popmovies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.me.popmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

/**
 * Created for placing the local data from the local database (favourite movies)
 */

public class MovieCursorAdapter extends CursorAdapter{

    final String LOG_TAG = getClass().getSimpleName();
    LayoutInflater inflater;

    public MovieCursorAdapter(Context context) {
        super(context, null, 0);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.grid_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //getting views
        ImageView imageView = (ImageView) view.findViewById(R.id.poster_view);
        TextView textView = (TextView) view.findViewById(R.id.movie_title_text_view);

        //getting data
        String title = cursor.getString(cursor.getColumnIndex(MoviesContract.COLUMN_TITLE));
        String imgPath = cursor.getString(cursor.getColumnIndex(MoviesContract.COLUMN_POSTER_ID));

        //Assigning data to views
        String imgUrl = "http://image.tmdb.org/t/p/w185" + imgPath;
        Log.i(LOG_TAG, "Url  ---->  " + imgUrl);

        Picasso.with(context)
                .load(imgUrl)
                .into(imageView);
        textView.setText(title);
    }
}
