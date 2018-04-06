package com.jzap.setlist.nfc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class ActiveUser {
    private static final String TAG = Config.TAG_HEADER + "ActiveUsr";

    static final String USER_KEY = "userkey";
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

    public static void setUserKey(Context context, String userKey) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(USER_KEY, userKey);
        editor.commit();
    }

    public static String getUserKey(Context context) {
        return getSharedPreferences(context).getString(USER_KEY, "");
    }

    // TODO: This will wipe my token right now as well
    public static void clearUserKey(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }

    public static User getActiveUser(Context context, HashMap<String, User> usersMap) {
        String activeUserKey = getUserKey(context);
        if(activeUserKey.length() == 0) {
            Log.e(TAG, "No active user");
            return null;
        }
        User user = usersMap.get(activeUserKey);
        if(user == null) {
            Log.e(TAG, "Couldn't find active user in database");
        }
        return user;
    }
}
