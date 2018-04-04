package com.jzap.setlist.nfc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class SaveSharedPreference {
    static final String PREF_USER_NAME = "username";

    static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setUserName(Context context, String userName) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.commit();
    }

    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(PREF_USER_NAME, "");
    }

    public static void clearUserName(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }
}
