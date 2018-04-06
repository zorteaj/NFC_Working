package com.jzap.setlist.nfc;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by JZ_W541 on 3/29/2018.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "JAZ_NFC";

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // If a user is already signed in, update their token
        String userName = SaveSharedPreference.getUserName(this);
        if(userName.length() != 0) {
            writeTokenToDB(userName, token);
        }
        SaveSharedPreference.setToken(this, token);
    }

    private void writeTokenToDB(final String userName, final String token) {

       mUsersRef.child(userName).child(token).setValue(token);


       /* final HashSet<String> contactsSet = new HashSet<>();



        mUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    if(snap.child("email").getValue().equals(userName)) {
                        snap.getRef().child("token").setValue(token);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }
}