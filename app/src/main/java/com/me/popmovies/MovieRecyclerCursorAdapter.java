package com.me.popmovies;


import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.me.popmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;


public class MovieRecyclerCursorAdapter extends RecyclerView.Adapter<MovieRecyclerCursorAdapter.viewHolder> {

    private Context mContext;

    private Cursor mCursor;

    private boolean mDataValid;

    private int mRowIdColumn;

    private DataSetObserver mDataSetObserver;

    public MovieRecyclerCursorAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    public Cursor getCursor() {
        return mCursor;
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
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_new, parent, false);
        viewHolder holder = new viewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {
        Movie currentMovie = null;

        if (mCursor != null && mCursor.moveToPosition(position)) {
            currentMovie = new Movie(
                    mCursor.getString(mCursor.getColumnIndex(MoviesContract.COLUMN_TITLE)),
                    mCursor.getString(mCursor.getColumnIndex(MoviesContract.COLUMN_MOVIE_ID)),
                    mCursor.getString(mCursor.getColumnIndex(MoviesContract.COLUMN_YEAR)),
                    mCursor.getString(mCursor.getColumnIndex(MoviesContract.COLUMN_DURATION)),
                    mCursor.getString(mCursor.getColumnIndex(MoviesContract.COLUMN_RATE)),
                    mCursor.getString(mCursor.getColumnIndex(MoviesContract.COLUMN_GENRES)),
                    null,
                    mCursor.getString(mCursor.getColumnIndex(MoviesContract.COLUMN_OVERVIEW)),
                    mCursor.getString(mCursor.getColumnIndex(MoviesContract.COLUMN_POSTER_ID)),
                    null,
                    null,
                    mCursor.getString(mCursor.getColumnIndex(MoviesContract.COLUMN_REVIEWS)));


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
            holder.rateC.setText("N/A"); /* As a placeHolder for now*/
            //duration
            holder.duration.setText(currentMovie.getmDuration() + " min");

        }
    }


    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}

