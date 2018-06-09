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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
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
    private Switch mPrivateAccountSwitch;

    private User mActiveUser;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setUpDatabase();
        //setUpPrivateAccountSwitch();

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

    // TODO: This is done elsewhere - make it common?
    private void setUpDatabase() {
        HashMap<String, User> users = FirebaseDBAdptr.getUsers();
        if(users.size() != 0) {
            refresh(users);
        }
        FirebaseDBAdptr.register(new FirebaseDBUsersCallback() {
            @Override
            public void call(int what, Object obj) {
                super.call(what, obj);
                refresh(users);
            }
        });
    }

    private void refresh(HashMap<String, User> users) {
        Log.i(TAG, "Refresh");
        //mUsers = users;
        setUpThisUser(users);
        setUpPrivateAccountSwitch();
        //setUpThisContact(users);
        //display();
        //mDBReady = true;
        //if (mRequestOutstanding) {
            //makeRequest();
            //mRequestOutstanding = false;
        //}
    }

    // TODO: This is duplicated
    private void setUpThisUser(HashMap<String, User> users) {
        mActiveUser = ActiveUser.getActiveUser(this, users);
    }

    private void setUpPrivateAccountSwitch() {
        mPrivateAccountSwitch = (Switch) findViewById(R.id.privateSwitch);
        mPrivateAccountSwitch.setChecked(mActiveUser.getPrivateAccount());

        mPrivateAccountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mPrivateAccountSwitch.setChecked(b);
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
                userRef.child("privateAccount").setValue(mPrivateAccountSwitch.isChecked());
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
