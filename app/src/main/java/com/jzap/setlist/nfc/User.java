package com.jzap.setlist.nfc;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by JZ_W541 on 3/29/2018.
 */

public class User {

    private static final String TAG = "JZ_NFC";

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");

    private String email, firstName, lastName, website, password, phone;
    private HashSet<String> contacts;

    public User(String email, String firstName, String lastName, String website, String password, String phone, HashSet<String> contacts) {
        this.email = email;
        this.website = website;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.phone = phone;
        this.contacts = contacts;
    }

    public User() {
        this.contacts = new HashSet<>();
    }

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

    public HashSet<String> getContacts() { return contacts; }

    public void addContact(String email) {
        contacts.add(email);
    }

}
