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

public class TrailerAdapter extends ArrayAdapter<Trailer> {
    private ArrayList<Trailer> mTrailer = new ArrayList<>();
    private Context mContext;

    public TrailerAdapter(Context context, ArrayList<Trailer> trailer) {
        super(context, 0, trailer);
        mContext = context;
        mTrailer = trailer;
    }

    public void setTrailer(ArrayList<Trailer> result) { mTrailer = result; }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        if (!mTrailer.isEmpty()) {
            Trailer trailer = mTrailer.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_trailer, parent, false);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_icon);
            imageView.setImageResource(R.drawable.trailer);

            TextView textView = (TextView) convertView.findViewById(R.id.list_item_trailer);
            textView.setText(trailer.getTitle());

            return convertView;
        }

        return convertView;
    }
}
