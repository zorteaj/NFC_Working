package com.jzap.setlist.nfc;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class ContactActivity extends AppCompatActivity {

    boolean mDBReady = false;
    boolean mRequestOutstanding = false;

    private static final String TAG = "JAZ_NFC";

    private TextView mContactUserName;
    private TextView mContactFirstName;
    private TextView mContactLastName;
    private ImageView mPhotoImageView;
    private Button mRequestButton;
    private Button mAcceptRequestButton;
    private Button mRemoveContactButton;
    private TableLayout mTable;
    private TextView mContactEmail;
    private TextView mContactWebsite;

    private NfcAdapter mNfcAdapter;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private DatabaseReference mRequestsRef = mRootRef.child("requests");

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
        setUpRemoveButton();

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
        mContactFirstName = (TextView) findViewById(R.id.userFirstName);
        mContactLastName = (TextView) findViewById(R.id.userLastName);
        mContactEmail = (TextView) findViewById(R.id.userEmailTextView);
        mContactWebsite = (TextView) findViewById(R.id.userWebsiteTextView);
        mRemoveContactButton = (Button) findViewById(R.id.removeContatButton);
        mRemoveContactButton.setVisibility(View.VISIBLE);
        mPhotoImageView = (ImageView) findViewById(R.id.photoImageView);
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
        // Create the request, used for notification
        DatabaseReference requestRef = mRequestsRef.push();
        requestRef.child("token").setValue(mThisContact.getToken());
        // TODO: This should probably use the key like everything else, if for nothing but consistency
        requestRef.child("requestor").setValue(mActiveUser.getEmail());
        requestRef.child("outstanding").setValue("true");

        // Put the contact request in the user's contact request list
        DatabaseReference contactRequests = mUsersRef.child(mThisContactKey).child("contactRequests");
        DatabaseReference contactRequest = contactRequests.child(mActiveUser.getCleanEmail());
        contactRequest.child("requestKey").setValue(requestRef.getKey());
    }

    private void setUpAcceptButton() {
        mAcceptRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add to contacts list
                mUsersRef.child(mThisContactKey).child("contacts").child(mActiveUser.getCleanEmail()).setValue(mActiveUser.getCleanEmail());
                // Delete the contact request
                //mUsersRef.child(mActiveUser.getCleanEmail()).child("contactRequests").child(mThisContactKey).removeValue();

                DatabaseReference contactRequest = mUsersRef.child(mActiveUser.getCleanEmail()).child("contactRequests").child(mThisContactKey);
                // Delete the request (for notification)
                Log.i(TAG, "attempting to delete this request: " + contactRequest.child("requestKey").getKey());
                //mRequestsRef.child(contactRequest.child("requestKey").getKey()).removeValue();
                contactRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.child("requestKey").getRef().removeValue();
                        mRequestsRef.child(dataSnapshot.child("requestKey").getValue(String.class)).removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // Delete the contact request
                contactRequest.removeValue();
            }
        });
    }

    private void setUpRemoveButton() {
        mRemoveContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsersRef.child(mActiveUser.getCleanEmail()).child("contacts").child(mThisContactKey).removeValue();
                mUsersRef.child(mThisContactKey).child("contacts").child(mActiveUser.getCleanEmail()).removeValue();
            }
        });
    }

    // TODO: The user might not be ready, if the DB did not reply yet
    private void display() {

        if(mThisContact == null) {
            Log.e(TAG, "Trying to display a null contact");
            Log.e(TAG, "Contact id = " + mThisContactKey);
            return;
        }

        mContactUserName = (TextView) findViewById(R.id.userNameTextView);
        mContactUserName.setText(mThisContact.getUserName());

        if(mActiveUser.getContacts().contains(mThisContact.getCleanEmail())) {
            displayContact(false);
        } else if(mActiveUser.getCleanEmail().equals(mThisContactKey)) {
            displayContact(true);
        } else {
            displayStranger();
        }

        setUpContactRequest();

    }

    // TODO: Should this be part of the DB Adptr?
    private void setUpContactRequest() {
        mUsersRef.child(mActiveUser.getCleanEmail()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If this contact is in this user's contact requests
                boolean isContactRequest = dataSnapshot.child("contactRequests").child(mThisContact.getCleanEmail()).exists();
                if(isContactRequest) {
                    mAcceptRequestButton.setVisibility(View.VISIBLE);
                } else {
                    mAcceptRequestButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void displayPhoto() {
        // TODO: Can't start a load for a destroyed activity crash?
        Glide.with(getApplicationContext()).load(mThisContact.getPhotoURL()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                Log.e(TAG,"Glide failed");
                Log.e(TAG, e.toString());
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(mPhotoImageView);
    }

    private void displayContact(boolean self) {
        displayPhoto();

        mRequestButton.setVisibility(View.INVISIBLE);
        mTable.setVisibility(View.VISIBLE);

        if(self) {
            mRemoveContactButton.setVisibility(View.INVISIBLE);
        } else {
            mRemoveContactButton.setVisibility(View.VISIBLE);
        }

        mContactUserName.setTextColor(Color.BLUE);

        mContactFirstName.setText(mThisContact.getFirstName());
        mContactLastName.setText(mThisContact.getLastName());
        mContactEmail.setText(mThisContact.getEmail());
        mContactWebsite.setText(mThisContact.getWebsite());
    }

    private void displayStranger() {
        mTable.setVisibility(View.INVISIBLE);
        mRequestButton.setVisibility(View.VISIBLE);
        mRemoveContactButton.setVisibility(View.INVISIBLE);
    }

    // TODO: This is duplicated
    private void setUpThisUser(HashMap<String, User> users) {
        mActiveUser = ActiveUser.getActiveUser(this, users);
    }

    private void setUpThisContact(HashMap<String, User> users) {
        Log.i(TAG, "Setting up contact");
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
            mRequestOutstanding = false;
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
            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
            for(int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
                NdefRecord[] records = msgs[i].getRecords();
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
