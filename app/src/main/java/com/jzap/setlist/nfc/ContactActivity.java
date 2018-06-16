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
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class ContactActivity extends AppCompatActivity {

    boolean mDBReady = false;
    boolean mRequestOutstanding = false;

    private static final String TAG = "JAZ_NFC";

    private TextView mContactUserName;

    private ImageView mPhotoImageView;
    private Button mRequestButton;
    private Button mAcceptRequestButton;
    private Button mRemoveContactButton;
    private TableLayout mTable;

    private EditText mContactEmail;
    private EditText mContactWebsite;
    private EditText mContactFirstName;
    private EditText mContactLastName;
    private EditText mContactFacebook;
    private EditText mContactInstagram;
    private EditText mContactTwitter;
    private EditText mContactLinkedin;
    private EditText mContactYoutube;
    private EditText mContactSnapchat;
    private EditText mContactPinterest;
    private EditText mContactVimeo;
    private EditText mContactFlickr;

    private Switch mEmailPrivateSwitch;
    private Switch mWebsitePrivateSwitch;
    private Switch mFirstNamePrivateSwitch;
    private Switch mLastNamePrivateSwitch;
    private Switch mFacebookPrivateSwitch;
    private Switch mInstagramPrivateSwitch;
    private Switch mTwitterPrivateSwitch;
    private Switch mLinkedinPrivateSwitch;
    private Switch mYoutubePrivateSwitch;
    private Switch mSnapchatPrivateSwitch;
    private Switch mPinterestPrivateSwitch;
    private Switch mVimeoPrivateSwitch;
    private Switch mFlickrPrivateSwitch;

    private Button mUpdateButton;

    private NfcAdapter mNfcAdapter;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private DatabaseReference mNotificationsRef = mRootRef.child("notifications");

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
        setUpUpdateButton();
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
        mRemoveContactButton = (Button) findViewById(R.id.removeContatButton);
        mRemoveContactButton.setVisibility(View.VISIBLE);
        mPhotoImageView = (ImageView) findViewById(R.id.photoImageView);
        mUpdateButton = findViewById(R.id.updateButton);

        mContactFirstName = findViewById(R.id.userFirstName);
        mContactLastName = findViewById(R.id.userLastName);
        mContactEmail = findViewById(R.id.userEmailTextView);
        mContactWebsite = findViewById(R.id.userWebsiteTextView);
        mContactFacebook = findViewById(R.id.facebookTextView);
        mContactInstagram = findViewById(R.id.instagramTextView);
        mContactTwitter = findViewById(R.id.twitterTextView);
        mContactLinkedin = findViewById(R.id.linkedinTextView);
        mContactYoutube = findViewById(R.id.youtubeTextView);
        mContactSnapchat = findViewById(R.id.snapchatTextView);
        mContactPinterest = findViewById(R.id.pintrestTextView);
        mContactVimeo = findViewById(R.id.vimeoTextView);
        mContactFlickr = findViewById(R.id.flickrTextView);

        mEmailPrivateSwitch = findViewById(R.id.privateEmail);
        mWebsitePrivateSwitch = findViewById(R.id.privateWebsite);
        mFirstNamePrivateSwitch = findViewById(R.id.privateFirstName);
        mLastNamePrivateSwitch = findViewById(R.id.privateLastName);
        mFacebookPrivateSwitch = findViewById(R.id.privateFacebook);
        mInstagramPrivateSwitch = findViewById(R.id.privateInstagram);
        mTwitterPrivateSwitch = findViewById(R.id.privateTwitter);
        mLinkedinPrivateSwitch = findViewById(R.id.privateLinkedin);
        mYoutubePrivateSwitch = findViewById(R.id.privateYoutube);
        mSnapchatPrivateSwitch = findViewById(R.id.privateSnapchat);
        mPinterestPrivateSwitch = findViewById(R.id.privatePintrest);
        mVimeoPrivateSwitch = findViewById(R.id.privateVimeo);
        mFlickrPrivateSwitch = findViewById(R.id.privateFlickr);
    }

    private void setUpUpdateButton() {
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mActiveUser.
                //mActiveUser.postToDB();
                // TODO: Optimize?
                Map<String, Account> accounts = new HashMap<>();

                Account facebookAccount = new Account("facebook", mContactFacebook.getText().toString(), mFacebookPrivateSwitch.isChecked());
                accounts.put("facebook", facebookAccount);

                Account instagramAccount = new Account("instagram", mContactInstagram.getText().toString(), mInstagramPrivateSwitch.isChecked());
                accounts.put("instagram", instagramAccount);

                Account twitterkAccount = new Account("twitter", mContactTwitter.getText().toString(), mTwitterPrivateSwitch.isChecked());
                accounts.put("twitter", twitterkAccount);

                Account youtubeAccount = new Account("youtube", mContactYoutube.getText().toString(), mYoutubePrivateSwitch.isChecked());
                accounts.put("youtube", youtubeAccount);

                Account linkedinkAccount = new Account("linkedin", mContactLinkedin.getText().toString(), mLinkedinPrivateSwitch.isChecked());
                accounts.put("linkedin", linkedinkAccount);

                Account snapchatAccount = new Account("snapchat", mContactSnapchat.getText().toString(), mSnapchatPrivateSwitch.isChecked());
                accounts.put("snapchat", snapchatAccount);

                Account pintrestAccount = new Account("pinterest", mContactPinterest.getText().toString(), mPinterestPrivateSwitch.isChecked());
                accounts.put("pinterest", pintrestAccount);

                Account vimeoAccount = new Account("vimeo", mContactVimeo.getText().toString(), mVimeoPrivateSwitch.isChecked());
                accounts.put("vimeo", vimeoAccount);

                Account flickrAccount = new Account("flickr", mContactFlickr.getText().toString(), mFlickrPrivateSwitch.isChecked());
                accounts.put("flckr", flickrAccount);

                mActiveUser.setAccounts(accounts);
                mActiveUser.postToDB();
            }
        });
    }

    private void setUpRequestButton() {
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If this contact has a private account, send a friend request;
                // otherwise, automatically add this person as a friend and notify them

                if(mThisContact.getPrivateAccount()) {
                    makeRequest();
                } else {
                    addAndNotifyContact();
                }
            }
        });
    }

    private void makeRequest() {
        // Create the request, used for notification
        DatabaseReference requestRef = mNotificationsRef.push();
        requestRef.child("token").setValue(mThisContact.getToken());
        // TODO: This should probably use the key like everything else, if for nothing but consistency
        requestRef.child("requestor").setValue(mActiveUser.getEmail());
        requestRef.child("request").setValue(true);
        // Outstanding must be set to true last, otherwise notifications will be sent after each change to the request
        requestRef.child("outstanding").setValue("true");

        // Put the contact request in the user's contact request list
        DatabaseReference contactRequests = mUsersRef.child(mThisContactKey).child("contactRequests");
        DatabaseReference contactRequest = contactRequests.child(mActiveUser.getCleanEmail());
        contactRequest.child("requestKey").setValue(requestRef.getKey());
    }

    private void addAndNotifyContact() {
        Log.i(TAG, "Adding contact and notifying them");
        // Add to contacts list
        mUsersRef.child(mActiveUser.getCleanEmail()).child("contacts").child(mThisContact.getCleanEmail()).setValue(mThisContact.getCleanEmail());

        // Create the request, used for notification
        DatabaseReference requestRef = mNotificationsRef.push();
        requestRef.child("token").setValue(mThisContact.getToken());
        // TODO: This should probably use the key like everything else, if for nothing but consistency
        requestRef.child("requestor").setValue(mActiveUser.getEmail());
        requestRef.child("request").setValue(false);
        // Outstanding must be set to true last, otherwise notifications will be sent after each change to the request
        requestRef.child("outstanding").setValue("true");
    }

    private void setUpAcceptButton() {
        mAcceptRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add to contacts list
                mUsersRef.child(mThisContactKey).child("contacts").child(mActiveUser.getCleanEmail()).setValue(mActiveUser.getCleanEmail());

                DatabaseReference contactRequest = mUsersRef.child(mActiveUser.getCleanEmail()).child("contactRequests").child(mThisContactKey);

                // Delete the request (for notification)
                Log.i(TAG, "attempting to delete this request: " + contactRequest.child("requestKey").getKey());
                contactRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        dataSnapshot.child("requestKey").getRef().removeValue();
                        mNotificationsRef.child(dataSnapshot.child("requestKey").getValue(String.class)).removeValue();
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

        mContactUserName = findViewById(R.id.userNameTextView);
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
            makeUneditable();
            mUpdateButton.setVisibility(View.INVISIBLE);
        }

        mContactUserName.setTextColor(Color.BLUE);

        mContactFirstName.setText(mThisContact.getFirstName());
        mContactLastName.setText(mThisContact.getLastName());
        mContactEmail.setText(mThisContact.getEmail());
        mContactWebsite.setText(mThisContact.getWebsite());

        Map<String, Account> thisContactAccounts = mThisContact.getAccounts();

        Account facebookAccount = thisContactAccounts.get("facebook");
        if(facebookAccount != null) {
            if(facebookAccount.isPrivate && !self) {
                mContactFacebook.setText("*Private*");
            } else {
                mContactFacebook.setText(facebookAccount.data);
            }
            mFacebookPrivateSwitch.setChecked(facebookAccount.isPrivate);
        }

        Account instagramAccount = thisContactAccounts.get("instagram");
        if(instagramAccount != null) {
            if(instagramAccount.isPrivate && !self) {
                mContactInstagram.setText("*Private*");
            } else {
                mContactInstagram.setText(instagramAccount.data);
            }
            mInstagramPrivateSwitch.setChecked(instagramAccount.isPrivate);
        }

        Account twitterAccount = thisContactAccounts.get("twitter");
        if(twitterAccount != null) {
            if(twitterAccount.isPrivate && !self) {
                mContactTwitter.setText("*Private*");
            } else {
                mContactTwitter.setText(twitterAccount.data);
            }
            mTwitterPrivateSwitch.setChecked(twitterAccount.isPrivate);
        }

        Account linkedinAccount = thisContactAccounts.get("linkedin");
        if(linkedinAccount != null) {
            if(linkedinAccount.isPrivate && !self) {
                mContactLinkedin.setText("*Private*");
            } else {
                mContactLinkedin.setText(linkedinAccount.data);
            }
            mLinkedinPrivateSwitch.setChecked(linkedinAccount.isPrivate);
        }

        Account youtubeAccount = thisContactAccounts.get("youtube");
        if(youtubeAccount != null) {
            if(youtubeAccount.isPrivate && !self) {
                mContactYoutube.setText("*Private*");
            } else {
                mContactYoutube.setText(youtubeAccount.data);
            }
            mYoutubePrivateSwitch.setChecked(youtubeAccount.isPrivate);
        }

        Account snapchatAccount = thisContactAccounts.get("snapchat");
        if(snapchatAccount != null) {
            if(snapchatAccount.isPrivate && !self) {
                mContactSnapchat.setText("*Private*");
            } else {
                mContactSnapchat.setText(snapchatAccount.data);
            }
            mSnapchatPrivateSwitch.setChecked(snapchatAccount.isPrivate);
        }

        Account pinterestAccount = thisContactAccounts.get("pinterest");
        if(pinterestAccount != null) {
            if(pinterestAccount.isPrivate && !self) {
                mContactPinterest.setText("*Private*");
            } else {
                mContactPinterest.setText(pinterestAccount.data);
            }
            mPinterestPrivateSwitch.setChecked(pinterestAccount.isPrivate);
        }

        Account vimeoAccount = thisContactAccounts.get("vimeo");
        if(vimeoAccount != null) {
            if(vimeoAccount.isPrivate && !self) {
                mContactVimeo.setText("*Private*");
            } else {
                mContactVimeo.setText(vimeoAccount.data);
            }
            mVimeoPrivateSwitch.setChecked(vimeoAccount.isPrivate);
        }

        Account flickrAccount = thisContactAccounts.get("flickr");
        if(flickrAccount != null) {
            if(flickrAccount.isPrivate && !self) {
                mContactFlickr.setText("*Private*");
            } else {
                mContactFlickr.setText(flickrAccount.data);
            }
            mFlickrPrivateSwitch.setChecked(flickrAccount.isPrivate);
        }
    }

    private void makeUneditable() {
        Log.i(TAG, "Make uneditable");
        mContactFirstName.setInputType(InputType.TYPE_NULL);
        mContactLastName.setInputType(InputType.TYPE_NULL);
        mContactEmail.setInputType(InputType.TYPE_NULL);
        mContactWebsite.setInputType(InputType.TYPE_NULL);
        mContactFacebook.setInputType(InputType.TYPE_NULL);
        mContactInstagram.setInputType(InputType.TYPE_NULL);
        mContactTwitter.setInputType(InputType.TYPE_NULL);
        mContactLinkedin.setInputType(InputType.TYPE_NULL);
        mContactYoutube.setInputType(InputType.TYPE_NULL);
        mContactSnapchat.setInputType(InputType.TYPE_NULL);
        mContactPinterest.setInputType(InputType.TYPE_NULL);
        mContactVimeo.setInputType(InputType.TYPE_NULL);
        mContactFlickr.setInputType(InputType.TYPE_NULL);

        mEmailPrivateSwitch.setVisibility(View.GONE);
        mLastNamePrivateSwitch.setVisibility(View.GONE);
        mFirstNamePrivateSwitch.setVisibility(View.GONE);
        mWebsitePrivateSwitch.setVisibility(View.GONE);
        mFacebookPrivateSwitch.setVisibility(View.GONE);
        mInstagramPrivateSwitch.setVisibility(View.GONE);
        mTwitterPrivateSwitch.setVisibility(View.GONE);
        mLinkedinPrivateSwitch.setVisibility(View.GONE);
        mYoutubePrivateSwitch.setVisibility(View.GONE);
        mSnapchatPrivateSwitch.setVisibility(View.GONE);
        mPinterestPrivateSwitch.setVisibility(View.GONE);
        mVimeoPrivateSwitch.setVisibility(View.GONE);
        mFlickrPrivateSwitch.setVisibility(View.GONE);
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
        //Log.i(TAG, "Setting up contact");
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
                // TODO: Might only want to do this once?
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
