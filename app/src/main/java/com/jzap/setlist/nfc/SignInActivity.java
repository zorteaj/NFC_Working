package com.jzap.setlist.nfc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "JAZ_NFC";

    private static int RC_SIGN_IN = 100;

    private GoogleSignInClient mGoogleSignInClient;

   // private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
  //  private DatabaseReference mUsersRef = mRootRef.child("users");

    private SignInButton mGoogleSignInButton;
    private Button mSignInButton;
    private Button mSignUpButton;
    private EditText mSignInEmailEditText;
    private EditText mSignInPasswordEditText;

    private HashMap<String, User> mUsers;

    // TODO: Validate
    // TODO: What the heck is this? Get rid of it, probably
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

        Log.i(TAG, "Starting App...");

        //test();

        if(isSignedIn()) {
            Log.i(TAG, "Already signed in");
            writeTokenToDB(ActiveUser.getUserKey(this), ActiveUser.getToken(this));
            startMainActivity();
        } else {
            Log.i(TAG, "NOT yet signed in");
            setUpDatabase();
            setUpManualSignUp();
        }
    }

    private void test() {
        int RC_SIGN_IN = 123;


        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
               /* new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(), */
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
               // new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
               /* new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build() */);

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                int x = 0;
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
            }
        }
    }*/

    private void setUpDatabase() {
        // TODO: It's a bit clunky to wait for the db to be ready to do much of anything else, but it's the safe quicky way for now
        FirebaseDBAdptr.register(new FirebaseDBUsersCallback() {
            @Override
            public void call(int what, Object obj) {
                super.call(what, obj);
                mUsers = users;
                setUpManualSignIn();
                setUpGoogleSignIn();
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
                Log.i(TAG, "Button clicked");
                mSignInEmailEditText = (EditText) findViewById(R.id.signInEmail) ;
                mSignInPasswordEditText = (EditText) findViewById(R.id.signInPassword);
                SignIn signIn = new SignIn(mSignInEmailEditText.getText().toString(), mSignInPasswordEditText.getText().toString());
                if(validateSignIn(signIn)) {
                    logIn(User.cleanEmail(mSignInEmailEditText.getText().toString()));
                } else {
                    Log.i(TAG, "Invalid");
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
        if(mUsers.containsKey(User.cleanEmail(signIn.email)) && (mUsers.get(User.cleanEmail(signIn.email)).getPassword()).equals(signIn.password)) {
            Log.i(TAG, "Good!");
            return true;
        }
        Log.i(TAG, "Failed here!");
        return false;
    }

    private boolean isSignedIn() {
       if(ActiveUser.getUserKey(this).length() == 0) {
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
            String token = ActiveUser.getToken(this);

            if(!mUsers.containsKey(account.getEmail())) {
                User user = new User(account.getEmail(),
                        account.getGivenName(),
                        account.getFamilyName(),
                        "defaultWebsite",
                        "no pw",
                        "no phone",
                        token,
                        account.getPhotoUrl().toString(),
                        contacts
                );
                user.postToDB();
            }
            logIn(User.cleanEmail(account.getEmail()));
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void logIn(String userName) {
        Log.i(TAG, "Logging in");
        ActiveUser.setUserKey(this, userName);
        writeTokenToDB(userName, ActiveUser.getToken(this));
        startMainActivity();
    }

    // TODO: This is duplicated in FirebaseIDService
    private void writeTokenToDB(final String userName, final String token) {
        Log.i(TAG, "Writing token to DB....");
        Log.i(TAG, "User = " + userName);
        Log.i(TAG, "Token = " + token);
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("users");
        usersRef.child(userName).child("token").setValue(token);
    }

}
