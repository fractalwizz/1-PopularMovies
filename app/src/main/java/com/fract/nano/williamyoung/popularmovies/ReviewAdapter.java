package com.fract.nano.williamyoung.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends ArrayAdapter<Review> {
    private ArrayList<Review> mReview = new ArrayList<>();
    private Context mContext;

    public ReviewAdapter(Context context, ArrayList<Review> review) {
        super(context, 0, review);
        mContext = context;
        mReview = review;
    }

    public void setReview(ArrayList<Review> result) {
        mReview.clear();
        mReview.addAll(result);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        if (!mReview.isEmpty()) {
            Review review = mReview.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_review, parent, false);
            }

            TextView authorView = (TextView) convertView.findViewById(R.id.list_item_author);
            authorView.setText(review.getAuthor() + ":");

            TextView reviewView = (TextView) convertView.findViewById(R.id.list_item_review);
            reviewView.setText(review.getReview());

            return convertView;
        }

        return convertView;
    }
}
