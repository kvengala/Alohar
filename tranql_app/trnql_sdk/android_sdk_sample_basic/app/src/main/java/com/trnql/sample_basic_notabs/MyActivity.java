package com.trnql.sample_basic_notabs;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.trnql.zen.core.AppData;

public class MyActivity extends AppCompatActivity {
/**
 * Called when the activity is first created.
 */
@Override
public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.main);
  AppData.startAllServices(this);
}


@Override
protected void onDestroy() {
  AppData.stopAllServices(getApplication());
  super.onDestroy();
}
}
