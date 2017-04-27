package com.example.android.moviesnow.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class MovieContract {
    public static final String CONTENT_AUTHORITY ="com.example.android.moviesnow";
    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_FAVORITE = "favorite";

    private MovieContract(){
    }
    /**
     * Inner class that defines constant values for the favorites database table.
     * Each entry in the table represents a single movie.
     */
    public static final class FavoriteEntry implements BaseColumns {
        //inside each of the Entry classes in the contract, we create a full URI for the class as a
        //constant called CONTENT_URI. The Uri.withAppendedPath() method appends the BASE_CONTENT_URI
        //which contains the scheme and the content authority) to the path segment.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FAVORITE);

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_ID = BaseColumns._ID;
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "moviePoster";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";

        public static Uri buildFavMovieUri(long id) {
            Log.d(TAG, "buildFavMovieUri: " + id);
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getMovieCategoryOrIDFromUri(Uri movieUriWithCategory) {
            Log.d(TAG, "getMovieCategoryOrIDFromUri: " + movieUriWithCategory);
            String category = movieUriWithCategory.getPathSegments().get(1);
            Log.d(TAG, "getMovieCategoryOrIDFromUri: " + category);
            return category;
        }

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of favorites.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single favorite.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

    }
}