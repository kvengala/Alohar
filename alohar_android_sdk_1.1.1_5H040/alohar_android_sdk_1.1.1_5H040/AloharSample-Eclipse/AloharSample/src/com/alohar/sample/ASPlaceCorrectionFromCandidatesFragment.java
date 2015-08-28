/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alohar.context.api.AcxServiceManager;
import com.alohar.context.api.AcxServiceManager.AcxServerCallback;
import com.alohar.context.api.AcxUserStayManager;
import com.alohar.context.api.model.AcxError;
import com.alohar.context.api.model.AcxPlace;
import com.alohar.context.api.model.AcxUserStay;


/**
 * {@code ASPlaceCorrectionFromCandidatesFragment} class demonstrates the usage of the following APIs:
 * 1. get candidates list of places for a given stay.
 * 2. correct the place corresponding to a selected stay using a place from the candidates list
 */
public class ASPlaceCorrectionFromCandidatesFragment extends Fragment {

    private static final String TAG = ASPlaceCorrectionFromCandidatesFragment.class.getSimpleName();

    private ASPlaceCandidatesListAdapter placeCandidatesListAdapter;
    private TextView statusTextView;

    private List<AcxPlace> places = new ArrayList<AcxPlace>();
    private AcxPlace selectedPlaceFromUserStay;
    private AcxPlace selectedPlaceFromCandidates;

    private boolean editInProgress = false;
    private long stayId;

    private AcxUserStayManager userStayManager;

    private String placeCorrectionSuccessString;
    private String placeCorrectionFailedString;
    private String numberOfPlaceCandidatesString;

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

        View root = inflater.inflate(R.layout.fragment_place_correction_from_candidates, container, false);

        statusTextView = (TextView) root.findViewById(R.id.place_candidates_title);

        // Candidates ListView of the selected place.
        ListView placeCandidatesList = (ListView) root.findViewById(R.id.place_candidates_list);
        placeCandidatesListAdapter = new ASPlaceCandidatesListAdapter(getActivity());
        placeCandidatesList.setAdapter(placeCandidatesListAdapter);
        placeCandidatesList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                view.setSelected(true);
                onCandidatesListSelected(position);
            }
        });

        placeCorrectionSuccessString = getString(R.string.place_correction_success);
        placeCorrectionFailedString = getString(R.string.place_correction_failed);
        numberOfPlaceCandidatesString = getString(R.string.number_of_place_candidates);

        Object obj = getActivity().getIntent()
                .getParcelableExtra(ASStaysListFragment.INTENT_EXTRA_KEY_USERSTAY);

        if (obj instanceof AcxUserStay) {
            // Set the name and address of the place selected from Stay
            AcxUserStay userStay = (AcxUserStay) obj;
            Log.d("DEBUG", "got user stay :" + userStay.toString());

            if (userStay != null) {
                stayId = userStay.getId();
                selectedPlaceFromUserStay = userStay.getSelectedPlace();

                // Call helper function that showcases the usage of the place candidates API
                placeCandidates(userStay);
            }
        } else {
            Log.d("DEBUG", "no user stays.");
        }
        enableDoneMenu(false);
 
        return root;
    }

    /**
     * Helper function to showcase the use of Alohar SDK for fetching the
     * candidates list of places {@code ALPlace} corresponding to an user stay
     * {@code ALUserStay}.
     * Candidates List provides a list of nearby POI (Point of Interest) corresponding
     * to the POI in the user stay.
     * This list of POI candidates can be used by the end-user to correct the location
     * of a particular stay.
     *
     * @param userStay {@code ALUserStay} input parameter. The stay id of this stay is used to
     * generate the list of potential POI candidates list.
     */
    private void placeCandidates(AcxUserStay userStay) {
        final Activity activity = getActivity();

        userStayManager.fetchUserStayFullCandidatePlaceList(userStay.getId(),
                new AcxServerCallback<List<AcxPlace>>(){

            @Override
            public void onSuccess(List<AcxPlace> result) {

                places.clear();
                places.addAll(result);

                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        statusTextView.setText(String.format(numberOfPlaceCandidatesString, places.size()));
                        placeCandidatesListAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(AcxError error) {

                final String message = "Place Candidates Error : " + error.toString();
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        statusTextView.setText(message);
                    }
                });
            }

        });
    }

    /**
     * Helper function to showcase the usage of Alohar SDK API for correcting a stay
     * with a place selected from the candidates' list of nearby places.
     * 
     * Stay correction API requires the following input parameters:
     *     Stay ID: of the user stay {@code ALUserStay} object that was selected for correction.
     *     POI ID: of the place {@code ALPlace} object selected from the candidates list.
     *     Owner Alohar Uid: UID of the registered owner.
     *     Response listener: Callback listener for the handle the response of the asynchronous
     *         call for place correction.
     *
     * @param placeForCorrection The {@code ALPlace} object that has been chosen from
     * the candidates list of nearby places.
     */
    private void placeCorrectionForPoi(AcxPlace placeForCorrection) {
        Log.d(TAG, "placeCorrectionForPoi(): " + placeForCorrection);

        final Activity activity = getActivity();

        userStayManager.correctUserStay(stayId, placeForCorrection.getId(), 
                new AcxServerCallback<AcxUserStay>() {

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
     * Helper function to decide whether 2 {@code ALPlace} objects refer to the same place
     * and are identical.
     *
     * @param placeA A {@code ALPlace} object
     * @param placeB A {@code ALPlace} object
     * 
     * @return true if both place objects are same same based on
     * fields' comparing criteria, else returns false.
     */
    private boolean isSamePoi(AcxPlace placeA, AcxPlace placeB) {
        return placeA.getId() == placeB.getId()
                && placeA.getName() != null && placeA.getName().equals(placeB.getName())
                && placeA.getAddress().equals(placeB.getAddress())
                && placeA.getLatE6() == placeB.getLatE6()
                && placeA.getLonE6() == placeB.getLonE6();
    }

    /**
     * Handler function to process the selection of the place to be corrected with
     * from the candidates' list of places near the stay.
     * 
     * EditText boxes are updated with the name and address of the place chosen.
     * The {ALPlace} object selected from candidates list is saved to be used when user
     * confirms the correction via "Done" menu button.
     */
    private void onCandidatesListSelected(int position) {
        selectedPlaceFromCandidates = places.get(position);
        if (selectedPlaceFromUserStay != null && isSamePoi(
                selectedPlaceFromCandidates, selectedPlaceFromUserStay)) {
            enableDoneMenu(false);
            Toast.makeText(getActivity(), getString(R.string.place_correction_futile_same_place),
                    Toast.LENGTH_LONG).show();
        } else {
            enableDoneMenu(true);
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
            Toast.makeText(activity, getString(R.string.place_correction_cancelled),
                    Toast.LENGTH_SHORT).show();
            finish(activity);
            break;
        }
        return true;
    }

    /**
     * Handler function for the "Done" menu action confirming place correction 
     * - on selection from candidates list
     */
    private void onDoneMenuActionSelected() {
        if (selectedPlaceFromCandidates != null) {
            // Call helper function that showcases the usage of the API for
            // correcting a stay with a place selected from candidates list of nearby places.
            placeCorrectionForPoi(selectedPlaceFromCandidates);
        }
    }

    /**
     * {@code ASPlaceCandidatesListAdapter} class to display the list of candidates
     * i.e list of {@code ALPlace} data in a list view.
     */
    class ASPlaceCandidatesListAdapter extends BaseAdapter {

        private Context context;

        /*
         * Class to help optimize the reuse of list items.
         */
        private class PlaceViewHolder {
            public TextView placeDetails;
        }

        public ASPlaceCandidatesListAdapter(Context c) {
            context = c;
        }

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            return places.size();
        }

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return places.get(position);
        }

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.row_place, null);
                PlaceViewHolder stayviewHolder = getPlaceItemViewHolder(convertView);
                convertView.setTag(stayviewHolder);
            }

            PlaceViewHolder pViewHolder = null;
            AcxPlace place = (AcxPlace) getItem(position);

            if (place != null) {
                pViewHolder = (PlaceViewHolder) convertView.getTag();
                pViewHolder.placeDetails.setText(ASUtility.getPlaceString(place));
            }

            return convertView;
        }
        
        /*
         * Helper method to generate the {@code PlaceViewHolder} object
         * required in the {@code ASPlaceCandidatesListAdapter}'s getView() method.
         */
        private PlaceViewHolder getPlaceItemViewHolder(View rowView) {
            PlaceViewHolder viewHolder = new PlaceViewHolder();
            viewHolder.placeDetails = (TextView) rowView.findViewById(R.id.place_details);
            return viewHolder;
        }
    }

    /**
     * Clean up members and finish the {@code Activity.}
     */
    private void finish(final Activity activity) {

        editInProgress = false;
        selectedPlaceFromUserStay = null;
        selectedPlaceFromCandidates = null;

        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.finish();
    }

}
