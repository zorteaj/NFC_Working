package com.jzap.setlist.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class ContactActivity extends AppCompatActivity {

    private TextView mContactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        mContactName = (TextView) findViewById(R.id.contactTextView);
        Intent intent = getIntent();

        mContactName.setText(intent.getStringExtra("CONTACT_EMAIL"));
    }
}
