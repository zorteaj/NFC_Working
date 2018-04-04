package com.jzap.setlist.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by JZ_W541 on 4/3/2018.
 */



public class SignUpActivity extends AppCompatActivity {

    private Button mSignUpButton;
    private EditText mFirstNameEditText;
    private EditText mLastNameEdtiText;
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
        mLastNameEdtiText = (EditText) findViewById(R.id.firstNameEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);

        mSignUpButton = (Button) findViewById(R.id.signUpButton);

        final String defaultWebsite = "defaultWebsite";

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Validate
                User user = new User(mEmailEditText.getText().toString(),
                        defaultWebsite,
                        mFirstNameEditText.getText().toString(),
                        mLastNameEdtiText.getText().toString(),
                        mPasswordEditText.getText().toString(),
                        mPhoneEditText.getText().toString()
                );
                user.postToDB();
                logIn(mEmailEditText.getText().toString());
            }
        });
    }

    // TODO: This is duplicated from SignInActivity
    private void logIn(String userName) {
        SaveSharedPreference.setUserName(this, userName);
        startMainActivity();
    }

    // TODO: This is duplicated from SignInActivity
    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
    }

}
