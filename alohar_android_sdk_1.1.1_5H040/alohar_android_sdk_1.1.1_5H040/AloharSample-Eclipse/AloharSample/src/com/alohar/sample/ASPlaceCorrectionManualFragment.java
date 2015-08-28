/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.alohar.context.api.AcxServiceManager;
import com.alohar.context.api.AcxServiceManager.AcxServerCallback;
import com.alohar.context.api.AcxUserStayManager;
import com.alohar.context.api.model.AcxError;
import com.alohar.context.api.model.AcxPlace;
import com.alohar.context.api.model.AcxUserStay;

/**
 * {@code ASPlaceCorrectionManualFragment} class demonstrates the usage of the following APIs:
 * 1. Correct the place corresponding to a selected stay by manually editing the
 *    name and address of the place.
 */
public class ASPlaceCorrectionManualFragment extends Fragment {

    private static final String TAG = ASPlaceCorrectionManualFragment.class.getSimpleName();

    private EditText placeAddressEditText;
    private EditText placeNameEditText;

    private AcxPlace selectedPlaceFromUserStay;
    private boolean editInProgress = false;
    private long stayId;

    private AcxUserStayManager userStayManager;

    private String placeCorrectionFailedString;
    private String placeCorrectionSuccessString;
    private String placeCorrectionFailedNoPlaceName;
    private String placeCorrectionFailedNoPlaceAddress;
    private String placeCorrectionNotRequiredString;
    private String placeCorrectionCancelledString;
    private String placeCorrectionEntryGeocodeErrorString;

    private TextWatcher placeCorrectionTextWatcher = new TextWatcher() {

        /*
         * (non-Javadoc)
         * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        /*
         * (non-Javadoc)
         * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        /*
         * (non-Javadoc)
         * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
         */
        @Override
        public void afterTextChanged(Editable s) {
            enableDoneMenu(true);
        }
    };

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        userStayManager = AcxServiceManager.getInstance().getUserStayManager();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_place_correction_manual, container, false);

        // EditText for PlaceName and Place Address
        placeAddressEditText = (EditText) root.findViewById(R.id.place_details_address);
        placeAddressEditText.addTextChangedListener(placeCorrectionTextWatcher);
        placeNameEditText = (EditText) root.findViewById(R.id.place_details_name);
        placeNameEditText.addTextChangedListener(placeCorrectionTextWatcher);

        placeCorrectionFailedString = getString(R.string.place_correction_failed);
        placeCorrectionSuccessString = getString(R.string.place_correction_success);
        placeCorrectionFailedNoPlaceName = getString(R.string.place_correction_failed_placename_empty);
        placeCorrectionFailedNoPlaceAddress = getString(R.string.place_correction_failed_placeaddress_empty);
        placeCorrectionNotRequiredString = getString(R.string.place_correction_not_required);
        placeCorrectionCancelledString = getString(R.string.place_correction_cancelled);
        placeCorrectionEntryGeocodeErrorString = getString(R.string.place_correction_address_entry_geocode_error);

        Object obj = getActivity().getIntent()
                .getParcelableExtra(ASStaysListFragment.INTENT_EXTRA_KEY_USERSTAY);

        if (obj instanceof AcxUserStay) {
            // Set the name and address of the place seelcted from Stay
            AcxUserStay userStay = (AcxUserStay)obj;
            if (userStay != null) {
                stayId = userStay.getId();
                selectedPlaceFromUserStay = userStay.getSelectedPlace(); 
                if (selectedPlaceFromUserStay != null) {
                    placeNameEditText.setText(selectedPlaceFromUserStay.getName());
                    placeAddressEditText.setText(selectedPlaceFromUserStay.getAddress());
                }
            }
        }
        enableDoneMenu(false);

        return root;
    }

    /**
     * Helper function to showcase the usage of Alohar SDK API for correcting a stay
     * with a place specified with name and address manually by user-input.
     * 
     * Stay correction API requires the following input parameters:
     *     Stay ID: of the user stay {@code ALUserStay} object that was selected for correction.
     *     Owner Alohar Uid: UID of the registered owner.
     *     Place Name: Name of the place.
     *     Place Address: Address of the place.
     *     Category(Optional): Category of the place
     *     Response listener: Callback listener for the handle the response of the asynchronous
     *         call for place correction.
     *
     * @param placeName The name of the place (edited by end-user)
     * @param placeAddressString The address of the place. (edited by end-user)
     * @param latitude The latitude of the place found by reverse geocoding using {@code Geocoder}
     * @param longitude The longitude of the place found by reverse geocoding using {@code Geocoder
     */
    private void placeCorrectionForPersonalPoi(String placeName, 
            String placeAddressString, double latitude, double longitude) {
        final Activity activity = getActivity();
        Log.d(TAG, "placeCorrectionForPersonalPoi(): [" + placeName + ", "
            + placeAddressString + ", lat:" + latitude + ", long:" + longitude + "]");

        userStayManager.correctUserStay(stayId, placeName, placeAddressString, latitude, longitude,
                new AcxServerCallback<AcxUserStay>(){

            @Override
            public void onSuccess(AcxUserStay result) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (activity == null || activity.isFinishing()) {
                            return;
                        }
                        Toast.makeText(activity, placeCorrectionSuccessString,
                                Toast.LENGTH_SHORT).show();
                    }
                });

                finish(activity);
            }

            @Override
            public void onError(AcxError error) {

                final String message = String.format(placeCorrectionFailedString, error.getMessage());

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (activity == null || activity.isFinishing()) {
                            return;
                        }
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });

                finish(activity);
            }
        });
        
    }


    /**
     * Handler function for the "Done" menu action confirming place correction 
     *  - after manual edit is completed.
     */
    private void onDoneMenuActionSelected() {
        
        // Read the Name and Address of the manually edited place.
        String editedPlaceName = placeNameEditText.getText().toString();
        editedPlaceName = editedPlaceName.trim();

        String editedPlaceAddress = placeAddressEditText.getText().toString();
        editedPlaceAddress = editedPlaceAddress.trim();

        // Check for validity of the input fields.
        if (editedPlaceName == null || editedPlaceName.length() == 0) {
            Toast.makeText(getActivity(), placeCorrectionFailedNoPlaceName, Toast.LENGTH_SHORT).show();
            enableDoneMenu(false);
            return;
        } else if (editedPlaceAddress == null || editedPlaceAddress.length() == 0) {
            Toast.makeText(getActivity(), placeCorrectionFailedNoPlaceAddress, Toast.LENGTH_SHORT).show();
            enableDoneMenu(false);
            return;
        }

        if (selectedPlaceFromUserStay != null && editedPlaceName.equals(selectedPlaceFromUserStay.getName())
                && editedPlaceAddress.equals(selectedPlaceFromUserStay.getAddress())) {
            Toast.makeText(getActivity(), placeCorrectionNotRequiredString, Toast.LENGTH_SHORT).show();
            enableDoneMenu(false);
        } else {
            // Reverse Geocode it to get a valid Location.
            // Then call Alohar SDK api to do the place correction.
            addressLookUpAndPlaceCorrectionForPersonalPOI(editedPlaceAddress);
        }
    }

    /**
     * Helper function to instantiate an asynchronous task to get {@code Address} 
     * from a address String using Google's Reverse-Geocode API.
     *
     * @param editedPlaceAddress Address string edited manually by end-user.
     */
    private void addressLookUpAndPlaceCorrectionForPersonalPOI(String editedPlaceAddress) {
        // Get valid Latlog from the address
        // If valid Latlng found, create personal place - call SDK api to correct with user input.
        // If valid latlong not found, show failure to update.
        Log.d(TAG, "startPersonalPlaceCorrection() for: " + editedPlaceAddress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
            // Show the activity indicator

            /*
             * Reverse geocoding is long-running and synchronous.
             * Run it on a background thread.
             * Pass the current location to the background task.
             * When the task finishes, onPostExecute() displays the address.
             */
            GetAddressTask getAddressAsyncTask = new GetAddressTask(getActivity());
            getAddressAsyncTask.execute(editedPlaceAddress);
        }
    }

    /**
     * A subclass of AsyncTask that calls getFromLocationName() in the
     * background. The class definition has these generic types:
     * Object  - A String object containing the current location's address string.
     * Void    - Indicates that progress units are not used
     * Address - An address object passed to onPostExecute()
     */
    private class GetAddressTask extends AsyncTask<Object, Void, Address> {

        Context context;
        Object[] params = null;

        public GetAddressTask(Context context) {
            super();
            this.context = context;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude look up the
         * address, and return it
         * 
         * @params params One or more Location objects
         * @return A string containing the address of the current location, or
         *         an empty string if no address can be found, or an error
         *         message
         */
        @Override
        protected Address doInBackground(Object... params) {
            this.params = params;
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String addressStr = (String)params[0];
            try {
                // Return 1 address.
                List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
                // If the reverse geocode returned an address
                if (addresses != null && addresses.size() > 0) {
                    // Get the first address
                    Address addressFromAddress = addresses.get(0);
                    Log.d(TAG, "getFromLocationName() Locale.getDefault() " + Locale.getDefault()
                            + ", addressStr: " + addressStr + ", address: " + addressFromAddress.toString());
                    return addressFromAddress;
                } else {
                    return null;
                }
            } catch (IOException e1) {
                Log.e(TAG, "IO Exception in getFromLocation() : " + e1.getMessage());
                e1.printStackTrace();
                return null;
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal argument "+ addressStr + " passed to address service";
                Log.e(TAG, errorString);
                e2.printStackTrace();
                return null;
            }
        }

        /**
         * A method that's called once doInBackground() completes.
         * Invoke the place correction API using the location information in
         * {@code Address}. If the lookup has failed, display the error
         * message.
         */
        @Override
        protected void onPostExecute(Address address) {
            // Display the results of the lookup.
            onAddressLookUpComplete(params, address);
        }
     }

    /**
     * Method to act upon the result of {@code Address} lookup using Google Geocode API.
     * If look up is successful, use its latlong to invoke place correction.
     * If look up failed either due to wrong address string input by end-user or
     * due to network failure, display error message why place correction failed.
     *   
     * @param params The input parameters by caller of Address lookup.
     * @param address The {@code Address} object returned by Google Geocode API on lookup via address string.
     */
    private void onAddressLookUpComplete(Object[] params, Address address) {

        if (address != null) {
            // Call helper function that showcases the usage of API for place correction with personal POI
            // i.e POI manually edited by user.
            placeCorrectionForPersonalPoi(placeNameEditText.getText().toString(),
                    placeAddressEditText.getText().toString(),
                    address.getLatitude(), address.getLongitude());
        } else {
            Log.e(TAG, "Address not found. Either server error or Invalid Address fields");
            final Activity activity = getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            Toast.makeText(activity, placeCorrectionEntryGeocodeErrorString,
                    Toast.LENGTH_SHORT).show();
            finish(activity);
        }
    }

    /**
     * Method to enable/disable the "Done" menu action based on whether
     * place has been selected from candidates list or edited in the text boxes manually.
     *
     * @param enable true to enable the "Done" menu action, else disable.
     */
    private void enableDoneMenu(boolean enable) {
        if (enable) {
            if (!editInProgress) {
                editInProgress = true;
                getActivity().invalidateOptionsMenu();
            }
        } else {
            editInProgress = false;
            getActivity().invalidateOptionsMenu();
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.place_correction, menu);

        MenuItem done = menu.findItem(R.id.action_place_correction_done);
        if (done != null) {
            if (editInProgress) {
                done.setEnabled(true);
            } else {
                done.setEnabled(false);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch(item.getItemId()) {
        case R.id.action_place_correction_done:
            editInProgress = false;
            onDoneMenuActionSelected();
            break;
        case R.id.action_place_correction_cancel:
            final Activity activity = getActivity();
            if (activity == null || activity.isFinishing()) {
                return true;
            }
            Toast.makeText(activity, placeCorrectionCancelledString, Toast.LENGTH_SHORT).show();
            finish(activity);
            break;
        }
        return true;
    }


    /**
     * Clean up members and finish the {@code Activity.}
     * @param activity 
     */
    private void finish(final Activity activity) {

        editInProgress = false;
        selectedPlaceFromUserStay = null;

        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.finish();
    }
}
