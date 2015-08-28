/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alohar.context.api.AcxLocationManager;
import com.alohar.context.api.AcxServiceManager;
import com.alohar.context.api.AcxServiceManager.AcxServerCallback;
import com.alohar.context.api.AcxUserStayManager;
import com.alohar.context.api.model.AcxError;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class ASLoginRegisterActivity extends Activity { // implements ALEventListener {

    private static final String TAG = ASLoginRegisterActivity.class.getSimpleName();

    private View loginRegisterLayout;
    private EditText uidEditText;
    private ProgressBar progressBar;

    private Handler handler;

    private AcxServiceManager serviceManager;
    private String uid = null;
    private String inputValidUidString;
    private String sdkVersionString;
    private String appNameString;

    private void handleLogInRequest() {
        String inputUid = uidEditText.getText().toString();
        if (inputUid.trim().length() == 0) {
            Toast.makeText(this, inputValidUidString, Toast.LENGTH_SHORT).show();
        } else {
            uid = inputUid;
            loginRegisterLayout.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            serviceManager.signIn(uid, new AcxServerCallback<String>() {

                @Override
                public void onSuccess(String aloharUid) {
                    handleSignInResponse(aloharUid);
                }

                @Override
                public void onError(AcxError error) {
                    handleError(error);
                }
            });
        }
    }

    private void handleRegisterRequest() {
        serviceManager.createUser(new AcxServerCallback<String>() {
            @Override
            public void onSuccess(String aloharUid) {
                handleSignUpResponse(aloharUid);
            }

            @Override
            public void onError(AcxError error) {
                handleError(error);
            }
        });

        loginRegisterLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void storeUid() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(ASConstant.PREF_KEY_UID, uid).commit();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, ASMainActivity.class);
        startActivity(intent);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get AcxServiceManager instance which was initialized in the ASApplication class.
        serviceManager = AcxServiceManager.getInstance();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        uid = prefs.getString(ASConstant.PREF_KEY_UID, null);
        Log.i(TAG, "Last UID used: " + uid);

        // Check if a UID is currently registered or previously stored.
        if ((uid != null) && (!uid.isEmpty())) {
            // UID exists, check if signed in.
            if (serviceManager.isSignedIn()) {
                // Already authenticated so continue on to the main activity.
                startMainActivity();
                // Close the authenticate/register activity.
                finish();
                return;
            }
        }

        inputValidUidString = getString(R.string.login_valid_uid);
        sdkVersionString = getString(R.string.login_sdk_version);
        appNameString = getString(R.string.app_name);

        handler = new Handler();

        setContentView(R.layout.activity_login_register);

        loginRegisterLayout = findViewById(R.id.login_register_layout);
        uidEditText = (EditText) findViewById(R.id.login_uid_edittext);
        progressBar = (ProgressBar) findViewById(R.id.login_register_progressbar);

        loginRegisterLayout.setVisibility(View.VISIBLE);

        // Populate the UI with the last used UID if it is available.
        if (uid != null) {
            uidEditText.setText(uid);
        }

        // Set up login button.
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {

            /* (non-Javadoc)
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            @Override
            public void onClick(View view) {
                handleLogInRequest();
            }
        });

        // Set up register button.
        findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {

            /* (non-Javadoc)
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            @Override
            public void onClick(View view) {
                handleRegisterRequest();
            }
        });
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPostCreate(android.os.Bundle)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Update the title with Alohar SDK version.
        String title = String.format(sdkVersionString, appNameString, serviceManager.getSdkVersion());
        setTitle(title);
    }

    private void handleSignUpResponse(String aloharUid) {
        uid = aloharUid;
        Log.i(TAG, "Sign up success: UID=" + uid);
        handler.post(new Runnable() {

            @Override
            public void run() {
                // Save the UID to shared preferences.
                storeUid();

                // Turn the context monitoring service on by default.
                serviceManager.startContextService();

                requestUpdates();

                // Continue on to the main activity.
                startMainActivity();
                finish();
            }
        });
    }

    private void handleSignInResponse(String aloharUid) {
        uid = aloharUid;
        Log.i(TAG, "Sign in success: UID=" + uid);
        handler.post(new Runnable() {

            @Override
            public void run() {
                // Save the UID to shared preferences.
                storeUid();
                // Turn the context monitoring service on by default.
                serviceManager.startContextService();

                requestUpdates();

                // Continue on to the main activity.
                startMainActivity();
                finish();
            }
        });
    }

    private void requestUpdates() {
        if (serviceManager.isSignedIn()) {
            ASEventsManager asEventsManager = ASEventsManager.getInstance();
            AcxLocationManager locationManager = serviceManager.getLocationManager();        
            locationManager.requestPotentialUserStayUpdates(asEventsManager);

            AcxUserStayManager userStayManager = serviceManager.getUserStayManager();
            userStayManager.requestUserStayUpdates(asEventsManager);
        }
    }

    private void handleError(AcxError error) {
        final String message = error.getMessage();
        handler.post(new Runnable() {

            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                loginRegisterLayout.setVisibility(View.VISIBLE);
                Toast.makeText(ASLoginRegisterActivity.this, message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
