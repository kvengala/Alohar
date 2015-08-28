/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alohar.context.api.AcxLocationManager;
import com.alohar.context.api.AcxLocationManager.AcxLocationCallback;
import com.alohar.context.api.AcxServiceManager;
import com.alohar.context.api.AcxServiceManager.AcxServerCallback;
import com.alohar.context.api.model.AcxError;

public class ASHomeFragment extends Fragment {

    private Context context;
    private Handler handler;
    private EditText uidEditText;
    private Switch aloharContextMonitoringSwitch;
    private TextView currentLocationTextView;
    private TextView lastKnownLocationTextView;
    private String uid = null;

    private AcxServiceManager serviceManager;
    private AcxLocationManager locationManager;

    private void handleGetLastKnownLocationRequest() {
        String message = "Getting last known location...";
        lastKnownLocationTextView.setText(message);
        final String defaultMessage = getString(R.string.last_known_location_default);
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (serviceManager.isSignedIn()) {
                    Location lastKnownLocation = locationManager.getLastKnownLocation();
                    if (lastKnownLocation != null) {
                        String latLngTime = "(" + lastKnownLocation.getLatitude() + ", " +
                                lastKnownLocation.getLongitude() + ") at " +
                                ASUtility.getDateTimeString(lastKnownLocation.getTime());
                        lastKnownLocationTextView.setText(latLngTime);
                    } else {
                        lastKnownLocationTextView.setText(defaultMessage);
                    }
                } else {
                    lastKnownLocationTextView.setText(defaultMessage);
                }
            }

        }, 1000);
    }

    private void handleUploadRequest() {
        String message = "Uploading context data";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        AcxServiceManager.getInstance().uploadContextData(new AcxServerCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                final String message = "Uploaded all context data.";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(AcxError error) {
                final String message = "Failed to upload context data.";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

        });
    }

    private void handleLogoutRequest() {
        String message = "Logging out";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        serviceManager.stopContextService();
        serviceManager.signOut();

        // Clear events when switching UID.
        ASEventsManager.getInstance().clearEvents();

        // Go to the login/register activity.
        Intent intent = new Intent(getActivity(), ASLoginRegisterActivity.class);
        startActivity(intent);
        // Close the main activity.
        getActivity().finish();
    }

    private void refreshUI() {
        uidEditText.setText(uid);
        updateAloharServiceStatus();
    }

    private void updateAloharServiceStatus() {
        if (serviceManager.isContextServiceOn()) {
            aloharContextMonitoringSwitch.setChecked(true);
        } else {
            aloharContextMonitoringSwitch.setChecked(false);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();
        handler = new Handler();

        // Get Alohar instance which was initialized in the ASApplication class.
        serviceManager = AcxServiceManager.getInstance();

        // Get the currently authenticated UID.
        uid = serviceManager.getAloharUid();

        // Get Alohar place manager for location and stay data.
        locationManager = serviceManager.getLocationManager();

    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        uidEditText = (EditText) root.findViewById(R.id.uid_edittext);
        aloharContextMonitoringSwitch = (Switch) root.findViewById(R.id.context_monitoring_switch);
        currentLocationTextView = (TextView) root.findViewById(R.id.current_location);
        lastKnownLocationTextView = (TextView) root.findViewById(R.id.last_known_location);
        Button lastKnownLocationButton = (Button) root.findViewById(
                R.id.get_last_known_location_button);
        Switch wifiOnlySwitch = (Switch) root.findViewById(R.id.wifi_only_switch);
        Button uploadButton = (Button) root.findViewById(R.id.upload_button);
        Button logoutButton = (Button) root.findViewById(R.id.logout_button);

        uidEditText.setText(uid);

        lastKnownLocationButton.setOnClickListener(new View.OnClickListener() {

            /* (non-Javadoc)
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            @Override
            public void onClick(View v) {
                handleGetLastKnownLocationRequest();
            }
        });

        aloharContextMonitoringSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            /* (non-Javadoc)
             * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Turn on Alohar service.
                    if (!serviceManager.isContextServiceOn()) {
                        serviceManager.startContextService();
                    }
                } else {
                    // Turn off Alohar service.
                    if (serviceManager.isContextServiceOn()) {
                        serviceManager.stopContextService();
                    }
                }
                // Start/stop service may take some time so delay refreshing the UI.
                handler.postDelayed(new Runnable() {

                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    @Override
                    public void run() {
                        refreshUI();
                    }
                }, 3000);
            }
        });

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isWifiOnly = preferences.getBoolean(ASConstant.PREF_KEY_WIFI_ONLY, false);
        serviceManager.setUploadContextDataOnlyOnWifi(isWifiOnly);
        wifiOnlySwitch.setChecked(isWifiOnly);
        wifiOnlySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean(ASConstant.PREF_KEY_WIFI_ONLY, isChecked).apply();
                serviceManager.setUploadContextDataOnlyOnWifi(isChecked);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {

            /* (non-Javadoc)
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            @Override
            public void onClick(View v) {
                handleUploadRequest();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {

            /* (non-Javadoc)
             * @see android.view.View.OnClickListener#onClick(android.view.View)
             */
            @Override
            public void onClick(View v) {
                handleLogoutRequest();
            }
        });

        return root;
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();

        // Unregister for Alohar location events.
        if (serviceManager.isSignedIn()) {
            locationManager.removeLocationUpdates();
        }
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

        // Register for Alohar location events.
        if (serviceManager.isSignedIn()) {
            locationManager.requestLocationUpdates(new AcxLocationCallback() {
                @Override
                public void onLocationUpdate(Location location) {
                    final String latLngTime = "(" + location.getLatitude() + ", " +
                            location.getLongitude() + ") at " +
                            ASUtility.getDateTimeString(location.getTime());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            currentLocationTextView.setText(latLngTime);
                        }
                    });
                }
            });
        }
        refreshUI();
    }
}
