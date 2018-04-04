package com.jzap.setlist.nfc;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by JZ_W541 on 3/29/2018.
 */

public class User {

    private static final String TAG = "JZ_NFC";

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");

    private String email, firstName, lastName, website, password, phone;

    public User(String email, String firstName, String lastName, String website, String password, String phone) {
        this.email = email;
        this.website = website;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.phone = phone;
    }

    public User() {}

    public void postToDB() {
        mUsersRef.push().setValue(this);
    }


    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }

    public String getFirstName() {return firstName; }

    public String getLastName() { return lastName; }

    public String getPassword() { return password; }

    public String getPhone() { return phone; }

}
