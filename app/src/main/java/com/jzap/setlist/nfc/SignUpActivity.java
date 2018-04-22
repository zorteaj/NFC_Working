package com.jzap.setlist.nfc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;

/**
 * Created by JZ_W541 on 4/3/2018.
 */



public class SignUpActivity extends AppCompatActivity {

    private final static String TAG = "JAZ_NFC";

    private Button mSignUpButton;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mUserNameEditText;
    private EditText mPhoneEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setUpSignUp();
    }

    private void setUpSignUp() {
        mFirstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
        mLastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
        mUserNameEditText = (EditText) findViewById(R.id.userNameEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);

        mSignUpButton = (Button) findViewById(R.id.submitChangesButton);

        final String defaultWebsite = "defaultWebsite";
        final String defaultPhotoURL = "http://mehandis.net/wp-content/uploads/2017/12/default-user.png";
        final Context context = this;

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Validate
                HashSet<String> contacts = new HashSet<>(); // TODO: Put contacts in here?
                String token = ActiveUser.getToken(context);
                User user = new User(mEmailEditText.getText().toString(),
                        mFirstNameEditText.getText().toString(),
                        mLastNameEditText.getText().toString(),
                        mUserNameEditText.getText().toString(),
                        defaultWebsite,
                        mPasswordEditText.getText().toString(),
                        mPhoneEditText.getText().toString(),
                        token,
                        defaultPhotoURL,
                        contacts
                );
                user.postToDB();
                logIn(User.cleanEmail(mEmailEditText.getText().toString()));
            }
        });
    }

    // TODO: This is duplicated from SignInActivity
    private void logIn(String userName) {
        ActiveUser.setUserKey(this, userName);
        writeTokenToDB(userName, ActiveUser.getToken(this));
        startMainActivity();
    }

    // TODO: This is duplicated in FirebaseIDService and SignInActivity
    private void writeTokenToDB(final String userName, final String token) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("users");
        usersRef.child(userName).child("token").setValue(token);
    }

    // TODO: This is duplicated from SignInActivity
    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
    }

}
