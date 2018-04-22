package com.jzap.setlist.nfc;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
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

    private String email, firstName, lastName, userName, website, password, phone, token, photoURL, cleanEmail;
    private HashSet<String> contacts = new HashSet<>();

    public User(String email, String firstName, String lastName, String userName, String website, String password, String phone, String token, String photoURL, HashSet<String> contacts) {
        this.email = email;
        this.website = website;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.phone = phone;
        this.token = token;
        this.photoURL = photoURL;
        this.contacts = contacts;
        this.cleanEmail = cleanEmail(email);
    }

    private User(String email, String firstName, String lastName, String userName, String website, String password, String phone, String token, String photoURL, DataSnapshot userDBSnapshot) {
        DataSnapshot contactsDB = userDBSnapshot.child("contacts");
        for (DataSnapshot contact : contactsDB.getChildren()) {
            contacts.add(contact.getValue(String.class));
        }
        this.email = email;
        this.website = website;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.phone = phone;
        this.token = token;
        this.photoURL = photoURL;
        this.cleanEmail = cleanEmail(email);
    }

    public User(DataSnapshot userDBSnapshot) {
        this( userDBSnapshot.child("email").getValue(String.class),
                userDBSnapshot.child("firstName").getValue(String.class),
                userDBSnapshot.child("lastName").getValue(String.class),
                userDBSnapshot.child("userName").getValue(String.class),
                userDBSnapshot.child("website").getValue(String.class),
                userDBSnapshot.child("password").getValue(String.class),
                userDBSnapshot.child("phone").getValue(String.class),
                userDBSnapshot.child("token").getValue(String.class),
                userDBSnapshot.child("photoURL").getValue(String.class),
                userDBSnapshot );
    }


    public void postToDB() {
        DatabaseReference user = mUsersRef.child(cleanEmail);
        user.child("email").setValue(email);
        user.child("website").setValue(website);
        user.child("firstName").setValue(firstName);
        user.child("lastName").setValue(lastName);
        user.child("userName").setValue(userName);
        user.child("password").setValue(password);
        user.child("phone").setValue(phone);
        user.child("token").setValue(token);
        user.child("photoURL").setValue(photoURL);
        // TODO: Do I ever need to push the contacts list?
    }

    public static String cleanEmail(String email) {
        return email.replace(".", ",");
    }

    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }

    public String getFirstName() {return firstName; }

    public String getLastName() { return lastName; }

    public String getUserName() { return userName; }

    public String getPassword() { return password; }

    public String getPhone() { return phone; }

    public HashSet<String> getContacts() { return contacts; }

    public String getToken() { return token; }

    public String getPhotoURL() { return photoURL; }

    public String getCleanEmail() { return cleanEmail; }

    public void addContact(String email) {
        contacts.add(email);
    }

}
