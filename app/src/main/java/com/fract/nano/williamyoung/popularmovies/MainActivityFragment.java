package com.fract.nano.williamyoung.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ImageAdapter adapt;
    private GridView gridView;
    private ArrayList<Film> filmList;

    public final static String FILM_KEY = "film";
    public final static String FAV_KEY = "favorite";

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_main, menu);
    }

    public void updateMovie() {
        String order = Utility.getPreferredSort(getActivity());

        if (order.equals(getString(R.string.pref_sort_favorite))) {
            new getFavTask().execute();
            Toast.makeText(getActivity(), "Favorites Loaded", Toast.LENGTH_SHORT).show();
        } else {
            new FetchMovieTask().execute(order);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        adapt = new ImageAdapter(getActivity());

        gridView = (GridView) view.findViewById(R.id.gridview_poster);
        gridView.setAdapter(adapt);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Film toSend = filmList.get(position);

                Bundle bundle = new Bundle();
                bundle.putParcelable(FILM_KEY, toSend);

                if (Utility.getTwoPane()) {
                    DetailFragment twoPaneFragment = new DetailFragment();
                    twoPaneFragment.setArguments(bundle);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.film_detail_container, twoPaneFragment, DetailFragment.DETAILFRAGMENT_TAG)
                            .commit();
                } else {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(FILM_KEY)) {
            filmList = savedInstanceState.getParcelableArrayList(FILM_KEY);
            adapt.setPoster(filmList);
            adapt.notifyDataSetChanged();
        } else {
            updateMovie();
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(FILM_KEY, filmList);
        super.onSaveInstanceState(savedInstanceState);
    }

    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Film>> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private ArrayList<Film> getMovieDataFromJson(String movieJsonStr) throws JSONException {
            final String OWM_LIST = "results";
            final String OWM_ID = "id";
            final String OWM_TITLE = "original_title";
            final String OWM_SYNOPSIS = "overview";
            final String OWM_RATING = "vote_average";
            final String OWM_RELEASE = "release_date";
            final String OWM_POSTER = "poster_path";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_LIST);

            ArrayList<Film> resultFilm = new ArrayList<>();

            for (int i = 0; i < movieArray.length(); i++) {
                Film film = new Film();

                JSONObject singleMovie = movieArray.getJSONObject(i);

                int id = singleMovie.getInt(OWM_ID);
                film.setID(id);

                String title = singleMovie.getString(OWM_TITLE);
                film.setTitle(title);

                String synopsis = singleMovie.getString(OWM_SYNOPSIS);
                film.setSynopsis(synopsis);

                float rating = (float) singleMovie.getDouble(OWM_RATING);
                film.setRating(rating);

                String release = singleMovie.getString(OWM_RELEASE);
                film.setRelease(release);

                String poster = singleMovie.getString(OWM_POSTER);
                String postUrl = "http://image.tmdb.org/t/p/w185/";
                film.setPoster(postUrl + poster);

                resultFilm.add(i, film);
            }

            return resultFilm;
        }

        @Override
        protected ArrayList<Film> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;

            String apiKey = Utility.getAPIKey();
            String sort = params[0];

            try {
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String KEY_PARAM = "api_key";
                final String SORT_PARAM = "sort_by";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, apiKey)
                        .appendQueryParameter(SORT_PARAM, sort)
                        .build();
                URL url = new URL(builtUri.toString());

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
                movieJsonStr = buffer.toString();
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
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(ArrayList<Film> result) {
            filmList = result;
            adapt.setPoster(result);
            adapt.notifyDataSetChanged();
        }
    }

    // SharedPreference code adapted from http://stackoverflow.com/questions/22984696/storing-array-list-object-in-sharedpreferences

    public class getFavTask extends AsyncTask<Void, Void, ArrayList<Film>> {
        private final String LOG_TAG = getFavTask.class.getSimpleName();

        ArrayList<Film> fav;

        @Override
        protected ArrayList<Film> doInBackground(Void... params) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Gson gson = new Gson();

            if (sharedPref.contains(FAV_KEY)) {
                String json = sharedPref.getString(FAV_KEY, null);
                Type type = new TypeToken<ArrayList<Film>>() {}.getType();
                fav = gson.fromJson(json, type);

                return fav;
            }

            return null;
        }

        @Override
        public void onPostExecute(ArrayList<Film> result) {
            filmList = result;
            adapt.setPoster(result);
            adapt.notifyDataSetChanged();
        }
    }
}
