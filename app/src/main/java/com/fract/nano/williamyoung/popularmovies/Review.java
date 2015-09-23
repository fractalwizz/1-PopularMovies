package com.fract.nano.williamyoung.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {
    private String name;
    private String review;

    public String getAuthor() { return name; }

    public void setAuthor(String name) { this.name = name; }

    public String getReview() { return review; }

    public void setReview(String review) { this.review = review; }

    public static Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel source) {
            Review mReview = new Review();

            mReview.name = source.readString();
            mReview.review = source.readString();

            return mReview;
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(review);
    }
}