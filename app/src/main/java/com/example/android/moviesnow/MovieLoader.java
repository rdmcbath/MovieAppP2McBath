package com.example.android.moviesnow;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

class MovieLoader extends AsyncTaskLoader<List<Movie>> {
    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = MovieLoader.class.getName();

    /**
     * Query URL
     */
    private String mUrl;

    /**
     * Constructs a new {@link MovieLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    MovieLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAG, "TEST: OnSTART LOADING");
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Movie> loadInBackground() {
        Log.i(LOG_TAG, "TEST: LOAD IN BACKGROUND");
        if (mUrl == null) {
            return null;
        }
        // Perform the network request, parse the response, and extract a grid of movie items.

        return MovieUtils.fetchMovieData(mUrl);
    }
}



