package com.example.android.moviesnow;

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.moviesnow.data.MovieContract;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = FavoriteActivity.class.getName();

    /**
     * Content URI for the existing favorites data loader
     */
    private Uri mCurrentFavoriteUri = MovieContract.FavoriteEntry.CONTENT_URI;
    private static final int FAVORITE_CURSOR_LOADER_ID = 2;
    private FavoriteAdapter mCursorAdapter;
    private ProgressBar spinnerProgress;
    List<Movie> movie = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_grid);

        // Initialize the adapter that takes data and populates the GridView attached to it
        mCursorAdapter = new FavoriteAdapter(this, null);
        // Find a reference to the {@link GridView} in the layout
        GridView gridItemView = (GridView) findViewById(R.id.favorite_grid_view);
        // Set the adapter to the GridView
        gridItemView.setAdapter(mCursorAdapter);
        // Create an empty TextView to display message when no data returned from GET request
        TextView mEmptyStateTextView = (TextView) gridItemView.findViewById(R.id.empty_view);
        gridItemView.setEmptyView(mEmptyStateTextView);

        spinnerProgress = (ProgressBar) findViewById(R.id.loading_spinner);
        spinnerProgress.setVisibility(View.VISIBLE);

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        getSupportLoaderManager().initLoader(FAVORITE_CURSOR_LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("favoritesList", (ArrayList<? extends Parcelable>) movie);
        super.onSaveInstanceState(outState);
    }

    /**
     * Helper method to delete all favorites in the database.
     */
    private void deleteAllFavorites() {
        int rowsDeleted = getContentResolver().delete(MovieContract
                .FavoriteEntry.CONTENT_URI, null, null);

        Log.v("CatalogActivity", rowsDeleted + " rows deleted from favorites");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "TEST: ONRESUME");

        getSupportLoaderManager().restartLoader(FAVORITE_CURSOR_LOADER_ID, null, this);
    }

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
    final String[] projection = new String[] {
            MovieContract.FavoriteEntry._ID,
            MovieContract.FavoriteEntry.COLUMN_TITLE,
            MovieContract.FavoriteEntry.COLUMN_POSTER,
            MovieContract.FavoriteEntry.COLUMN_OVERVIEW,
            MovieContract.FavoriteEntry.COLUMN_RATING,
            MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Log.i(LOG_TAG, "TEST: OnSTART LOADING");

        // create and return a CursorLoader that will take care of creating a Cursor
        // for the data being displayed.
        return new android.support.v4.content.CursorLoader(this, mCurrentFavoriteUri,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "TEST:OnLOAD FINISHED");

        // Swap the new cursor in. (The framework will take care of closing the old cursor
        // once we return.)
        mCursorAdapter.swapCursor(data);

        // Hide loading indicator because the data has been loaded
        spinnerProgress.setVisibility(View.GONE);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "TEST:OnLOAD RESET");
        // This is called when the last Cursor provided to onLoadFinished() above is about to be
        // closed.  We need to make sure we are no longer using it.
        mCursorAdapter.swapCursor(null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorites_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete" menu option
            case R.id.delete_all_movies:
                //Pop up confirmation dialog for deletion
                showWarningDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Prompt the user to confirm that they want to delete all favorites.
     */
    private void showWarningDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.warning_dialog_msg_all_movies);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Yes" button, so delete all the favorites.
                deleteAllFavorites();
                showToast();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "No" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showToast() {
        // display toast confirming successful deletion of all favorites
        Toast.makeText(this, R.string.delete_all_favorites_successful, Toast.LENGTH_SHORT).show();
    }
}







