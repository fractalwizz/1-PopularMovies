package com.fract.nano.williamyoung.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<Film> mFilm;
    private int size = 0;

    public ImageAdapter(Context context) {
        mContext = context;
    }

    public int getCount() { return size; }

    public Film getItem(int position) { return mFilm.get(position); }

    public long getItemId(int position) {
        return mFilm.get(position).getID();
    }

    public void setPoster(ArrayList<Film> result) {
        mFilm = result;

        if (mFilm != null) {
            size = mFilm.size();
        } else {
            size = 0;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }

        if (mFilm != null) {
            Film temp = mFilm.get(position);

            Picasso.with(mContext)
                    .load(temp.getPoster())
                    .placeholder(R.drawable.placehold)
                    .error(R.drawable.error)
                    .fit().centerCrop()
                    .into(imageView);
        }

        return imageView;
    }
}