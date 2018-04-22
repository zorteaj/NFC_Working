package com.jzap.setlist.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;

/**
 * Created by JZ_W541 on 4/22/2018.
 */

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = Config.TAG_HEADER + "Settings";

    private Button mLogOutButton;
    private GoogleSignInClient mGoogleSignInClient;

    private Button mSubmitChangesButton;
    private EditText mUserNameChangeEditText;
    private EditText mPhoneChangeEditText;
    private EditText mPasswordChangeEditText;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setUpChanges();
        setUpGoogleSignOut();
        setUpLogOut();
    }

    private void setUpLogOut() {
        final Context context = this;
        final Activity activity = this;
        mLogOutButton = (Button) findViewById(R.id.logOutButton);
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Reassign the token?
                ActiveUser.clearUserKey(context);
                startSignInActivity();

                mGoogleSignInClient.signOut().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i(TAG, "Signed out");
                    }
                });
            }
        });
    }

    private void setUpChanges() {
        mUserNameChangeEditText = (EditText) findViewById(R.id.userNameChangeEditText);
        mPhoneChangeEditText = (EditText) findViewById(R.id.phoneChangeEditText);
        mPasswordChangeEditText = (EditText) findViewById(R.id.passwordChangeEditText);

        mSubmitChangesButton = (Button) findViewById(R.id.submitChangesButton);

        final Context context = this;

        mSubmitChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference userRef = mUsersRef.child(ActiveUser.getUserKey(context));
                if(mUserNameChangeEditText.getText().toString().length() != 0)
                    userRef.child("userName").setValue(mUserNameChangeEditText.getText().toString());
                if(mPasswordChangeEditText.getText().toString().length() != 0)
                    userRef.child("phone").setValue(mPasswordChangeEditText.getText().toString());
                if(mPasswordChangeEditText.getText().toString().length() != 0)
                    userRef.child("password").setValue(mPasswordChangeEditText.getText().toString());
            }
        });
    }

    private void startSignInActivity() {
        Intent signInIntent = new Intent(this, SignInActivity.class);
        startActivity(signInIntent);
    }

    private void setUpGoogleSignOut() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

}
