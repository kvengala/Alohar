package com.trnql.sample_basic_notabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.trnql.smart.activity.ActivityEntry;
import com.trnql.smart.base.SmartFragment;

/**
 * @author Akhil Indurti
 * @version 1.0
 * @since 5/4/15, 12:23 PM
 */
public class ActivityFragment extends SmartFragment {
TextView activity_movement;
TextView activity_tilting;

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  View view = inflater.inflate(R.layout.frag_activity, container, false);
  activity_movement = (TextView) view.findViewById(R.id.activity_movement);
  activity_tilting = (TextView) view.findViewById(R.id.activity_tilting);
  return view;
}

@Override
protected void smartActivityChange(ActivityEntry value) {
  activity_movement.setText(String.format("Movement:   %s", value.getActivityString()));
  activity_tilting.setText(String.format("Tilting:   %s", value.isTilting() ? "Yes" : "No"));
}

public static ActivityFragment getInstance() {

  return new ActivityFragment();
}
}//end class ActivityFragment
