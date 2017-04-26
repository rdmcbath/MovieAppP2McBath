package com.example.android.moviesnow.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * {@link ContentProvider} for the app.
 *
 */
//database helper object
public class MovieProvider extends ContentProvider {

    public static final String LOG_TAG = MovieProvider.class.getSimpleName();

    //database helper object
    private MovieDbHelper mDbHelper;

    //Codes for the UriMatcher
    static final int FAVORITE = 100;
    static final int FAVORITE_WITH_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_FAVORITE, FAVORITE);
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_FAVORITE + "/#", FAVORITE_WITH_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query (Uri uri,String[] projection, String selection, String[] selectionArgs,
                         String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FAVORITE:
                // For the Favorites code, query the favorites table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the favorites table.
                cursor = database.query(MovieContract.FavoriteEntry.TABLE_NAME, projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case FAVORITE_WITH_ID:
                // For the FAVORITE_WITH_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.favorites/favorites/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = MovieContract.FavoriteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the favorites table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(MovieContract.FavoriteEntry.TABLE_NAME, projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //set notification URI on the cursor, so we know what content URI the cursor was created for
        //If the data at this URI changes, then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;

    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FAVORITE:
                return insertFavorite(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    /**
     * Insert a favorite into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertFavorite(Uri uri, ContentValues values) {

        // Check that the title is not null
        String title = values.getAsString(MovieContract.FavoriteEntry.COLUMN_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Movie requires a title");
        }
        // Check that the poster is not null
        String moviePoster = values.getAsString(MovieContract.FavoriteEntry.COLUMN_POSTER);
        if (moviePoster == null) {
            throw new IllegalArgumentException("Movie requires a poster");
        }
        // Check that the overview is not null
        String overview = values.getAsString(MovieContract.FavoriteEntry.COLUMN_OVERVIEW);
        if (overview == null) {
            throw new IllegalArgumentException("Movie requires an overview");
        }
        // Check that the rating is not null
        String rating = values.getAsString(MovieContract.FavoriteEntry.COLUMN_RATING);
        if (title == rating) {
            throw new IllegalArgumentException("Movie requires a rating");
        }
        // Check that the release date is not null
        String releaseDate = values.getAsString(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE);
        if (releaseDate == null) {
            throw new IllegalArgumentException("Movie requires a release date");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new favorite with the given values
        long id = database.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //Notify all listeners that the favorite data has changed, for the favorite content URI.
        //uri: content://com.example.android.favorites/favorites
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FAVORITE:
                return updateFavorite(uri, contentValues, selection, selectionArgs);
            case FAVORITE_WITH_ID:
                // For the FAVORITE_WITH_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = MovieContract.FavoriteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateFavorite(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    /**
     * Update favorites in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more movies).
     * Return the number of rows that were successfully updated.
     */
    private int updateFavorite(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link FavoriteEntry#COLUMN_TITLE} key is present,
        // check that the title value is not null.
        if (values.containsKey(MovieContract.FavoriteEntry.COLUMN_TITLE)) {
            String title = values.getAsString(MovieContract.FavoriteEntry.COLUMN_TITLE);
            if (title == null) {
                throw new IllegalArgumentException("Movie requires a name");
            }
        }
        // If the {@link FavoriteEntry#COLUMN_POSTER} key is present,
        // check that the poster value is not null.
        if (values.containsKey(MovieContract.FavoriteEntry.COLUMN_POSTER)) {
            String poster = values.getAsString(MovieContract.FavoriteEntry.COLUMN_POSTER);
            if (poster == null) {
                throw new IllegalArgumentException("Movie requires a poster");
            }
        }
        // If the {@link FavoriteEntry#COLUMN_OVERVIEW} key is present,
        // check that the overview value is not null.
        if (values.containsKey(MovieContract.FavoriteEntry.COLUMN_OVERVIEW)) {
            String overview = values.getAsString(MovieContract.FavoriteEntry.COLUMN_OVERVIEW);
            if (overview == null) {
                throw new IllegalArgumentException("Movie requires an overview");
            }
        }
        // If the {@link FavoriteEntry#COLUMN_RATING} key is present,
        // check that the rating value is not null.
        if (values.containsKey(MovieContract.FavoriteEntry.COLUMN_RATING)) {
            String rating = values.getAsString(MovieContract.FavoriteEntry.COLUMN_RATING);
            if (rating == null) {
                throw new IllegalArgumentException("Movie requires a rating");
            }
        }
        // If the {@link FavoriteEntry#COLUMN_RELEASE_DATE} key is present,
        // check that the releaseDate value is not null.
        if (values.containsKey(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE)) {
            String releaseDate = values.getAsString(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE);
            if (releaseDate == null) {
                throw new IllegalArgumentException("Movie requires an overview");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(MovieContract.FavoriteEntry.TABLE_NAME,
                values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Notify all listeners that the favorite data has changed, for the favorite content URI.
        //uri: content://com.example.android.favorites/favorites
        getContext().getContentResolver().notifyChange(uri, null);

        // Track the number of rows that were deleted
        int rowsDeleted = 0;

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);

            // Return the number of rows deleted
            return rowsDeleted;
        }

        final int match = sUriMatcher.match(uri);

        int deleteRows;

        switch (match) {
            case FAVORITE:
                // Delete all rows that match the selection and selection args
                deleteRows = database.delete(MovieContract.FavoriteEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            case FAVORITE_WITH_ID:
                // Delete a single row given by the ID in the URI
                selection = MovieContract.FavoriteEntry._ID + "=?";

                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Delete a single row given by the ID in the URI
                deleteRows = database.delete(MovieContract.FavoriteEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (deleteRows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleteRows;
    }

    @Override
    public int bulkInsert (Uri uri, ContentValues [] values) {

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FAVORITE:
                // allows for multiple transactions
                db.beginTransaction();
                // keep track of successful inserts
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);

                return returnCount;
            default:
                return super.bulkInsert(uri, values);

        }
    }

/**
 * Returns the MIME type of data for the content URI.
 */
            @Override
            public String getType (Uri uri){
                final int match = sUriMatcher.match(uri);
                switch (match) {
                    case FAVORITE:
                        return MovieContract.FavoriteEntry.CONTENT_LIST_TYPE;
                    case FAVORITE_WITH_ID:
                        return MovieContract.FavoriteEntry.CONTENT_ITEM_TYPE;
                    default:
                        throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
                }
            }
        }
