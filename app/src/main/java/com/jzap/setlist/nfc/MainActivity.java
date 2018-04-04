package com.jzap.setlist.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "JAZ_NFC";

    private NfcAdapter mNfcAdapter;

    private GoogleSignInClient mGoogleSignInClient;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("users");

    private Button mLogOutButton;
    private TextView mAccountNameTextView;
    private RecyclerView mUsersRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getUsersFromDB();
        setUpUserAccount();
        setUpNFC();
        setUpGoogleSignOut();
        setUpLogOut();
        debugPrintToken();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setUpLogOut() {
        final Context context = this;
        final Activity activity = this;
        mLogOutButton = (Button) findViewById(R.id.logOutButton);
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveSharedPreference.clearUserName(context);
                startSignInActivity();

                mGoogleSignInClient.signOut().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i(TAG, "Signed out");
                    }
                });
            }
        });
    }

    private void setUpGoogleSignOut() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void startSignInActivity() {
        Intent signInIntent = new Intent(this, SignInActivity.class);
        startActivity(signInIntent);
    }

    private void setUpUserAccount() {
       mAccountNameTextView = (TextView) findViewById(R.id.allUsersTextView); // TODO: hack
       mAccountNameTextView.setText(SaveSharedPreference.getUserName(this));
   }

    private void displayUsers(HashMap<String, User> users) {
        mUsersRecyclerView = (RecyclerView) findViewById(R.id.usersRecyclerView);
        UsersRecyclerViewAdptr adptr = new UsersRecyclerViewAdptr(users, this);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUsersRecyclerView.setAdapter(adptr);
        //adptr.notifyItemChanged(0);
    }

    private void debugPrintToken() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "Token = " + refreshedToken);
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

        Intent intent = getIntent();
        if(intent != null && intent.getAction() == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            Log.i(TAG, "Got NDEF");
            processTag(intent);
        } else {
            Log.i(TAG, "Did NOT get NDEF");
        }
    }

    private void setUpButton() {
       /* mUserId = (TextView) findViewById(R.id.userId);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mTestRef.setValue("New Value");
            }
        });*/
    }

    private void getUsersFromDB() {
        final HashMap<String, User> users = new HashMap<>();
        final HashSet<String> contactsSet = new HashSet<>();
        mUsersRef.addListenerForSingleValueEvent(new ValueEventListener() { // was addValueEventListener
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    DataSnapshot contactsDB = snap.child("contacts");
                    for(DataSnapshot contact : contactsDB.getChildren()) {
                        contactsSet.add(contact.getValue(String.class));
                    }
                    try {
                        User user = new User(snap.child("email").getValue(String.class),
                                snap.child("firstName").getValue(String.class),
                                snap.child("lastName").getValue(String.class),
                                snap.child("website").getValue(String.class),
                                snap.child("password").getValue(String.class),
                                snap.child("phone").getValue(String.class),
                                contactsSet );

                        if(user != null) {
                            users.put(user.getEmail(), user);
                        }
                    } catch(DatabaseException e) {
                        Log.e(TAG, "Couldn't make a user out of this db value");
                        Log.e(TAG, e.toString());
                    }
                }
                displayUsers(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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
                    //mUserId.setText(payload);
                }
            }
        } else {
            Log.i(TAG, "Problem reading tag");
        }
    }
}
