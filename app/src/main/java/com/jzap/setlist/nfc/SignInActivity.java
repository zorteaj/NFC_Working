package com.jzap.setlist.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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
 * Created by JZ_W541 on 4/3/2018.
 */

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "JAZ_NFC";

    private static int RC_SIGN_IN = 100;

    private GoogleSignInClient mGoogleSignInClient;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");

    private SignInButton mGoogleSignInButton;
    private Button mSignInButton;
    private Button mSignUpButton;
    private EditText mSignInEmailEditText;
    private EditText mSignInPasswordEditText;

    private HashMap<String, User> mUsers;

    // TODO: Validate
    private class SignIn {
        public String email;
        public String password;

        public SignIn(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if(isSignedIn()) {
            Log.i(TAG, "Already signed in");
            startMainActivity();
        } else {
            Log.i(TAG, "NOT yet signed in");
            setUpDatabase();
            setUpGoogleSignIn();
            setUpManualSignIn();
            setUpManualSignUp();
        }
    }

    // TODO: This is duplicated
    private void setUpDatabase() {
        mUsers = new HashMap<>();
        final HashSet<String> contactsSet = new HashSet<>();
        mUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    DataSnapshot contactsDB = snap.child("contacts");
                    for (DataSnapshot contact : contactsDB.getChildren()) {
                        contactsSet.add(contact.getValue(String.class));
                    }
                    try {
                        User user = new User(snap.child("email").getValue(String.class),
                                snap.child("firstName").getValue(String.class),
                                snap.child("lastName").getValue(String.class),
                                snap.child("website").getValue(String.class),
                                snap.child("password").getValue(String.class),
                                snap.child("phone").getValue(String.class),
                                contactsSet);

                        if (user != null) {
                            mUsers.put(user.getEmail(), user);
                        }
                    } catch (DatabaseException e) {
                        Log.e(TAG, "Couldn't make a user out of THIS db value");
                        Log.e(TAG, e.toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
    }

    private void setUpManualSignIn() {
        mSignInButton = (Button) findViewById(R.id.signInButton);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Get, validate, and store email and password
                mSignInEmailEditText = (EditText) findViewById(R.id.signInEmail) ;
                mSignInPasswordEditText = (EditText) findViewById(R.id.signInPassword);
                SignIn signIn = new SignIn(mSignInEmailEditText.getText().toString(), mSignInPasswordEditText.getText().toString());
                if(validateSignIn(signIn)) {
                    logIn(mSignInEmailEditText.getText().toString());
                } else {
                    // TODO: Give invalid sign in feedback
                }
            }
        });
    }

    private void setUpManualSignUp() {
        mSignUpButton = (Button) findViewById(R.id.signUpButton);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignUpActivity();
            }
        });
    }

    private void startSignUpActivity() {
        Intent signUpActivityIntent = new Intent(this, SignUpActivity.class);
        startActivity(signUpActivityIntent);
    }

    private boolean validateSignIn(SignIn signIn) {
        if(mUsers == null) { // TODO: We need to wait for the database callback to occur - this is a design issue
            Log.i(TAG, "Users is null");
            return false;
        }
        if(mUsers.containsKey(signIn.email) && (mUsers.get(signIn.email).getPassword()).equals(signIn.password)) {
            Log.i(TAG, "Good!");
            return true;
        }
        Log.i(TAG, "Failed here!");
        return false;
    }

    private boolean isSignedIn() {
       /* mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(mGoogleSignInAccount != null) {
            return true;
        }
        return false;*/
       if(SaveSharedPreference.getUserName(this).length() == 0) {
           return false;
       }
       return true;
    }

    private void setUpGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInButton = (SignInButton) findViewById(R.id.googleSignInButton);
        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });
    }

    private void googleSignIn()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            HashSet<String> contacts = new HashSet<>(); // TODO: Fill in any contacts?
            // New user
            if(!mUsers.containsKey(account.getEmail())) {
                User user = new User(account.getEmail(),
                        account.getGivenName(),
                        account.getFamilyName(),
                        "defaultWebsite",
                        "no pw",
                        "no phone",
                        contacts
                );
                user.postToDB();
            }
            logIn(account.getEmail());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void logIn(String userName) {
        Log.i(TAG, "Logging in");
        SaveSharedPreference.setUserName(this, userName);
        startMainActivity();
    }

}
