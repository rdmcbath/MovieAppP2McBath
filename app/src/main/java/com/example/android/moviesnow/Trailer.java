package com.example.android.moviesnow;


import android.os.Parcel;
import android.os.Parcelable;

class Trailer implements Parcelable {

    public static final String PARCEL_TAG = "trailer_tag";


    public final static Parcelable.Creator<Trailer> CREATOR = new Creator<Trailer>() {


        public static final String PARCEL_TAG = "trailer_tag";

        public Trailer createFromParcel(Parcel in) {
            Trailer instance = new Trailer();
            instance.key = ((String) in.readValue((String.class.getClassLoader())));
            instance.site = ((String) in.readValue((String.class.getClassLoader())));
            instance.name = ((String) in.readValue((String.class.getClassLoader())));

            return instance;
        }

        public Trailer[] newArray(int size) {
            return (new Trailer[size]);
        }

    };

    public String name;
    public String key;
    public String site;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(key);
        dest.writeString(site);
    }

}
