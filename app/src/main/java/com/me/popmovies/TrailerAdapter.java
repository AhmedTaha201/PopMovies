package com.me.popmovies;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TrailerAdapter extends ArrayAdapter<Trailer> {

    Context mContext;


    public TrailerAdapter(@NonNull Context context, @NonNull List<Trailer> objects) {
        super(context, 0, objects);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Trailer trailer = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.trailers_list_item, parent, false);
        }

        //Trailer name
        TextView trailerNameTextView = (TextView) convertView.findViewById(R.id.trailer_name_text_view);
        trailerNameTextView.setText(trailer.getName() + " - " + trailer.getType());

        //Video intent
        Uri videoUri = Uri.parse(trailer.trailerUrlBuild());
        final Intent trailerIntent = new Intent(Intent.ACTION_VIEW, videoUri);

        PackageManager manager = mContext.getPackageManager();

        if (trailerIntent.resolveActivity(manager) != null){
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(trailerIntent);
                }
            });
        }
        return convertView;

    }
}
