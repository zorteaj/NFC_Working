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

    DatabaseReference mThisUserRef;

    private HashMap<String, User> mUsers;

    private User mActiveUser;
    private User mThisContact;
    private String mThisContactKey;

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
            mThisContactKey = User.cleanEmail(intent.getStringExtra("REQUESTOR"));
        } else if (intent.getAction() == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            processTag(intent);
        } else {
            mThisContactKey = intent.getStringExtra("CONTACT_KEY");
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
        mUsersRef.child(mThisContactKey).child("contactRequests").child(mActiveUser.getCleanEmail()).setValue(mActiveUser.getCleanEmail());

        DatabaseReference requestRef = mRequestsRef.push();
        requestRef.child("token").setValue(mThisContact.getToken());
        // TODO: This should probably use the key like everything else, if for nothing but consistency
        requestRef.child("requestor").setValue(mActiveUser.getEmail());
        requestRef.child("outstanding").setValue("true");
    }

    private void setUpAcceptButton() {
        mAcceptRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add to contacts list
                mUsersRef.child(mThisContactKey).child("contacts").child(mActiveUser.getCleanEmail()).setValue(mActiveUser.getCleanEmail());
                // Delete the contact request
                mUsersRef.child(mActiveUser.getCleanEmail()).child("contactRequests").child(mThisContactKey).removeValue();
            }
        });
    }

    // TODO: The user might not be ready, if the DB did not reply yet
    private void display() {
        mContactName = (TextView) findViewById(R.id.userNameTextView);
        mContactName.setText(mThisContact.getFirstName() + " " + mThisContact.getLastName());

        if(mActiveUser.getContacts().contains(mThisContact.getCleanEmail()) || mActiveUser.getCleanEmail().equals(mThisContactKey)) {
            displayContact();
        } else {
            displayStranger();
        }

        setUpContactRequest();

    }

    // TODO: Should this be part of the DB Adptr?
    private void setUpContactRequest() {
        mUsersRef.child(mActiveUser.getCleanEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot contactRequests = dataSnapshot.child("contactRequests");
                for(DataSnapshot contact : contactRequests.getChildren()) {
                    // If this contact is in this user's contact requests
                    if(contact.getKey().equals(mThisContact.getCleanEmail())) {
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
    private void setUpThisUser(HashMap<String, User> users) {
        mActiveUser = ActiveUser.getActiveUser(this, users);
    }

    private void setUpThisContact(HashMap<String, User> users) {
        mThisContact = users.get(mThisContactKey);
    }

    private void refresh(HashMap<String, User> users) {
        mUsers = users;
        setUpThisUser(users);
        setUpThisContact(users);
        display();
        mDBReady = true;
        if (mRequestOutstanding) {
            makeRequest();
        }
    }

    private void setUpDatabase() {
        HashMap<String, User> users = FirebaseDBAdptr.getUsers();
        if(users.size() != 0) {
            refresh(users);
        }
        FirebaseDBAdptr.register(new FirebaseDBUsersCallback() {
            @Override
            public void call(int what, Object obj) {
                super.call(what, obj);
                refresh(users);
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
                    mThisContactKey = User.cleanEmail(payload);
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
