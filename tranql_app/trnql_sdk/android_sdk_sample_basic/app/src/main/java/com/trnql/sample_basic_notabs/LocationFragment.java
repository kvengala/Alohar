package com.trnql.sample_basic_notabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.trnql.smart.base.SmartFragment;
import com.trnql.smart.location.AddressEntry;
import com.trnql.smart.location.LocationEntry;

/**
 * @author Akhil Indurti
 * @version 1.0
 * @since 5/4/15, 12:23 PM
 */
public class LocationFragment extends SmartFragment {
TextView location_latitude;
TextView location_longitude;
TextView location_address;
TextView location_accuracy;


@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  View view = inflater.inflate(R.layout.frag_location, container, false);
  location_latitude = (TextView) view.findViewById(R.id.location_latitude);
  location_longitude = (TextView) view.findViewById(R.id.location_longitude);
  location_address = (TextView) view.findViewById(R.id.location_address);
  location_accuracy = (TextView) view.findViewById(R.id.location_accuracy);
  return view;
}

@Override
protected void smartLatLngChange(LocationEntry value) {
  location_latitude.setText(String.format("Latitude:   %s", String.valueOf(value.getLatitude())));
  location_longitude.setText(String.format("Longitude:   %s", String.valueOf(value.getLongitude())));
  location_accuracy.setText(String.format("Accuracy:   %s", String.valueOf(value.getAccuracy())));
}

@Override
protected void smartAddressChange(AddressEntry value) {
  location_address.setText(String.format("Address:   %s", value.toString()));
}

public static LocationFragment getInstance() {
  return new LocationFragment();
}

}//end class LocationFragment
