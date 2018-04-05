package com.jzap.setlist.nfc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class SaveSharedPreference {
    static final String PREF_USER_NAME = "username";
    static final String TOKEN = "token";

    static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setToken(Context context, String token) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(TOKEN, token);
        editor.commit();
    }

    public static String getToken(Context context) {
        return getSharedPreferences(context).getString(TOKEN, "");
    }

    public static void clearToken(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }

    public static void setUserName(Context context, String userName) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.commit();
    }

    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(PREF_USER_NAME, "");
    }

    // TODO: This will wipe my token right now as well
    public static void clearUserName(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }
}
