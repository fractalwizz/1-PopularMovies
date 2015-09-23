package com.fract.nano.williamyoung.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;


public class MainActivity extends AppCompatActivity {

    private String mSort;
    private boolean mTwoPane;
    public final static String FAV_KEY = "favorite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSort = Utility.getPreferredSort(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.film_detail_container) != null) {
            mTwoPane = true;
            Utility.setTwoPane(mTwoPane);
            Log.w("MainActivity", "Choosing dual-pane");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent setIntent = new Intent(this, SettingsActivity.class);
            startActivity(setIntent);

            return true;
        }
        if (id == R.id.action_clear) {
            new ClearFavTask().execute();
            Toast.makeText(this, "Favorites Cleared", Toast.LENGTH_SHORT).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        String sort = Utility.getPreferredSort(this);

        if (sort != null && !sort.equals(mSort)) {
            mSort = sort;
            MainActivityFragment ff = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_poster);

            if (null != ff) {
                ff.updateMovie();
            }
        }
    }

    // SharedPreference code adapted from http://stackoverflow.com/questions/22984696/storing-array-list-object-in-sharedpreferences

    public class ClearFavTask extends AsyncTask<Void, Void, Void> {
        private final String LOG_TAG = ClearFavTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = sharedPref.edit();

            if (sharedPref.contains(FAV_KEY)) {
                editor.remove(FAV_KEY);
            }

            editor.apply();

            return null;
        }
    }
}
