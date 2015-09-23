package com.fract.nano.williamyoung.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {
    private static boolean mTwoPane = false;
    public static String getPreferredSort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.pref_sort_popdesc));
    }

    public static String getAPIKey() { return ""; } // remove before committing to GitHub

    public static boolean getTwoPane() { return mTwoPane; }

    public static void setTwoPane(boolean twoPane) { mTwoPane = twoPane; }
}