package com.example.android.moviesnow;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is meant to hold static variables and methods, which can be accessed
 * directly from the class named MovieUtils
 */
class MovieUtils {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MovieUtils.class.getSimpleName();

    /**
     * Query the MovieDB dataset and return a list of {@link Movie} objects.
     */
    static List<Movie> fetchMovieData(String requestUrl) {
        Log.i(LOG_TAG, requestUrl);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "TEST: Problem making the HTTP request", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Movie} object
        // Return the list of {@link newsItems}
        return extractFeatureFromJson(jsonResponse);
    }

    /**
     * Return a list of {@link Movie} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<Movie> extractFeatureFromJson(String MovieDbJSON) {

        {
            // If the JSON string is empty or null, then return early.
            if (TextUtils.isEmpty(MovieDbJSON)) {
                return null;
            }

            // Create an empty ArrayList that we can start adding movie items to
            List<Movie> movieItems = new ArrayList<>();

            // Try to parse the JSON Response string. If there's a problem with the way the JSON
            // is formatted, a JSONException exception object will be thrown.
            // Catch the exception so the app doesn't crash, and print the error message to the logs.
            try {
                JSONObject JsonObject = new JSONObject(MovieDbJSON);

                // Extract the JSONArray associated with the key called "results",
                JSONArray movieResults = JsonObject.getJSONArray("results");

                int totalItems = JsonObject.optInt("totalItems");

                Log.v("TEST", "TEST" + totalItems);

                // If there are items in the results array
                for (int i = 0; i < movieResults.length(); i++) {

                    // Extract out the first result (which is a movie result)
                    JSONObject ArrayObject = movieResults.getJSONObject(i);

                    //Extract out the Id
                    String movieId = ArrayObject.getString("id");

                    // Extract out the title
                    String movieTitle = ArrayObject.getString("title");

                    //Extract out the release date
                    String movieReleaseDate = ArrayObject.getString("release_date");

                    //Extract out the overview
                    String movieOverview = ArrayObject.getString("overview");

                    //Extract out the average rating
                    Double movieVoteAverage = ArrayObject.getDouble("vote_average");

                    //Extract out the movie poster image
                    String moviePosterPath = ArrayObject.getString("poster_path");

                    StringBuilder formattedDate = new StringBuilder(movieReleaseDate);
                    for (int j = 0; j < movieReleaseDate.length(); j++) {
                        if (movieReleaseDate.charAt(j) == 'T' || movieReleaseDate.charAt(j) == 'Z')
                            formattedDate.setCharAt(j, ' ');
                    }
 //                   formattedDate = new SimpleDateFormat("yyyy");
                    movieReleaseDate = formattedDate.toString();

                    // Create a new {@link movieItem} object
                    Movie movieItem = new Movie(movieId, movieTitle, moviePosterPath, movieOverview,
                            movieVoteAverage, movieReleaseDate);

                    movieItems.add(movieItem);
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "UTILS: Problem parsing TMDb JSON results", e);
            }
            return movieItems;
        }
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {

        URL url;
        try {

            url = new URL(stringUrl);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "UTILS: Error with creating URL ", e);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If request was successful (response code 200), read input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the TMDb JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the whole
     * JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}


