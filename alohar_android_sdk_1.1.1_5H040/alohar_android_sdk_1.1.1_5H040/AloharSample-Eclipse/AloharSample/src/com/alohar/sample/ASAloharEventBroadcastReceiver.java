/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.alohar.context.api.AcxIntents;

/**
 * This {@code WakefulBroadcastReceiver} takes care of creating and managing a partial wake lock
 * for your app. It passes off the work of processing the intent to an {@code IntentService}, while
 * ensuring that the device does not go back to sleep in the transition. The {@code IntentService}
 * calls {@code completeWakefulIntent()} when it is ready to release the wake lock.
 */
public class ASAloharEventBroadcastReceiver extends WakefulBroadcastReceiver {

    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action.equals(AcxIntents.ACTION_CHECK_SYSTEM_SETTINGS) ||
                    action.equals(AcxIntents.ACTION_POTENTIAL_USERSTAY) ||
                    action.equals(AcxIntents.ACTION_USERSTAY)) { 

                // Explicitly specify the IntentService that will handle the intent.
                ComponentName comp = new ComponentName(context.getPackageName(),
                        ASAloharEventIntentService.class.getName());
                // Start the intent service, keeping the device awake while it is launching.
                startWakefulService(context, (intent.setComponent(comp)));
            }
        }
    }
}
