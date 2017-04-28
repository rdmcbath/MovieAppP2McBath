package com.example.android.moviesnow;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.moviesnow.data.MovieContract;
import com.github.zagum.switchicon.SwitchIconView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;
import static com.example.android.moviesnow.R.id.movie_overview;
import static com.example.android.moviesnow.R.id.movie_rating;
import static com.example.android.moviesnow.R.id.movie_title;
import static com.example.android.moviesnow.R.id.release_date;
import static com.example.android.moviesnow.R.id.tv_movie_poster;

public class MovieDetail extends AppCompatActivity {

    private static final String LOG_TAG = MovieDetail.class.getSimpleName();

    private List<String> movieTrailerListKey = new ArrayList<>();
    private List<String> movieTrailerListName = new ArrayList<>();
    private List<String> movieReviewList = new ArrayList<>();
    final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    final String POSTER_WIDTH = "w500";
    final String API_KEY = "aefc73e95d19c5632cac821acbcf1925";
    private static final String MOVIE_SHARE_HASHTAG = "  #ILoveThisMovie";
    Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ArrayList<Trailer> trailers = new ArrayList<>();
        setContentView(R.layout.movie_detail);

        Movie currentMovie = this.getIntent().getParcelableExtra("movie");

        //Find the text view with view ID movie_title
        TextView titleView = (TextView) findViewById(movie_title);
        String title = currentMovie.getmTitle();
        // Get the title name from the current title object and set this text on the TextView
        titleView.setText(title);

        //Find the image view with view ID tv_movie_poster
        ImageView moviePosterView = (ImageView) findViewById(tv_movie_poster);
        String moviePoster = currentMovie.getmMoviePoster();

        // Using picasso, get the poster from the current poster object and set this image on the imageView
        final String FULL_POSTER_URL = POSTER_BASE_URL + POSTER_WIDTH + moviePoster;
        Picasso.with(getBaseContext()).load(FULL_POSTER_URL).into(moviePosterView);

        //Find the text view with view ID movie_overview
        TextView overviewView = (TextView) findViewById(movie_overview);
        String overview = currentMovie.getmOverview();
        // Get the overview from the current overview object and set this text on the TextView
        overviewView.setText(overview);

        //Find the text view with view ID movie_rating
        TextView ratingView = (TextView) findViewById(movie_rating);
        String rating = String.valueOf(currentMovie.getmRating());
        // Get the rating from the current rating object and set this text on the TextView
        ratingView.setText(rating);

        //Find the text view with view ID release_date
        TextView releaseDateView = (TextView) findViewById(release_date);
        String releaseDate = currentMovie.getmReleaseDate();
        // Get the Date from the current date object and set this text on the TextView
        releaseDateView.setText(releaseDate);

        RecyclerView trailerView = (RecyclerView) findViewById(R.id.trailer_view);
        trailerView.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView reviewView = (RecyclerView) findViewById(R.id.review_view);
        reviewView.setLayoutManager(new LinearLayoutManager(this));

        //Get movieId
        String currentMovieId = currentMovie.getmId();
        Log.i(LOG_TAG, "movie id is: " + currentMovieId);
        this.updateReviews();
        this.updateTrailers();

        //If user wants to add movie as a favorite, he will press the heart button
        final SwitchIconView favoriteSwitch = (SwitchIconView) findViewById(R.id.button_favorite);
        favoriteSwitch.setIconEnabled(false);

        favoriteSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (favoriteSwitch.isIconEnabled()) {
                    deleteFavorite();
                    favoriteSwitch.switchState(true);
                } else {
                    saveFavorite();
                    favoriteSwitch.switchState(true);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    //Execute FetchTrailerTask
    private void updateReviews() {
        FetchTrailerTask trailerTask = new FetchTrailerTask();
        trailerTask.execute();
    }

    //Execute FetchReviewTask
    private void updateTrailers() {
        FetchReviewTask reviewTask = new FetchReviewTask();
        reviewTask.execute();
    }

    // Add FetchTrailerTask
    private class FetchTrailerTask extends AsyncTask<String, Void, String> {
        Intent intent = getIntent();
        Movie currentMovie = intent.getParcelableExtra("movie");
        String currentMovieId = currentMovie.getmId();

        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

        private void getTrailerDataFromJson(String trailerJsonStr) throws JSONException {
            //get the root "result" array
            JSONObject trailerObject = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = trailerObject.getJSONArray("results");
            //base Url for the TrailerInfo
            final String youTubeBaseUrl = "https://www.youtube.com/watch?v=";

            for (int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                if (trailer.getString("site").contentEquals("YouTube")) {
                    movieTrailerListKey.add(i, trailer.getString("key"));
                    movieTrailerListName.add(i, trailer.getString("name"));
                }
                Log.i(LOG_TAG, " YouTube URL is: " + youTubeBaseUrl + movieTrailerListKey.get(i));
                Log.i(LOG_TAG, " YouTube Trailer name is: " + movieTrailerListName.get(i));
            }
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // contain the raw JSON response as as string
            String trailerDataStr = null;
            try {
                //construct URL
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + currentMovieId + "/videos";

                Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter("api_key", API_KEY)
                        .build();

                URL url = new URL(buildUri.toString());
                Log.e(LOG_TAG, "Trailer url is " + url);

                //Create the request to Moviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                //Reading input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    //nothing to do
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    //empty stream. No parsing
                    return null;
                }
                trailerDataStr = buffer.toString();

            } catch (IOException e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                getTrailerDataFromJson(trailerDataStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            final ArrayList<Trailer> trailers = new ArrayList<>();
            final String youTubeBaseUrl = "https://www.youtube.com/watch?v=";

            // get all member variables in place
            for (int i = 0; i < movieTrailerListKey.size(); i++) {
                Trailer trailer = new Trailer();
                trailer.name = movieTrailerListName.get(i);
                trailer.key = movieTrailerListKey.get(i);
                trailers.add(trailer);
            }
            Log.i(LOG_TAG, "Result trailer url is:   " + youTubeBaseUrl + movieTrailerListKey);
            Log.i(LOG_TAG, "Result trailer name is:   " + movieTrailerListName);

            RecyclerView trailerView = (RecyclerView) findViewById(R.id.trailer_view);
            trailerView.setHasFixedSize(true);
            trailerView.setNestedScrollingEnabled(false);
            trailerView.setItemAnimator(new DefaultItemAnimator());
            final TrailerAdapter mTrailerAdapter = new TrailerAdapter(getApplicationContext(), trailers);
            trailerView.setAdapter(mTrailerAdapter);
        }
    }

    // Add FetchReviewTask
    public class FetchReviewTask extends AsyncTask<String, Void, String> {
        Intent intent = getIntent();
        Movie currentMovie = intent.getParcelableExtra("movie");
        String movieId = currentMovie.getmId();

        private final String LOG_TAG = FetchReviewTask.class.getSimpleName();

        private void getReviewDataFromJson(String reviewJsonStr) throws JSONException {
            //get the root "result" array
            JSONObject reviewObject = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewObject.getJSONArray("results");

            for (int i = 0; i < reviewArray.length(); i++) {
                JSONObject review = reviewArray.getJSONObject(i);
                movieReviewList.add(i, review.getString("content"));
            }
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // contain the raw JSON response as as string
            String reviewDataStr = null;
            try {
                //construct URL
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + movieId + "/reviews";

                Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter("api_key", API_KEY)
                        .build();

                URL url = new URL(buildUri.toString());
                Log.e(LOG_TAG, "Review url is " + url);

                //Create the request to Moviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                //Reading input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    //nothing to do
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                if (buffer.length() == 0) {
                    //empty stream. No parsing
                    return null;
                }
                reviewDataStr = buffer.toString();

            } catch (IOException e) {
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                getReviewDataFromJson(reviewDataStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(LOG_TAG, "Result review is:   " + movieReviewList);

            ArrayList<Review> reviews = new ArrayList<>();

            for (int i = 0; i < movieReviewList.size(); i++) {
                Review review = new Review();
                review.content = movieReviewList.get(i);
                reviews.add(review);
            }
            RecyclerView reviewView = (RecyclerView) findViewById(R.id.review_view);
            reviewView.setHasFixedSize(true);
            reviewView.setNestedScrollingEnabled(false);
            reviewView.setItemAnimator(new DefaultItemAnimator());
            final ReviewAdapter mReviewAdapter = new ReviewAdapter(this, reviews);
            reviewView.setAdapter(mReviewAdapter);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void saveFavorite() {
        Intent intent = getIntent();
        Movie currentMovie = intent.getParcelableExtra("movie");
        String id = currentMovie.getmId();
        String title = currentMovie.getmTitle();
        String moviePoster = currentMovie.getmMoviePoster();
        String overview = currentMovie.getmOverview();
        String rating = String.valueOf(currentMovie.getmRating());
        String releaseDate = currentMovie.getmReleaseDate();

        ContentValues contentValues = new ContentValues();

        contentValues.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID, id);
        contentValues.put(MovieContract.FavoriteEntry.COLUMN_TITLE, title);
        contentValues.put(MovieContract.FavoriteEntry.COLUMN_POSTER, moviePoster);
        contentValues.put(MovieContract.FavoriteEntry.COLUMN_OVERVIEW, overview);
        contentValues.put(MovieContract.FavoriteEntry.COLUMN_RATING, rating);
        contentValues.put(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE, releaseDate);

        Uri newUri = getContentResolver().insert(MovieContract.FavoriteEntry.CONTENT_URI, contentValues);

        Log.v("MovieDetail", "savedFavorite method, new row Uri is " + newUri);

        // insertion was successful and we can display a toast.
        Toast.makeText(this, R.string.successfully_added, Toast.LENGTH_SHORT).show();
        }

    private void deleteFavorite() {
        Uri mCurrentFavoriteUri = MovieContract.FavoriteEntry.CONTENT_URI;
        Intent intent = getIntent();
        Movie currentMovie = intent.getParcelableExtra("movie");
        int currentMovieId = Integer.valueOf(currentMovie.getmId());
        if (mCurrentFavoriteUri != null) {

                //Call the ContentResolver to delete the pet at the given content URI.
            //Pass in null for the selection and selection args because the mCurrentPetUri
            //content URI already identifies the pet that we want
            int rowsDeleted = getContentResolver().delete(MovieContract.FavoriteEntry
                    .buildFavMovieUri(currentMovieId), null, null);
            Log.d("DetailActivity", "deleteFavorite: Deleted: " + rowsDeleted);

            //Show toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0){
                //If no rows deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_favorite_failed),
                        Toast.LENGTH_SHORT).show();
            }else {
                //otherwise, the delete was successful and we can display a toast
                Toast.makeText(this, getString(R.string.delete_favorite_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        //Close the activity
        finish();
    }

    // Helper method to assist in determining if the movie is already in the favorites table
    public boolean movieIsFavorite() {
        Intent intent = getIntent();
        Movie currentMovie = intent.getParcelableExtra("movie");
        String title = currentMovie.getmTitle();

        Log.i(LOG_TAG, "Movie TITLE is " + title);

        // This will perform a query on the favorites table to return a Cursor containing that
        // row of the table.
        mCursor = getContentResolver().query(MovieContract.FavoriteEntry
                .buildFavMovieUri(id), null, null, null, null);

        assert mCursor != null;
        if (mCursor.moveToNext()) {
            //movie exists in database, set button to red
            Log.d("MovieDetail", "MovieIsFavorite Method: Movie already a favorite");
            final SwitchIconView favoriteSwitch = (SwitchIconView) findViewById(R.id.button_favorite);
            favoriteSwitch.switchState(true);
//            favoriteSwitch.setImageResource(R.drawable.ic_redheart);
            mCursor.close();
            return false;
        } else {
            //no movie returned, this must not exist in database.  Set button to white
            Log.d("MovieDetail", "MovieIsFavorite Method: Movie not yet a favorite");
            final SwitchIconView favoriteSwitch = (SwitchIconView) findViewById(R.id.button_favorite);
            favoriteSwitch.switchState(false);
//            favoriteSwitch.setImageResource(R.drawable.ic_whiteheart);
            mCursor.close();
            return true;
        }
    }

    /**
     * Detail Menu Handling. Options: simple share of movie, or delete from favorites table.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }
    /**
     * Callback invoked when a menu item was selected from this Activity's menu. Android will
     * automatically handle clicks on the "up" button for us so long as we have specified
     * DetailActivity's parent Activity in the AndroidManifest.
     *
     * @param item The menu item that was selected by the user
     * @return true if you handle the menu click here, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       /* Settings menu item clicked */

        /* Get the ID of the clicked item */
            switch (item.getItemId()) {

        /* Share menu item clicked */
                case R.id.action_share:
                    Intent shareIntent = createShareMovieIntent();
                    startActivity(shareIntent);
                    //exit activity
                    finish();
                    return true;

            /* Delete menu item clicked */
                case R.id.action_delete_favorite:
                    deleteFavorite();
                    //exit activity
                    finish();
                    return true;
            }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Uses the ShareCompat Intent builder to create our movie intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
     *
     * @return the Intent to use to share our movie title
     */
    private Intent createShareMovieIntent() {
        Intent intent = getIntent();
        Movie currentMovie = intent.getParcelableExtra("movie");
        String title = currentMovie.getmTitle();
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(title + MOVIE_SHARE_HASHTAG)
                .getIntent();
        Log.i(LOG_TAG, "MOVIE TITLE IS " + title);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }
}















