package com.example.android.moviesnow;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    private String mTitle;
    private String mMoviePoster;
    private String mOverview;
    private Double mRating;
    private String mReleaseDate;
    private String mId;

    /**
     * Constructs a new {@link Movie}
     * @param id          is the id of the movie
     * @param title       is the title of the movie
     * @param releaseDate is the release date of the movie
     * @param overview    is the overview of the movie
     * @param rating      is the average rating of the movie
     * @param moviePoster is the url of the image of the movie poster
     */
    public Movie(String id, String title, String moviePoster, String overview, Double rating, String releaseDate) {
        mId = id;
        mTitle = title;
        mMoviePoster = moviePoster;
        mOverview = overview;
        mRating = rating;
        mReleaseDate = releaseDate;
    }

    //returns the id
    public String getmId() {return mId;}

    public void setmId(String mId) {
        this.mId = mId;
    }

    //returns the title
    public String getmTitle() {return mTitle;}

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    //returns the release date
    public String getmReleaseDate() {return mReleaseDate;}

    public void setmReleaseDate(String mReleaseDate) {
        this.mReleaseDate = mReleaseDate;
    }

    //returns the overview
    public String getmOverview() {return mOverview;}

    public void setmOverview(String mOverview) {
        this.mOverview = mOverview;
    }

    //returns the rating
    public Double getmRating() {return mRating;}

    public void setmRating(Double mRating) {
        this.mRating = mRating;
    }

    //returns the url image of the movie poster
    public String getmMoviePoster() {return mMoviePoster;}

    public void setmMoviePoster(String mMoviePoster) {
        this.mMoviePoster = mMoviePoster;
    }

    protected Movie(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mMoviePoster = in.readString();
        mOverview = in.readString();
        mRating = in.readDouble();
        mReleaseDate = in.readString();
            }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mMoviePoster);
        dest.writeString(mOverview);
        dest.writeDouble(mRating);
        dest.writeString(mReleaseDate);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {return new Movie[size];
        }
    };
}
