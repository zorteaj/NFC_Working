package com.jzap.setlist.nfc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class ContactActivity extends AppCompatActivity {

    boolean mDBReady = false;
    boolean mRequestOutstanding = false;

    private static final String TAG = "JAZ_NFC";

    private TextView mContactName;
    private Button mRequestButton;
    private Button mAcceptRequestButton;
    private TableLayout mTable;
    private TextView mContactEmail;
    private TextView mContactWebsite;

    private NfcAdapter mNfcAdapter;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private DatabaseReference mRequestsRef = mRootRef.child("requests");

    DatabaseReference mThisContactRef;
    DatabaseReference mThisUserRef;

    private HashMap<String, User> mUsers;

    private User mThisUser;
    private User mThisContact;
    private String mThisContactEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        setUpNFC();
        setUpDisplay();
        setUpRequestButton();
        setUpAcceptButton();

        Intent intent = getIntent();

        if(intent.getAction() == "CONTACT_REQUEST") {
            mThisContactEmail = intent.getStringExtra("REQUESTOR");
        } else if (intent.getAction() == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            processTag(intent);
        } else {
            mThisContactEmail = intent.getStringExtra("CONTACT_EMAIL");
        }

        setUpDatabase();
    }

    private void setUpDisplay() {
        mTable = (TableLayout) findViewById(R.id.contactDetailsTable);
        mTable.setVisibility(View.INVISIBLE);
        mRequestButton = (Button) findViewById(R.id.requestContactButton);
        mRequestButton.setVisibility(View.INVISIBLE);
        mAcceptRequestButton = (Button) findViewById(R.id.acceptRequestButton);
        mAcceptRequestButton.setVisibility(View.INVISIBLE);
        mContactEmail = (TextView) findViewById(R.id.userEmailTextView);
        mContactWebsite = (TextView) findViewById(R.id.userWebsiteTextView);
    }

    private void setUpRequestButton() {
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeRequest();
            }
        });
    }

    private void makeRequest() {
        DatabaseReference contactRequests = mThisContactRef.child("contactRequests");
        DatabaseReference contactRequest = contactRequests.push();
        contactRequest.setValue(mThisUser.getEmail());

        DatabaseReference requestRef = mRequestsRef.push();
        requestRef.child("token").setValue(mThisContact.getToken());
        requestRef.child("requestor").setValue(mThisUser.getEmail());
        requestRef.child("outstanding").setValue("true");
    }

    private void setUpAcceptButton() {
        mAcceptRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add to contacts list
                DatabaseReference contacts = mThisContactRef.child("contacts");
                DatabaseReference contact = contacts.push();
                contact.setValue(mThisUser.getEmail());

                // Delete the contact request
               // DatabaseReference contactRequests = mThisUserRef.child("contactRequests");
            }
        });
    }

    // TODO: The user might not be ready, if the DB did not reply yet
    private void display() {
        mContactName = (TextView) findViewById(R.id.userNameTextView);
        mContactName.setText(mThisContact.getFirstName() + " " + mThisContact.getLastName());

        if(mThisUser.getContacts().contains(mThisContactEmail) || mThisUser.getEmail().equals(mThisContactEmail)) {
            displayContact();
        } else {
            displayStranger();
        }

        setUpContactRequest();

    }

    private void setUpContactRequest() {
        mThisUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot contactRequests = dataSnapshot.child("contactRequests");
                for(DataSnapshot contact : contactRequests.getChildren()) {
                    // If this contact is in this user's contact requests
                    if(contact.getValue().equals(mThisContact.getEmail())) {
                        mAcceptRequestButton.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void displayContact() {
        mRequestButton.setVisibility(View.INVISIBLE);
        mTable.setVisibility(View.VISIBLE);

        mContactName.setTextColor(Color.BLUE);

        mContactEmail.setText(mThisContact.getEmail());
        mContactWebsite.setText(mThisContact.getWebsite());
    }

    private void displayStranger() {
        mTable.setVisibility(View.INVISIBLE);
        mRequestButton.setVisibility(View.VISIBLE);
    }

    // TODO: This is duplicated
    private void setUpThisUser() {
        mThisUser = mUsers.get(SaveSharedPreference.getUserName(this));
        if(mThisUser == null) {
            Log.e(TAG, "Null user!"); // TODO: Throw exception?
        }
    }

    // TODO: This is duplicated
    private void setUpDatabase() {
        final Context context = this;
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
                                snap.child("token").getValue(String.class),
                                contactsSet);

                        if (user != null) {
                            mUsers.put(user.getEmail(), user);
                            if(user.getEmail().equals(mThisContactEmail)) {
                                mThisContact = user;
                                mThisContactRef = snap.getRef();
                            }
                            if(user.getEmail().equals(SaveSharedPreference.getUserName(context))) { // TODO: Things are getting a little messy here, especially with setUpThisUser(), as it relates to this
                                mThisUserRef = snap.getRef();
                            }
                        }
                    } catch (DatabaseException e) {
                        Log.e(TAG, "Couldn't make a user out of THIS db value");
                        Log.e(TAG, e.toString());
                    }
                }
                setUpThisUser();
                display();
                mDBReady = true;
                if (mRequestOutstanding) {
                    makeRequest();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(mNfcAdapter == null) {
            Toast.makeText(this, "No NFC support on this device", Toast.LENGTH_LONG).show();
            //finish();
            return;
        }

        if(mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC Adapter is enabled", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "NFC Adapter is NOT enabled", Toast.LENGTH_LONG).show();
        }
    }

    // TODO: Only send request if you're already not friends
    private void processTag(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(rawMsgs != null) {
            Log.i(TAG, "There are " + rawMsgs.length + " messages in this tag");
            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
            for(int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
                NdefRecord[] records = msgs[i].getRecords();
                Log.i(TAG, "There are " + records.length + " records in this message");
                for(int j = 0; j < records.length; j++) {
                    String payload = new String(records[j].getPayload());
                    Log.i(TAG, payload);
                    mThisContactEmail = payload;
                    if(mDBReady) {
                        makeRequest();
                    } else {
                        mRequestOutstanding = true;
                    }
                }
            }
        } else {
            Log.i(TAG, "Problem reading tag");
        }
    }

}
