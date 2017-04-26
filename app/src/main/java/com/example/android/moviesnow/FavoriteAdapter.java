package com.example.android.moviesnow;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.moviesnow.data.MovieContract;
import com.squareup.picasso.Picasso;


class FavoriteAdapter extends CursorAdapter {
    private static final String LOG_TAG = FavoriteAdapter.class.getSimpleName();
    private Context mContext;

    /**
     * Constructs a new {@link FavoriteAdapter}.
     *  @param context     The context
     * @param cursor      The cursor from which to get the data.
     */

    FavoriteAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0/* flags */);
        this.mContext = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        return LayoutInflater.from(context).inflate(R.layout.favorite_grid_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        int posterIndex = cursor.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_POSTER);
        final String moviePoster = cursor.getString(posterIndex);
        final ImageView iconView = (ImageView) view.findViewById(R.id.androidFavoriteMovieImage);
        String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
        String POSTER_WIDTH = "w185";
        final String FULL_POSTER_URL = POSTER_BASE_URL + POSTER_WIDTH + moviePoster;
        Picasso.with(mContext).load(FULL_POSTER_URL).into(iconView);

        cursor.getPosition();

        iconView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                int idIndex = cursor.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID);
                final String id = cursor.getString(idIndex);

                int titleIndex = cursor.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_TITLE);
                final String title = cursor.getString(titleIndex);

                int overviewIndex = cursor.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_OVERVIEW);
                final String overview = cursor.getString(overviewIndex);

                int ratingIndex = cursor.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_RATING);
                final double rating = cursor.getDouble(ratingIndex);

                int releaseDateIndex = cursor.getColumnIndex(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE);
                final String releaseDate = cursor.getString(releaseDateIndex);

                Movie currentFavorite = new Movie(id, title, moviePoster, overview, rating, releaseDate);

                Intent intent = new Intent(context, MovieDetail.class);
                intent.putExtra("movie", currentFavorite);

                context.startActivity(intent);
            }
        });
    }
}












