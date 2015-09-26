package com.trnql_devpost.kashyap.callbusy;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
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
    }
    //@Override
    protected void smartActivityChange(ActivityEntry userActivity) {
        textview.setText(userActivity.getActivityString());
    }
}