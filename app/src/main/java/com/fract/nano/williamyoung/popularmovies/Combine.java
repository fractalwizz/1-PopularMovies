package com.fract.nano.williamyoung.popularmovies;

import android.util.Log;

import java.util.ArrayList;

public class Combine {
    private ArrayList<Trailer> mTrailer;
    private ArrayList<Review> mReview;
    private int runtime;

    public ArrayList<Trailer> getTrailers() { return mTrailer; }

    public void setTrailers(ArrayList<Trailer> trailer) { this.mTrailer = trailer; }

    public ArrayList<Review> getReviews() { return mReview; }

    public void setReviews(ArrayList<Review> review) { this.mReview = review; }

    public String getRuntime() { return String.valueOf(runtime) + "min"; }

    public void setRuntime(int runtime) { this.runtime = runtime; }
}