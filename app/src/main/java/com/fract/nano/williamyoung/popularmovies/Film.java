package com.fract.nano.williamyoung.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Film implements Parcelable {
    private int ID;
    private String title;
    private String synopsis;
    private float rating;
    private String release;
    private String poster;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getRelease() { return release.substring(0,4); }

    public String getFullRelease() { return release; }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public static Parcelable.Creator<Film> CREATOR = new Creator<Film>() {
        @Override
        public Film createFromParcel(Parcel source) {
            Film mFilm = new Film();

            mFilm.ID = source.readInt();
            mFilm.title = source.readString();
            mFilm.synopsis = source.readString();
            mFilm.rating = source.readFloat();
            mFilm.release = source.readString();
            mFilm.poster = source.readString();

            return mFilm;
        }

        @Override
        public Film[] newArray(int size) {
            return new Film[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(ID);
        parcel.writeString(title);
        parcel.writeString(synopsis);
        parcel.writeFloat(rating);
        parcel.writeString(release);
        parcel.writeString(poster);
    }
}
