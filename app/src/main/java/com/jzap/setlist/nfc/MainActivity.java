package com.jzap.setlist.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "JAZ_NFC";

    private GoogleSignInClient mGoogleSignInClient;

    private Button mLogOutButton;
    private TextView mAccountNameTextView;
    private RecyclerView mUsersRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpDatabase();
        setUpUserAccount();
        setUpGoogleSignOut();
        setUpLogOut();
        //debugPrintToken();
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    private void setUpGoogleSignOut() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void startSignInActivity() {
        Intent signInIntent = new Intent(this, SignInActivity.class);
        startActivity(signInIntent);
    }

    private void setUpUserAccount() {
       mAccountNameTextView = (TextView) findViewById(R.id.allUsersTextView); // TODO: hack
       mAccountNameTextView.setText(ActiveUser.getUserKey(this));
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
