// This is a dump of Facebook login and data fetching

//package com.jzap.setlist.nfc;
//
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.Signature;
//import android.os.Bundle;
//import android.util.Base64;
//import android.util.Log;
//
//import com.facebook.AccessToken;
//import com.facebook.CallbackManager;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.FacebookSdk;
//import com.facebook.GraphRequest;
//import com.facebook.GraphResponse;
//import com.facebook.appevents.AppEventsLogger;
//import com.facebook.login.LoginResult;
//import com.facebook.login.widget.LoginButton;
//
//import org.json.JSONObject;
//
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;
//
///**
// * Created by JZ_W541 on 6/16/2018.
// */
//
//public class FacebookAdapter {
//
//    CallbackManager callbackManager;
//    private LoginButton loginButton;
//    private static final String EMAIL = "email";
//    private static final String LINK = "user_link";
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    private void logHashKey() {
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.jzap.setlist.nfc",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.i(TAG, Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, e.toString());
//        } catch (NoSuchAlgorithmException e) {
//            Log.e(TAG, e.toString());
//        }
//    }
//
//    private void testFacebookSetup() {
//        callbackManager = CallbackManager.Factory.create();
//        loginButton = (LoginButton) findViewById(R.id.login_button);
//        loginButton.setReadPermissions(Arrays.asList(LINK));
//        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                // App code
//                Log.i(TAG, "FACEBOOK SUCCESS!");
//                AccessToken accessToken = loginResult.getAccessToken();
//                GraphRequest request = GraphRequest.newMeRequest(
//                        accessToken,
//                        new GraphRequest.GraphJSONObjectCallback() {
//                            @Override
//                            public void onCompleted(
//                                    JSONObject object,
//                                    GraphResponse response) {
//                                // Application code
//                                Log.i(TAG, "Graph request callback!");
//                                Log.i(TAG, object.toString());
//                                Log.i(TAG, response.getRawResponse());
//                            }
//                        });
//                Bundle parameters = new Bundle();
//                parameters.putString("fields", "id,name,link");
//                request.setParameters(parameters);
//                request.executeAsync();
//            }
//
//            @Override
//            public void onCancel() {
//                // App code
//                Log.i(TAG, "FACEBOOK CANCEL!");
//            }
//
//            @Override
//            public void onError(FacebookException exception) {
//                // App code
//                Log.i(TAG, "FACEBOOK ERROR!");
//            }
//        });
//    }
//}

/*
<com.facebook.login.widget.LoginButton
        android:id="@+id/login_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center_horizontal"
        android:layout_marginTop="30dp"
        android:layout_marginBottom="30dp" />*/
