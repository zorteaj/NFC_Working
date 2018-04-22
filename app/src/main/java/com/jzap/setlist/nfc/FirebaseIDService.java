package com.jzap.setlist.nfc;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

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
        Log.i(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        // If a user is already signed in, update their token
        String userName = ActiveUser.getUserKey(this);
        if(userName.length() != 0) {
            writeTokenToDB(userName, token);
        }
        ActiveUser.setToken(this, token);
    }

    private void writeTokenToDB(final String userName, final String token) {
       mUsersRef.child(userName).child("token").setValue(token);
    }
}