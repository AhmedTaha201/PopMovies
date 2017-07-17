package com.me.popmovies;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
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

        Movie currentMovie = null;

        // Getting the intent that started the activity and then getting its data
        Intent detailsIntent = getActivity().getIntent();

        //TODO -- Use Android Parcelable instead of Java serializble
        if (detailsIntent != null && detailsIntent.hasExtra(getString(R.string.intent_key))) {
            currentMovie = (Movie) detailsIntent.getSerializableExtra(getString(R.string.intent_key));
        }
        // Putting the data in its places

        //Title
        TextView titleTextView = (TextView) rootView.findViewById(R.id.movieTitle);
        titleTextView.setText(currentMovie.getmTitle());


        // Setting the text to be only the year instead of the whole date
        final TextView yearTextView = (TextView) rootView.findViewById(R.id.movieReleaseDate);
        final String date = currentMovie.getmReleaseDate();
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

        //Duration
        TextView durationTextView = (TextView) rootView.findViewById(R.id.movieDuration);
        String duration = currentMovie.getmDuration() + "min";
        durationTextView.setText(duration);

        //Rate
        TextView rateTextView = (TextView) rootView.findViewById(R.id.movieRate);
        rateTextView.setText(currentMovie.getmRate());

        //Summary
        TextView summaryTextView = (TextView) rootView.findViewById(R.id.movieStory);
        summaryTextView.setText(currentMovie.getmSummary());

        ImageView posterView = (ImageView) rootView.findViewById(R.id.moviePoster);
        String imgUrl = "http://image.tmdb.org/t/p/w185" + currentMovie.getmImageResourceId();
        Picasso.with(getActivity()).load(imgUrl).into(posterView);

        //Trailers list
        TrailerAdapter trailerAdapter = new TrailerAdapter(getActivity(), currentMovie.getmTrailers());

        ExpandableHeightListView listView = (ExpandableHeightListView) rootView.findViewById(R.id.trailers_list_view);
        listView.setAdapter(trailerAdapter);

        // This actually does the magic
        listView.setExpanded(true);



        return rootView;
    }

}
