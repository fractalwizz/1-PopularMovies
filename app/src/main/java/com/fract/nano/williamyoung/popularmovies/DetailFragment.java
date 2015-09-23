package com.fract.nano.williamyoung.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DetailFragment extends Fragment {
    public DetailFragment() {}

    public static final String DETAILFRAGMENT_TAG = "DFTAG";

    public final static String RUNTIME_KEY = "runtime";
    public final static String TRAILER_KEY = "trailer";
    public final static String REVIEW_KEY = "review";
    public final static String FAV_KEY = "favorite";

    private ShareActionProvider mShareActionProvider;
    private ArrayList<Trailer> mTrailer;
    private ArrayList<Review> mReview;
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;
    private Film mFilm;
    private ListView trailerView;
    private ListView reviewView;
    private TextView titleView;
    private ImageView posterView;
    private TextView releaseView;
    private int runtime;
    private TextView runtimeView;
    private TextView ratingView;
    private Button favButton;
    private TextView synopsisView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        setHasOptionsMenu(true);

        if (arguments != null) {
            mFilm = arguments.getParcelable(MainActivityFragment.FILM_KEY);

            View view = inflater.inflate(R.layout.fragment_detail, container, false);
            trailerView = (ListView) view.findViewById(R.id.listview_trailer);

            View headerView = inflater.inflate(R.layout.header, null, false);
            trailerView.addHeaderView(headerView, null, false);
            View footerView = inflater.inflate(R.layout.footer, null, false);
            trailerView.addFooterView(footerView, null, false);

            trailerAdapter = new TrailerAdapter(getActivity(), null);

            reviewView = (ListView) footerView.findViewById(R.id.listview_reviews);
            reviewAdapter = new ReviewAdapter(getActivity(), null);

            titleView = (TextView) headerView.findViewById(R.id.detail_original_title);
            titleView.setText(mFilm.getTitle());

            posterView = (ImageView) headerView.findViewById(R.id.detail_poster);
            Picasso.with(getActivity())
                    .load(mFilm.getPoster())
                    .placeholder(R.drawable.placehold)
                    .error(R.drawable.error)
                    .resize(260,360)
                    .into(posterView);

            releaseView = (TextView) headerView.findViewById(R.id.detail_release_date);
            releaseView.setText(mFilm.getRelease());

            runtimeView = (TextView) headerView.findViewById(R.id.detail_runtime);

            ratingView = (TextView) headerView.findViewById(R.id.detail_user_rating);
            ratingView.setText(String.valueOf(mFilm.getRating()) + "/10");

            favButton = (Button) headerView.findViewById(R.id.detail_fav_btn);
            favButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getActivity();

                    new AddFavTask().execute(mFilm);
                    Toast.makeText(context, mFilm.getTitle() + " added to Favorites", Toast.LENGTH_SHORT).show();
                }
            });

            synopsisView = (TextView) headerView.findViewById(R.id.detail_synopsis);
            synopsisView.setText(mFilm.getSynopsis());

            return view;
        }

        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.detail_fragment, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mTrailer != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTrailer.get(0).getUrl());

        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(TRAILER_KEY)) {
            mTrailer = savedInstanceState.getParcelableArrayList(TRAILER_KEY);
            mReview = savedInstanceState.getParcelableArrayList(REVIEW_KEY);

            runtime = savedInstanceState.getInt(RUNTIME_KEY);
            runtimeView.setText(String.valueOf(runtime) + "min");

            trailerAdapter = new TrailerAdapter(getActivity(), mTrailer);
            trailerView.setAdapter(trailerAdapter);
            trailerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Trailer trailer = (Trailer) parent.getItemAtPosition(position);

                    Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getUrl()));
                    startActivity(trailerIntent);
                }
            });

            reviewAdapter = new ReviewAdapter(getActivity(), mReview);
            reviewView.setAdapter(reviewAdapter);
        } else {
            if (mFilm != null) {
                new FetchDetailTask().execute(String.valueOf(mFilm.getID()));
            }
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(RUNTIME_KEY, runtime);
        savedInstanceState.putParcelableArrayList(TRAILER_KEY, mTrailer);
        savedInstanceState.putParcelableArrayList(REVIEW_KEY, mReview);
        super.onSaveInstanceState(savedInstanceState);
    }

    public class FetchDetailTask extends AsyncTask<String, Void, Combine> {
        private final String LOG_TAG = FetchDetailTask.class.getSimpleName();

        private Combine getDetailDataFromJson(String detailJsonStr) throws JSONException {
            final String OWM_TRAILER = "trailers";
            final String OWM_YOUTUBE = "youtube";
            final String OWM_NAME = "name";
            final String OWM_SOURCE = "source";
            final String OWM_RUNTIME = "runtime";
            final String OWM_REVIEW = "reviews";
            final String OWM_RESULTS = "results";
            final String OWM_AUTHOR = "author";
            final String OWM_CONTENT = "content";

            JSONObject detailJson = new JSONObject(detailJsonStr);

            runtime = detailJson.getInt(OWM_RUNTIME);

            JSONObject trailers = detailJson.getJSONObject(OWM_TRAILER);
            JSONArray youtubeArray = trailers.getJSONArray(OWM_YOUTUBE);
            ArrayList<Trailer> resultTrailer = new ArrayList<>();

            for (int i = 0; i < youtubeArray.length(); i++) {
                Trailer trailer = new Trailer();

                JSONObject singleTrailer = youtubeArray.getJSONObject(i);

                String title = singleTrailer.getString(OWM_NAME);
                trailer.setTitle(title);

                String url = singleTrailer.getString(OWM_SOURCE);
                String youUrl = "https://www.youtube.com/watch?v=";
                trailer.setUrl(youUrl + url);

                resultTrailer.add(i, trailer);
            }

            JSONObject reviews = detailJson.getJSONObject(OWM_REVIEW);
            JSONArray reviewArray = reviews.getJSONArray(OWM_RESULTS);
            ArrayList<Review> resultReview = new ArrayList<>();

            for (int j = 0; j < reviewArray.length(); j++) {
                Review review = new Review();

                JSONObject singleReview = reviewArray.getJSONObject(j);

                String name = singleReview.getString(OWM_AUTHOR);
                review.setAuthor(name);

                String content = singleReview.getString(OWM_CONTENT);
                review.setReview(content);

                resultReview.add(j, review);
            }

            Combine combine = new Combine();
            combine.setTrailers(resultTrailer);
            combine.setReviews(resultReview);
            combine.setRuntime(runtime);

            return combine;
        }

        @Override
        protected Combine doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String detailJsonStr = null;

            String apiKey = Utility.getAPIKey();
            String filmID = params[0];

            try {
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/" + filmID + "?";
                final String KEY_PARAM = "api_key";
                final String APPEND_PARAM = "append_to_response";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, apiKey)
                        .appendQueryParameter(APPEND_PARAM, "trailers,reviews")
                        .build();
                URL url = new URL(builtUri.toString());
                Log.w("URL constructed", builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                detailJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "ERROR ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "ERROR closing stream", e);
                    }
                }
            }

            try {
                return getDetailDataFromJson(detailJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(Combine combine) {
            //TODO fix for possible offline viewing of cached-only information
            mTrailer = combine.getTrailers();
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
            mReview = combine.getReviews();

            runtimeView.setText(combine.getRuntime());

            trailerAdapter = new TrailerAdapter(getActivity(), mTrailer);
            trailerView.setAdapter(trailerAdapter);
            trailerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Trailer trailer = (Trailer) parent.getItemAtPosition(position);

                    Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getUrl()));
                    startActivity(trailerIntent);
                }
            });

            reviewAdapter = new ReviewAdapter(getActivity(), mReview);
            reviewView.setAdapter(reviewAdapter);
        }
    }

    // SharedPreference Code adapted from http://stackoverflow.com/questions/22984696/storing-array-list-object-in-sharedpreferences

    public class AddFavTask extends AsyncTask<Object, Void, Void> {
        private final String LOG_TAG = AddFavTask.class.getSimpleName();
        private String json;
        private ArrayList<Film> fav;

        @Override
        protected Void doInBackground(Object... params) {
            if (params.length == 0) {
                return null;
            }

            if (params[0] instanceof Film) {
                Film newFav = (Film) params[0];

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPref.edit();

                Gson gson = new Gson();

                if (sharedPref.contains(FAV_KEY)) {
                    json = sharedPref.getString(FAV_KEY, null);
                    Type type = new TypeToken<ArrayList<Film>>() {}.getType();
                    fav = gson.fromJson(json, type);
                } else {
                    fav = new ArrayList<>();
                }

                fav.add(newFav);

                json = gson.toJson(fav);
                editor.putString(FAV_KEY, json);
                editor.apply();
            }

            return null;
        }
    }
}