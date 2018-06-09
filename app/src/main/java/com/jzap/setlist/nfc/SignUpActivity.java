package com.jzap.setlist.nfc;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by JZ_W541 on 4/3/2018.
 */



public class SignUpActivity extends AppCompatActivity {

    private final static String TAG = "JAZ_NFC";

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    private Button mSignUpButton;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mUserNameEditText;
    private EditText mPhoneEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private ImageView mProfilePictureImageView;

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        setUpPermissions();
        setUpSignUp();
    }

    private void setUpPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "No permission yet");
            requestWriteExternalStoragePermission();
        }
    }

    private void requestWriteExternalStoragePermission() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Inform and request")
                    .setMessage("You need to enable permissions, bla bla bla")
                    .setPositiveButton("a string", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 111: {
           /*     // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && PackageManager.PERMISSION_GRANTED
                // allowed
            } else {
                // denied
            }*/
            break;
            }
        }
    }

    private void setUpSignUp() {

        displayPhoto();

        mFirstNameEditText = (EditText) findViewById(R.id.firstNameEditText);
        mLastNameEditText = (EditText) findViewById(R.id.lastNameEditText);
        mUserNameEditText = (EditText) findViewById(R.id.userNameEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordEditText);

        mSignUpButton = (Button) findViewById(R.id.submitChangesButton);



        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPicture(mEmailEditText.getText().toString());
            }
        });
    }

    private void uploadPicture(String cleanEmail) {
        StorageReference pictureRef = storageRef.child("userImages").child(cleanEmail + ".jpg");

        // Get the data from an ImageView as bytes
        mProfilePictureImageView.setDrawingCacheEnabled(true);
        mProfilePictureImageView.buildDrawingCache();
        Bitmap bitmap = mProfilePictureImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        final String defaultWebsite = "defaultWebsite";
        final String defaultPhotoURL = "http://mehandis.net/wp-content/uploads/2017/12/default-user.png";
        final Context context = this;

        UploadTask uploadTask = pictureRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri pictureUrl = taskSnapshot.getDownloadUrl();
                String pictureUrlS = pictureUrl.toString();
                Log.i(TAG, "Picture download url = " + pictureUrl.toString());

                // TODO: Validate
                // TODO: I'm waiting to upload the user to the db until the profile picture uploads to storage, which might not be the best idea, and is a bit confusing
                HashSet<String> contacts = new HashSet<>(); // TODO: Put contacts in here?
                String token = ActiveUser.getToken(context);
                User user = new User(mEmailEditText.getText().toString(),
                        mFirstNameEditText.getText().toString(),
                        mLastNameEditText.getText().toString(),
                        mUserNameEditText.getText().toString(),
                        defaultWebsite,
                        mPasswordEditText.getText().toString(),
                        mPhoneEditText.getText().toString(),
                        token,
                        pictureUrlS,
                        false,
                        contacts
                );
                user.postToDB();
                logIn(User.cleanEmail(mEmailEditText.getText().toString()));
            }
        });

    }

    private void displayPhoto() {
        mProfilePictureImageView = (ImageView) findViewById(R.id.signUpPhotoImageView);

        final String defaultPhotoURL = "http://mehandis.net/wp-content/uploads/2017/12/default-user.png";

        // TODO: Can't start a load for a destroyed activity crash?
        Glide.with(getApplicationContext()).load(defaultPhotoURL).listener(new RequestListener<Drawable>() {
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
        }).into(mProfilePictureImageView);

        final Activity context = this;

        mProfilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(context);
                }
                builder.setTitle("Set Profile Picture")
                        .setMessage("Take a new picture or choose from gallery?")
                        .setPositiveButton("Take picture", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                                    // Create the File where the photo should go
                                    File photoFile = createImageFile();
                                    // Continue only if the File was successfully created
                                    if (photoFile != null) {
                                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                        cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        startActivityForResult(cameraIntent, 0);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
                            }
                        })
                        //.setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private File createImageFile()  {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Log.i(TAG, "Storage dir = " + storageDir.getAbsolutePath());
        File image = null;
        try {
            image = File.createTempFile(imageFileName,".jpg", storageDir);
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.i(TAG, "Got profile picture result");
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Bitmap bm = null;
                    try {
                        bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mProfilePictureImageView.setImageBitmap(bm);
                }
                break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    mProfilePictureImageView.setImageURI(selectedImage);
                }
                break;
        }
    }

    // TODO: This is duplicated from SignInActivity
    private void logIn(String userName) {
        ActiveUser.setUserKey(this, userName);
        writeTokenToDB(userName, ActiveUser.getToken(this));
        startMainActivity();
    }

    // TODO: This is duplicated in FirebaseIDService and SignInActivity
    private void writeTokenToDB(final String userName, final String token) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = rootRef.child("users");
        usersRef.child(userName).child("token").setValue(token);
    }

    // TODO: This is duplicated from SignInActivity
    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
    }

}
