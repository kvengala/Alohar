/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.alohar.sample.ASPlaceEvent.EventType;

public class ASEventsFragment extends Fragment {

    private EventAdapter adapter = new EventAdapter();
    private CopyOnWriteArrayList<ASPlaceEvent> events = new CopyOnWriteArrayList<ASPlaceEvent>();
    private ASEventsManager.ASEventsListener asEventsListener = new ASEventsManager.ASEventsListener() {
        @Override
        public void onEventsUpdated(CopyOnWriteArrayList<ASPlaceEvent> events) {
            ASEventsFragment.this.events = events;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };

    private String unknownEventString;

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unknownEventString = getString(R.string.realtime_event_unknown);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_events, container, false);
        Switch notificationSwitch = (Switch) root.findViewById(R.id.notification_switch);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        notificationSwitch.setChecked(preferences.getBoolean(ASConstant.PREF_KEY_NOTIFICATION_SWITCH, true));
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean(ASConstant.PREF_KEY_NOTIFICATION_SWITCH, isChecked)
                        .apply();
            }
        });

        ListView listView = (ListView) root.findViewById(R.id.events_listview);
        listView.setAdapter(adapter);
        return root;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        ASEventsManager.getInstance().setADEventsListener(asEventsListener);
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        ASEventsManager.getInstance().setADEventsListener(null);
    }

    class EventAdapter extends BaseAdapter {

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            return events.size();
        }

        /*
         * (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return events.get(position);
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_event, null);
            }
            ASPlaceEvent e = (ASPlaceEvent)getItem(position);
            ((TextView)convertView.findViewById(R.id.type)).setText("type: " + e.getType().toString());
            ((TextView)convertView.findViewById(R.id.time)).setText("time: " + ASUtility.getDateTimeString(e.getTime()));
        	((TextView)convertView.findViewById(R.id.details)).setText(getDisplayText(e));
            return convertView;
        }
        
        private String getDisplayText(ASPlaceEvent e) {
            if (e.getType() == EventType.USERSTAY) {
                return ASUtility.getUserStayString(e.getUserStay());
            } else if (e.getType() == EventType.ARRIVAL || e.getType() == EventType.DEPARTURE) {
                return  "coordinate: " + ASUtility.getLatLngString(e.getLatitude(), e.getLongitude());
            } else {
            	return unknownEventString;
            }
        }
    }
}
