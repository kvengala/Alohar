package com.trnql_devpost.kashyap.callbusy;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.trnql.smart.base.SmartFragment;
import com.trnql.smart.location.AddressEntry;
import com.trnql.smart.location.LocationEntry;
import com.trnql_devpost.kashyap.callbusy.R;

public class activity_location extends SmartFragment {
    TextView location_latitude;
    TextView location_longitude;
    TextView location_address;
    TextView location_accuracy;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_activity_location, container, false);
        location_latitude = (TextView) view.findViewById(R.id.location_latitude);
        location_longitude = (TextView) view.findViewById(R.id.location_longitude);
        location_address = (TextView) view.findViewById(R.id.location_address);
        location_accuracy = (TextView) view.findViewById(R.id.location_accuracy);
        return view;
    }

    @Override
    protected void smartLatLngChange(LocationEntry location) {
        String lat_string = String.valueOf(location.getLatitude());
        String long_string = String.valueOf(location.getLongitude());
        String acc_string = String.valueOf(location.getAccuracy());

        location_latitude.setText("Latitude:   %s" + lat_string);
        location_longitude.setText("Longitude:   %s" + long_string);
        location_accuracy.setText("Accuracy:   %s" + acc_string);
    }

    @Override
    protected void smartAddressChange(AddressEntry address) {
        String addr_string = address.toString();
        location_address.setText("Address:   %s" + addr_string);
    }
}
