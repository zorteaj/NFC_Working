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

    private String email, firstName, lastName, website, password, phone, token;
    private HashSet<String> contacts;

    public User(String email, String firstName, String lastName, String website, String password, String phone, String token, HashSet<String> contacts) {
        this.email = email;
        this.website = website;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.phone = phone;
        this.token = token;
        this.contacts = contacts;
    }

    public User() {
        this.contacts = new HashSet<>();
    }

    public void postToDB() {
        DatabaseReference user = mUsersRef.push();
        user.child("email").setValue(email);
        user.child("website").setValue(website);
        user.child("firstName").setValue(firstName);
        user.child("lastName").setValue(lastName);
        user.child("password").setValue(password);
        user.child("phone").setValue(phone);
        user.child("token").setValue(token);
        DatabaseReference contacts = user.child("contacts");
        contacts.child("exampleContact").setValue("mark@mark.com");
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

    public String getToken() { return token; }

    public void addContact(String email) {
        contacts.add(email);
    }

}
