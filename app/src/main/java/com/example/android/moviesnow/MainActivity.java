package com.example.android.moviesnow;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String LOG_TAG = MainActivity.class.getName();

    //Store the value for preferences so that a change can be monitored
    String sort_criteria;

    private String sortBy;
    private String x = "popular";

    private static String SORT_ORDER;
    private static final int SORT_ORDER_POPULAR = 0;
    private static final int SORT_ORDER_TOP_RATED = 1;
    final String apiKey = "aefc73e95d19c5632cac821acbcf1925";

    /**
     * Constant value for the news loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int MOVIE_LOADER_ID = 1;
    SharedPreferences sharedPrefs;
    private MovieAdapter mAdapter = null;
    RecyclerView recyclerView;
    private ProgressBar spinnerProgress;
    List<Movie> movie = new ArrayList<>();

    // TextView that is displayed when the list is empty (For Later)
    private TextView mEmptyStateTextView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (id == R.id.action_popular) {
            sharedPrefs.edit().putInt(SORT_ORDER, SORT_ORDER_POPULAR).apply();
            queryAPI();

            // Show toast to verify sort order by most popular
            Toast.makeText(getApplicationContext(), "Sorted by most popular"
                    , Toast.LENGTH_SHORT).show();
        }

        if (id == R.id.action_top_rated) {
            sharedPrefs.edit().putInt(SORT_ORDER, SORT_ORDER_TOP_RATED).apply();
            queryAPI();

            // Show toast to verify sort order by top rated
            Toast.makeText(getApplicationContext(), "Sorted by top rated"
                    , Toast.LENGTH_SHORT).show();
        }

        if (id == R.id.action_favorites) {

            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);

            spinnerProgress.setVisibility(View.VISIBLE);

            // Show toast to verify sort order by top rated
            Toast.makeText(getApplicationContext(), "Sorted by favorites"
                    , Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the adapter that takes data and populated the GridView attached to it
        mAdapter = new MovieAdapter(this, new ArrayList<Movie>());

        //set default preferences when the app starts (order by relevance)
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        PreferenceManager.setDefaultValues(this, R.xml.settings_main, true);
        sortBy = sharedPrefs.getString(
                getString(R.string.settings_sort_by_default),
                getString(R.string.main_menu_action_popular));

        // Find a reference to the {@link GridView} in the layout
        GridView gridItemView = (GridView) findViewById(R.id.movie_grid_view);

        // Set the adapter to the GridView
        gridItemView.setAdapter(mAdapter);

        // Create an empty TextView to display message when no data returned from GET request
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        gridItemView.setEmptyView(mEmptyStateTextView);

        spinnerProgress = (ProgressBar) findViewById(R.id.loading_spinner);
        spinnerProgress.setVisibility(View.VISIBLE);

        gridItemView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Start the details activity
                Movie dataToSend = mAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, MovieDetail.class);
                intent.putExtra("movie", dataToSend);
                startActivity(intent);
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {

            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(MOVIE_LOADER_ID, null, MainActivity.this);

        } else {

            // Update empty state with no connection error message
            Toast.makeText(getApplicationContext(), "No Network", Toast.LENGTH_SHORT).show();
            //hide loading indicator
            spinnerProgress.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("moviesList", (ArrayList<? extends Parcelable>) movie);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "TEST: OnCREATE LOADER CALLBACK");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.authority("api.themoviedb.org");
        builder.appendEncodedPath("3/movie/" + x);
        builder.appendQueryParameter("api_key", apiKey);
        Log.v("TEST", "TEST" + builder.toString());

        return new MovieLoader(this, builder.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "TEST: ONRESUME");

        PreferenceManager.setDefaultValues(this, R.xml.settings_main, true);

        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movieItems) {
        Log.i(LOG_TAG, "TEST: OnLOADFINISH");

        // Hide loading indicator because the data has been loaded
        spinnerProgress.setVisibility(View.GONE);
        // Clear the adapter of previous movie data
        mAdapter.clear();

        if (movieItems == null) {
            // Set empty state text to display "No movies found."
            mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
            mEmptyStateTextView.setText(R.string.no_movies_found);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            mAdapter.clear();
        }
        // If there are valid {@link movie}items, then add them to the adapter's
        // data set. This will trigger the GridView to update.
        if (movieItems != null && !movieItems.isEmpty()) {
            mAdapter.addAll(movieItems);

            // Notify the Adapter of changes
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        Log.i(LOG_TAG, "TEST:OnLOAD RESET");

        // Clear the adapter of previous movie data
        mAdapter.clear();
        mEmptyStateTextView.setVisibility(View.GONE);
    }

    private void queryAPI() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int sortOrder = sharedPreferences.getInt(SORT_ORDER, SORT_ORDER_POPULAR);
        switch (sortOrder) {

            case SORT_ORDER_POPULAR:
                x = "popular";
                //query popular URL
                String POPULARITY_URL = "http://api.themoviedb.org/3/movie/popular?api_key=aefc73e95d19c5632cac821acbcf1925";
                Uri.parse(POPULARITY_URL);
                Log.i(LOG_TAG, "TEST: Popular API: " + POPULARITY_URL);
                getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
                mAdapter.notifyDataSetChanged();
                spinnerProgress.setVisibility(View.VISIBLE);
                break;

            case SORT_ORDER_TOP_RATED:
                x = "top_rated";
                //query top rated URL
                String HIGHEST_RATED_URL = "http://api.themoviedb.org/3/movie/top_rated?api_key=aefc73e95d19c5632cac821acbcf1925";
                Uri.parse(HIGHEST_RATED_URL);
                Log.i(LOG_TAG, "TEST: Top Rated API: " + HIGHEST_RATED_URL);
                getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
                mAdapter.notifyDataSetChanged();
                spinnerProgress.setVisibility(View.VISIBLE);
                break;
        }
    }
}


