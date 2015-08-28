/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alohar.context.api.AcxServiceManager;
import com.alohar.context.api.AcxServiceManager.AcxServerCallback;
import com.alohar.context.api.AcxUserStayManager;
import com.alohar.context.api.model.AcxError;
import com.alohar.context.api.model.AcxUserStay;
import com.alohar.sample.ASDatePickerFragment.ASDatePickerDateSetListener;

/**
 * {@code ASStaysListFragment} implements the usage of Alohar SDK API that
 * provides a list of user stays based on POI's visited by the user within a
 * specified time interval.
 */
public class ASStaysListFragment extends Fragment {

    protected static final String TAG = ASStaysListFragment.class.getSimpleName();

    private Activity activity;
    private ListView stayListView;
    private ASStaysListAdapter adapter;

    private AcxUserStayManager userStayManager;
    private final List<AcxUserStay> userStays = new ArrayList<AcxUserStay>();
    
    private TextView statusTextView;
    private Button startDatePickerButton;
    private Button endDatePickerButton;

    private long endTimeMillis;
    private long startTimeMillis;

    private int itemClickedPosition = -1;

    private String numberOfUserstaysString;
    private String userstaysErrorString;

    public static final String INTENT_EXTRA_KEY_USERSTAY = "UserStay";

    private final OnClickListener datePickerListener = new OnClickListener() {

        /*
         * (non-Javadoc)
         * @see android.view.View.OnClickListener#onClick(android.view.View)
         */
        @Override
        public void onClick(View v) {
            showDatePickerDialog(v);
        }
    };

    private OnItemClickListener userStayClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position,
                long id) {
            itemClickedPosition = position;
            registerForContextMenu(stayListView);
            getActivity().openContextMenu(stayListView);
        }
    };

    /* (non-Javadoc)
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_stays, container, false);
        activity = getActivity();

        numberOfUserstaysString = activity.getString(R.string.number_of_userstays);
        userstaysErrorString = activity.getString(R.string.userstay_error);

        statusTextView = (TextView)root.findViewById(R.id.stays_title);
        statusTextView.setVisibility(View.GONE);

        startDatePickerButton = (Button) root.findViewById(R.id.stayslist_start_date_picker);
        startDatePickerButton.setOnClickListener(datePickerListener);

        endDatePickerButton = (Button) root.findViewById(R.id.stayslist_end_date_picker);
        endDatePickerButton.setOnClickListener(datePickerListener);

        stayListView = (ListView)root.findViewById(R.id.stays_listview);
        adapter = new ASStaysListAdapter(activity);
        stayListView.setAdapter(adapter);
        stayListView.setOnItemClickListener(userStayClickListener);
        registerForContextMenu(stayListView);

        // Initialize the {@code ALPlaceManager} member object to be able to call the user stays api.
        userStayManager = AcxServiceManager.getInstance().getUserStayManager();

        return root;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        View header = View.inflate(getActivity(),R.layout.context_menu_header, null);
        TextView title = (TextView) header.findViewById(R.id.context_menu_title);
        title.setText(getString(R.string.userstay_context_menu_title));
        menu.setHeaderView(header);

        if (v.getId() == R.id.stays_listview) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.userstay_actions, menu);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = -1;
        if (info == null) {
            position = itemClickedPosition;
        } else {
            position = info.position;
        }
        Log.d(TAG, "onContextItemSelected(): " + position + ", menuItem: " + item.getTitle());
        int itemId = item.getItemId();
        if (itemId == R.id.action_stay_correction) {
            AcxUserStay userStay = userStays.get(userStays.size() - position - 1);
            if (userStay != null) {
                Intent intent = new Intent(activity, ASPlaceCorrectionDialogActivity.class);
                Log.d(TAG, "put user stay: " + userStay.toString());
                intent.putExtra(INTENT_EXTRA_KEY_USERSTAY, userStay);
                startActivity(intent);
            }
            return true;
        } else if (itemId == R.id.action_stay_deletion) {
            AcxUserStay userStay = userStays.get(userStays.size() - position - 1);
            if (userStay != null) {
                Intent intent = new Intent(activity, ASUserStayDeletionDialogActivity.class);
                Log.d(TAG, "put user stay: " + userStay.toString());
                intent.putExtra(INTENT_EXTRA_KEY_USERSTAY, userStay);
                startActivity(intent);
            }
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

        // Fetch stays for last 7 days by default.
        long currentTime = System.currentTimeMillis();
        long pastDayTime = currentTime - (7 * 24 * 60 * 60 * 1000);
        startTimeMillis = ASUtility.getStartOfTheDayInMillis(pastDayTime);
        endTimeMillis = ASUtility.getEndOfTheDayInMillis(currentTime);
        startDatePickerButton.setText(ASUtility.getDateTimeString(startTimeMillis));
        endDatePickerButton.setText(ASUtility.getDateTimeString(endTimeMillis));

        // Call helper function that showcases the usage of the user stays API.
        fetchUserStays(pastDayTime, currentTime);
    }

    /**
     * Helper function to showcase the use of Alohar SDK for fetching the
     * user stays of the current user during a particular time range.
     *
     * @param startTimeMillis Time in millisecs for the start of the time range 
     * @param endTimeMillis Time in millisecs for the end of the time range 
     */
    public void fetchUserStays(final long startTimeMillis, final long endTimeMillis) {

        userStayManager.fetchUserStayList(startTimeMillis, endTimeMillis, 
                new AcxServerCallback<List<AcxUserStay>>(){

            @Override
            public void onSuccess(List<AcxUserStay> result) {
                userStays.clear();
                userStays.addAll(result);
                // Reverse the stays list to be able to display
                // the stays in reverse chronological way.
                Collections.reverse(userStays);

                final String message = String.format(numberOfUserstaysString, userStays.size());

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        statusTextView.setVisibility(View.VISIBLE);
                        statusTextView.setText(message);
                    }
                });
            }

            @Override
            public void onError(AcxError error) {

                final String message = String.format(userstaysErrorString, error.toString());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusTextView.setVisibility(View.VISIBLE);
                        statusTextView.setText(message);
                    }
                });
            }

        });
    }

    /*
     * {@code ASStaysListAdapter} extends {@code BaseAdapter} for
     * the adapter object for {@code ListView} used to display
     * the list of user stays {@code ALUserStay}
     */
    private class ASStaysListAdapter extends BaseAdapter {

        Context context;

        /*
         * Class to help optimize the reuse of list items.
         */
        private class StayViewHolder {
            public TextView stayDetails;
        }

        public ASStaysListAdapter(Context c) {
            context = c;
        }

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
        	return userStays.size();
        }

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return userStays.get(userStays.size() - position - 1);
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
                convertView = LayoutInflater.from(context).inflate(R.layout.row_userstay, null);
                StayViewHolder stayviewHolder = getStayItemViewHolder(convertView);
                convertView.setTag(stayviewHolder);
            }

            AcxUserStay userStay = (AcxUserStay) getItem(position);
            if (userStay != null) {

                StayViewHolder sViewHolder = (StayViewHolder) convertView.getTag();

                sViewHolder.stayDetails.setText(ASUtility.getUserStayString(userStay));
            }

            return convertView;
        }

        /*
         * Helper method to generate the {@code StayViewHolder} object
         * required in the {@code ASStaysListAdapter}'s getView() method.
         */
        private StayViewHolder getStayItemViewHolder(View rowView) {
            StayViewHolder viewHolder = new StayViewHolder();
            viewHolder.stayDetails = (TextView) rowView.findViewById(R.id.stay_details);
            return viewHolder;
        }
    }


    /*
     * Method to show the {@code DialogFragment} for date picker
     * to show the start date picker and end date picker.
     */
    public void showDatePickerDialog(final View datePickerView) {
        final ASDatePickerFragment newFragment;
        switch (datePickerView.getId()) {
        case R.id.stayslist_start_date_picker:
            newFragment = new ASDatePickerFragment();
            ((ASDatePickerFragment)newFragment).registerADDatePickerDateSetListener(new ASDatePickerDateSetListener() {

                public void onADDateSelected(DatePicker view, int year, int month,
                        int day) {
                    ((ASDatePickerFragment)newFragment).unregisterADDatePickerDateSetListener(this);
                    long currentStartTimeMillis = ASUtility.getStartOfTheDayInMillis(year, month, day);
                    if (currentStartTimeMillis < endTimeMillis) {
                        startTimeMillis = currentStartTimeMillis;
                        startDatePickerButton.setText(ASUtility.getDateTimeString(startTimeMillis));
                        // Call helper function that showcases the usage of the user stays API.
                        fetchUserStays(startTimeMillis, endTimeMillis);
                    } else {
                        Toast.makeText(activity, "Error: Start Time in future. Select valid Start Time in past." , Toast.LENGTH_LONG).show();
                    }
                }
            });
            newFragment.show(activity.getFragmentManager(), "datePickerStartDate");
            break;
        case R.id.stayslist_end_date_picker:
            newFragment = new ASDatePickerFragment();
            ((ASDatePickerFragment)newFragment).registerADDatePickerDateSetListener(new ASDatePickerDateSetListener() {

                @Override
                public void onADDateSelected(DatePicker view, int year, int month,
                        int day) {
                    ((ASDatePickerFragment)newFragment).unregisterADDatePickerDateSetListener(this);
                    long currentEndTimeInMillis = ASUtility.getEndOfTheDayInMillis(year, month, day);
                    if (currentEndTimeInMillis > startTimeMillis) {
                        endTimeMillis = currentEndTimeInMillis;
                        endDatePickerButton.setText(ASUtility.getDateTimeString(endTimeMillis));
                        // Call helper function that showcases the usage of the user stays API.
                        fetchUserStays(startTimeMillis, endTimeMillis);
                    } else {
                        Toast.makeText(activity, "Error: End Time should be later than Start Time." , Toast.LENGTH_LONG).show();
                    }
                }
            });
            newFragment.show(activity.getFragmentManager(), "datePickerEndDate");
            break;
        }
    }

}
