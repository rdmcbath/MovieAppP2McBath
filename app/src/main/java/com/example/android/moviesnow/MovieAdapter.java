package com.example.android.moviesnow;

//MovieAdapter knows how to create a grid item layout for each movie in the data source
//(a grid of movie posters)

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

// The purpose of this MovieAdapter is for the main screen to display the poster in a grid view.

class MovieAdapter extends ArrayAdapter<Movie> {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    // construct a new MovieAdapter
    MovieAdapter(MainActivity context, ArrayList<Movie> movieList) {
        super(context, 0, movieList);
    }

    // Inflate the grid_item.xml
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final Movie movie = getItem(position);
        String moviePosterPath;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grid_item, parent, false);
        }
        // show the image with Picasso
        ImageView iconView = (ImageView) convertView.findViewById(R.id.androidMovieImage);
        assert movie != null;
        moviePosterPath = movie.getmMoviePoster();
        final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
        final String POSTER_WIDTH = "w185";
        final String FULL_POSTER_URL = POSTER_BASE_URL + POSTER_WIDTH + moviePosterPath;
        Picasso.with(getContext()).load(FULL_POSTER_URL).into(iconView);

        Log.v(LOG_TAG, "ADAPTER GETVIEW method called");

        return convertView;
    }
}
