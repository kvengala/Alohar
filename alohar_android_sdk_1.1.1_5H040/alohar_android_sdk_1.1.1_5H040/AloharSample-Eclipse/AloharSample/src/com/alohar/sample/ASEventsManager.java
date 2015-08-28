/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.alohar.context.api.AcxLocationManager.AcxPotentialUserStayCallback;
import com.alohar.context.api.AcxUserStayManager.AcxUserStayCallback;
import com.alohar.context.api.model.AcxUserStay;

public class ASEventsManager implements AcxPotentialUserStayCallback, AcxUserStayCallback {
    public interface ASEventsListener {
        void onEventsUpdated(CopyOnWriteArrayList<ASPlaceEvent> events);
    }

    private static ASEventsManager instance;
    private final Context context;
    private static final int MAX_NUM_EVENTS = 200;
    private ASEventsListener asEventsListener;
    private SharedPreferences prefs;
    private CopyOnWriteArrayList<ASPlaceEvent> events = new CopyOnWriteArrayList<ASPlaceEvent>();
    private final String contentTitle;

    private ASEventsManager(Context ctx) {
        context = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        contentTitle = context.getString(R.string.app_name);
        loadEvents();
    }

    public static synchronized ASEventsManager init(Context ctx){
        if (instance == null) {
            instance = new ASEventsManager(ctx);
        }
        return instance;
    }

    public static synchronized ASEventsManager getInstance(){
        return instance;
    }

    public void setADEventsListener(ASEventsListener asEventsListener) {
        this.asEventsListener = asEventsListener;
        if (asEventsListener != null) {
            asEventsListener.onEventsUpdated(events);
        }
    }

    public void clearEvents() {
        events.clear();
        saveEvents();
    }

    @Override
    public void onArrival(double latitude, double longitude) {
        long time = System.currentTimeMillis();
        ASPlaceEvent newEvent = ASPlaceEvent.createArrivalEvent(time, latitude, longitude);

        events.add(0, newEvent);
        saveEvents();
        if (asEventsListener != null) {
            asEventsListener.onEventsUpdated(events);
        }
        showNotification(String.format("ARRIVAL: %6f, %6f", latitude, longitude));
    }

    @Override
    public void onDeparture(double latitude, double longitude) {
        long time = System.currentTimeMillis();
        ASPlaceEvent newEvent = ASPlaceEvent.createDepartureEvent(time, latitude, longitude);

        events.add(0, newEvent);
        saveEvents();
        if (asEventsListener != null) {
            asEventsListener.onEventsUpdated(events);
        }
        showNotification(String.format("DEPARTURE: %6f, %6f", latitude, longitude));
    }

    @Override
    public void onUserStayUpdate(AcxUserStay userStay) {
        long time = System.currentTimeMillis();
        ASPlaceEvent newEvent = ASPlaceEvent.createUserStayUpdateEvent(time, userStay);
        
        events.add(0, newEvent);
        saveEvents();

        if (asEventsListener != null) {
            asEventsListener.onEventsUpdated(events);
        }

        String message = "";
        if (userStay.getSelectedPlace() != null) {
            message = userStay.getSelectedPlace().getName();
        } else if (!userStay.getTopCandidatePlaceList().isEmpty()) {
            message = userStay.getTopCandidatePlaceList().get(0).getName();
        }

        showNotification("USERSTAY: " + message);
    }

    private void saveEvents() {

        Log.d("Event", "[event] Save events.");

        while (events.size() > MAX_NUM_EVENTS) {
            events.remove(MAX_NUM_EVENTS - 1);
        }
        try {
            JSONArray jArr = new JSONArray();
            for (ASPlaceEvent event : events) {
                jArr.put(event.toJson());
            }
            prefs.edit().putString(ASConstant.PREF_KEY_EVENTS, jArr.toString()).apply();
        } catch (JSONException e) {
            Log.e("Event", "[event] Error: " + e.getMessage());
        }
    }

    private void loadEvents() {

        Log.d("Event", "[event] Load events.");

        String eventStr = prefs.getString(ASConstant.PREF_KEY_EVENTS, null);
        if (eventStr != null) {        	
            try {
                JSONArray jArr = new JSONArray(eventStr);
                events.clear();
                for (int i=0; i<jArr.length(); i++) {
                    JSONObject jObj = jArr.getJSONObject(i);
                    events.add(ASPlaceEvent.fromJson(jObj));
                }
            } catch (JSONException e) {
                Log.e("Event", "[event] Error: " + e.getMessage());
                prefs.edit().remove(ASConstant.PREF_KEY_EVENTS).commit();
            }
        }
    }

    private void showNotification(String msg) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!preferences.getBoolean(ASConstant.PREF_KEY_NOTIFICATION_SWITCH, true)) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, ASMainActivity.class), 0);


        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        int notificationIcon = whiteIcon ? R.drawable.ic_stat_android5 : R.drawable.ic_stat_android4;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(notificationIcon)
                .setContentTitle(contentTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setTicker(msg)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
