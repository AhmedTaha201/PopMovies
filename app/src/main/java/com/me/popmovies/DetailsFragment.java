package com.me.popmovies;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {


    public DetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        String[] movieDetails = null;

        // Getting the intent that started the activity and then getting its data
        Intent detailsIntent = getActivity().getIntent();

        if (detailsIntent != null && detailsIntent.hasExtra(Intent.EXTRA_TEXT)) {
            movieDetails = detailsIntent.getStringExtra(Intent.EXTRA_TEXT).split("&");
        }
        // Putting the data in its places

        TextView titleTextView = (TextView) rootView.findViewById(R.id.movieTitle);
        titleTextView.setText(movieDetails[0]);


        // Setting the text to be only the year instead of the whole date
        final TextView yearTextView = (TextView) rootView.findViewById(R.id.movieReleaseDate);
        final String date = movieDetails[1];
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


        TextView durationTextView = (TextView) rootView.findViewById(R.id.movieDuration);
        String duration = movieDetails[2] + "min";
        durationTextView.setText(duration);


        TextView rateTextView = (TextView) rootView.findViewById(R.id.movieRate);
        rateTextView.setText(movieDetails[3]);


        TextView summaryTextView = (TextView) rootView.findViewById(R.id.movieStory);
        summaryTextView.setText(movieDetails[4]);

        ImageView posterView = (ImageView) rootView.findViewById(R.id.moviePoster);
        String imgUrl = "http://image.tmdb.org/t/p/w185" + movieDetails[5];
        Picasso.with(getActivity()).load(imgUrl).into(posterView);


        return rootView;
    }

}
