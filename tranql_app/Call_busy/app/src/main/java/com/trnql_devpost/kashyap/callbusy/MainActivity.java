package com.trnql_devpost.kashyap.callbusy;

import android.support.v7.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import android.os.Bundle;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.trnql.smart.activity.ActivityEntry;

public class MainActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */


    TextView textview;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        CallbackManager callbackManager;
        setContentView(R.layout.activity_login_activity);
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

    }
    //@Override
    protected void smartActivityChange(ActivityEntry userActivity) {
        textview.setText(userActivity.getActivityString());
    }
}