package com.jzap.setlist.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;*/
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "JAZ_NFC";

    private Button mSettingsButton;
    private TextView mCurrentUserNameTextView;
    private RecyclerView mUsersRecyclerView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpDatabase();
        setUpUserAccount();
        setUpSettings();
        //debugPrintToken();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setUpSettings() {
        final Context context = this;
        final Activity activity = this;
        mSettingsButton = (Button) findViewById(R.id.settingsButton);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSettingsActivity();
                //testAccountLink();
            }
        });
    }

    private void startSettingsActivity() {
        Intent signInIntent = new Intent(this, SettingsActivity.class);
        startActivity(signInIntent);
    }

    private void setUpUserAccount() {
       mCurrentUserNameTextView = (TextView) findViewById(R.id.currentUserNameTextView); // TODO: hack
       mCurrentUserNameTextView.setText(ActiveUser.getUserKey(this));
   }

    private void displayUsers(HashMap<String, User> users) {
        mUsersRecyclerView = (RecyclerView) findViewById(R.id.usersRecyclerView);
        UsersRecyclerViewAdptr adptr = new UsersRecyclerViewAdptr(users, this);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUsersRecyclerView.setAdapter(adptr);
        //adptr.notifyItemChanged(0);
    }

    private void debugPrintToken() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "Token = " + refreshedToken);
    }

    private void setUpDatabase() {
        // This is a bit sketchy, but I'm displaying the current users, and subsequently registering for a change to users, at which point, I'll display them
        HashMap<String, User> users = (FirebaseDBAdptr.getUsers());
        if(users.size() != 0) {
            displayUsers(users);
        }
        FirebaseDBAdptr.register(new FirebaseDBUsersCallback() {
            @Override
            public void call(int what, Object obj) {
                super.call(what, obj);
                displayUsers(users);
            }
        });
    }
}
