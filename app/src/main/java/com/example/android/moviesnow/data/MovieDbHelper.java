package com.example.android.moviesnow.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MovieDbHelper extends SQLiteOpenHelper {

    private static MovieDbHelper instance;

    public static synchronized MovieDbHelper getInstance(Context context)
    {
        if (instance == null)
            instance = new MovieDbHelper(context);
        return instance;
    }

    public static final String LOG_TAG = MovieDbHelper.class.getSimpleName();

    // Database version - in the event the database schema is changed
    private static final int DATABASE_VERSION = 13;

    public static final String DATABASE_NAME ="movies.db";

    //Construct a new instance of the {@link MovieDbHelper}
    //@param context of the app

    public MovieDbHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase sqLiteDatabase){

        // Create a String that contains the SQL statement to create the favorites table
        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " +
                MovieContract.FavoriteEntry.TABLE_NAME + " (" +
                MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.FavoriteEntry.COLUMN_TITLE +" TEXT NOT NULL, "+
                MovieContract.FavoriteEntry.COLUMN_POSTER + " TEXT NOT NULL, "+
                MovieContract.FavoriteEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieContract.FavoriteEntry.COLUMN_RATING + " REAL NOT NULL, " +
                MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL);";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);

        Log.i(LOG_TAG, "SQL Statement String is: " + SQL_CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.FavoriteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
