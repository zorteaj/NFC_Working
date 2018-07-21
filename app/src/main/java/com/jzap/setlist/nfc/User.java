package com.jzap.setlist.nfc;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by JZ_W541 on 3/29/2018.
 */

public class User {

    private static final String TAG = "JZ_NFC";

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");

    private String email, firstName, lastName, userName, website, password, phone, token, photoURL, cleanEmail;
    private boolean privateAccount = false;
    //private HashSet<String> contacts = new HashSet<>();
    private Map<String, Integer> contacts = new HashMap<>();
    private Map<String, Account> accounts = new HashMap<>();

    public User(String email, String firstName, String lastName, String userName, String website, String password, String phone, String token, String photoURL, boolean privateAccount, Map<String, Integer> contacts) {
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
        this.privateAccount = privateAccount;
    }

    private User(String email, String firstName, String lastName, String userName, String website, String password, String phone, String token, String photoURL, boolean privateAccount, DataSnapshot userDBSnapshot) {
        DataSnapshot contactsDB = userDBSnapshot.child("contacts");
        for (DataSnapshot contact : contactsDB.getChildren()) {
            contacts.put(contact.getKey(), contact.getValue(Integer.class));
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
        this.privateAccount = privateAccount;
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
                userDBSnapshot.child("privateAccount").getValue(Boolean.class),
                userDBSnapshot );
        // TODO: Optimize?
        Map<String, Account> accounts = new HashMap<>();
        DataSnapshot snapshotAccounts = userDBSnapshot.child("accounts");
        if(snapshotAccounts != null) {
            for(DataSnapshot snapshotAccount : snapshotAccounts.getChildren()) {
                Account account = new Account(snapshotAccount.getKey(), snapshotAccount.child("data").getValue(String.class), snapshotAccount.child("private").getValue(Boolean.class));
                accounts.put(snapshotAccount.getKey(), account);
            }
        }
        this.accounts = accounts;
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
        user.child("privateAccount").setValue(privateAccount);

        Iterator it = accounts.entrySet().iterator();
        while(it.hasNext()) {
            Account account = (Account) ((Map.Entry) it.next()).getValue();
            user.child("accounts").child(account.name).child("private").setValue(account.isPrivate);
            user.child("accounts").child(account.name).child("data").setValue(account.data);
        }

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

    public Map<String, Integer> getContacts() { return contacts; }

    public String getToken() { return token; }

    public String getPhotoURL() { return photoURL; }

    public String getCleanEmail() { return cleanEmail; }

    public boolean getPrivateAccount() { return privateAccount; }

    public Map<String, Account> getAccounts() { return accounts; }

    public void setAccounts(Map<String, Account> accounts) { this.accounts = accounts; }

    public void setPrivateAccount(boolean privateAccount) {
        this.privateAccount = privateAccount;
    }

 /*   public void addContact(String email) {
        contacts.add(email);
    }*/

}
