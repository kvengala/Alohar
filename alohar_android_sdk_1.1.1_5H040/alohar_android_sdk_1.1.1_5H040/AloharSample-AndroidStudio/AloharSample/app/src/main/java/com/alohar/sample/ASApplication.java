/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alohar.context.api.AcxLocationManager;
import com.alohar.context.api.AcxServiceManager;
import com.alohar.context.api.AcxUserStayManager;

public class ASApplication extends Application {
    /* (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the Alohar SDK.
        AcxServiceManager.initialize(this, ASConfig.APP_ID, ASConfig.API_KEY);

        // Initialize Alohar Sample's Events Manager.
        ASEventsManager.init(this);

        AcxServiceManager acxManager = AcxServiceManager.getInstance();

        if (acxManager.isSignedIn()) {
            ASEventsManager asEventsManager = ASEventsManager.getInstance();
            AcxLocationManager locationManager = acxManager.getLocationManager();        
            locationManager.requestPotentialUserStayUpdates(asEventsManager);
            AcxUserStayManager userStayManager = acxManager.getUserStayManager();
            userStayManager.requestUserStayUpdates(asEventsManager);
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isWifiOnly = preferences.getBoolean(ASConstant.PREF_KEY_WIFI_ONLY, false);
            acxManager.setUploadContextDataOnlyOnWifi(isWifiOnly);
        }
    }
}
