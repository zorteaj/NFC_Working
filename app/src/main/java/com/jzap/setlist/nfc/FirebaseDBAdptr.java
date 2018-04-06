package com.jzap.setlist.nfc;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by JZ_W541 on 4/5/2018.
 */

public class FirebaseDBAdptr {

    private static final String TAG = Config.TAG_HEADER + "FbDBAdptr";

    private static DatabaseReference rootDBRef = FirebaseDatabase.getInstance().getReference();
    private static DatabaseReference usersDBRef = rootDBRef.child("users");

    private static HashMap<String, User> users = new HashMap<>();
    private static boolean init = false;

    private static List<FirebaseDBUsersCallback> usersCallbacks = new ArrayList<>();

    public static void register(FirebaseDBUsersCallback callback) {
        if(!init) {
            init();
            init = true;
        }
        usersCallbacks.add(callback);
    }

    public static HashMap<String, User> getUsers() {
        return users;
    }

    private static void init() {
        // TODO: Consider using child event listener
        usersDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // TODO: I'm clearing the entire set of users and replacing them.  There's probably a better way.
                users = new HashMap<>();
                int count = 0;
                for(DataSnapshot userDBSnapShot : dataSnapshot.getChildren()) {
                    count++;
                    try {
                        User user = new User(userDBSnapShot);
                        if (user != null) {
                            users.put(user.getCleanEmail(), user);
                        } else {
                            Log.w(TAG, "Got null after trying to construct new user");
                        }
                    } catch (DatabaseException e) {
                        Log.e(TAG, "Couldn't make a user out of this db value");
                        Log.e(TAG, e.toString());
                    }
                }
                for(Callback callback : usersCallbacks) {
                    callback.call(Callback.USERS_UPDATED, users);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
