package com.fract.nano.williamyoung.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Trailer implements Parcelable {
    private String title;
    private String url;

    public String getTitle (){ return title; }

    public void setTitle (String title) { this.title = title; }

    public String getUrl () { return url; }

    public void setUrl (String url) { this.url = url; }

    public static Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel source) {
            Trailer mTrailer = new Trailer();

            mTrailer.title = source.readString();
            mTrailer.url = source.readString();

            return mTrailer;
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(title);
        parcel.writeString(url);
    }
}