/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.alohar.context.api.AcxIntents;

/**
 * This {@code IntentService} does the actual handling of the intent received by the
 * {@code ASAloharEventBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) which holds a
 * partial wake lock for this service while the service does its work. When the service is
 * finished, it calls {@code completeWakefulIntent()} to release the wake lock.
 */
public class ASAloharEventIntentService extends IntentService {

    private static final String TAG = ASAloharEventIntentService.class.getSimpleName();

    private void showNotification(int id, String message) {
        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        int notificationIcon = whiteIcon ? R.drawable.ic_stat_android5 : R.drawable.ic_stat_android4;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
        .setSmallIcon(notificationIcon)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(message)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
        .setDefaults(Notification.DEFAULT_SOUND);
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notificationBuilder.build());
    }

    /* (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null) {
            Log.d(TAG, "[realtime] onHandleIntent: intent is null.");
            return;
        }

        final String action = intent.getAction();
        Log.d(TAG, "Alohar SDK intent action: " + intent.getAction());

        if (action != null && !action.isEmpty()) {

            final Bundle extras = intent.getExtras();

            if (action.equals(AcxIntents.ACTION_POTENTIAL_USERSTAY)) {
                handlePotentialUserStayIntent(extras);
            } else if (action.equals(AcxIntents.ACTION_USERSTAY)) {
                handleUserStayIntent(extras);
            } else if (action.equals(AcxIntents.ACTION_CHECK_SYSTEM_SETTINGS)) {
                handleSystemSettingsIntent(extras);
            } else {
                Log.d(TAG, "[realtime] unknown action: " + action);
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        ASAloharEventBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handlePotentialUserStayIntent(final Bundle extras) {
        if (extras == null) {
            Log.d(TAG, "[realtime] handle potential user stay : extras is null.");
            return;
        }

        final int eventType = extras.getInt(AcxIntents.EXTRA_POTENTIAL_USERSTAY_EVENT_TYPE);
        switch (eventType) {
        case AcxIntents.EXTRA_POTENTIAL_USERSTAY_EVENT_TYPE_ARRIVAL:
            Log.d(TAG, "[realtime] handle potential user stay : ARRIVAL.");
            break;
        case AcxIntents.EXTRA_POTENTIAL_USERSTAY_EVENT_TYPE_DEPARTURE:
            Log.d(TAG, "[realtime] handle potential user stay : DEPARTURE.");
            break;
        default:
            Log.d(TAG, "[realtime] handle potential user stay : UNKNOWN.");
        }
    }

    private void handleUserStayIntent(final Bundle extras) {
        if (extras == null) {
            Log.d(TAG, "[realtime] handle user stay : extras is null.");
            return;
        }

        final int eventType = extras.getInt(AcxIntents.EXTRA_USERSTAY_EVENT_TYPE);
        switch (eventType) {
        case AcxIntents.EXTRA_USERSTAY_EVENT_TYPE_UPDATE:
            break;
        default:
            Log.d(TAG, "[realtime] handle user stay : UNKNOWN.");
            break;
        }
    }

    private void handleSystemSettingsIntent(final Bundle extras) {
        if (extras == null) {
            Log.d(TAG, "[realtime] handle system settings : extras is null.");
            return;
        }
        final int settings = extras.getInt(AcxIntents.EXTRA_RECOMMENDED_SYSTEM_SETTINGS);
        switch (settings) {
        case AcxIntents.EXTRA_RECOMMENDED_SYSTEM_SETTINGS_ENABLE_GPS_AND_NETWORK:
            showNotification(0, getString(R.string.sdk_notify_lbs_off));
            break;
        case AcxIntents.EXTRA_RECOMMENDED_SYSTEM_SETTINGS_ENABLE_HIGH_ACCURACY:
            showNotification(0, getString(R.string.sdk_notify_lbs_off));
            break;
        case AcxIntents.EXTRA_RECOMMENDED_SYSTEM_SETTINGS_ENABLE_SCANNING_ALWAYS_AVAILABLE:
            showNotification(1, getString(R.string.sdk_notify_wifi_off));
            break;
        case AcxIntents.EXTRA_RECOMMENDED_SYSTEM_SETTINGS_ENABLE_WIFI:
            showNotification(1, getString(R.string.sdk_notify_wifi_off));
            break;
        }
    }

    public ASAloharEventIntentService() {
        super("ASAloharEventIntentService");
    }
}
