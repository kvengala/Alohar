/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.alohar.context.api.model.AcxUserStay;

public class ASPlaceEvent implements Serializable {

    private static final long serialVersionUID = -471044525324356520L;

    public enum EventType {
        ARRIVAL, DEPARTURE, USERSTAY
    }

    private final EventType type;
    private final long time;
    private final double latitude;
    private final double longitude;
    private final AcxUserStay userStay;

    public EventType getType() {
        return this.type;
    }

    public long getTime() {
        return this.time;
    }

    public AcxUserStay getUserStay() {
        return this.userStay;
    }

    public static ASPlaceEvent createArrivalEvent(long time, double latitude, double longitude) {
        return new ASPlaceEvent(EventType.ARRIVAL, time, latitude, longitude);
    }

    public static ASPlaceEvent createDepartureEvent(long time, double latitude, double longitude) {
        return new ASPlaceEvent(EventType.DEPARTURE, time, latitude, longitude);
    }

    public static ASPlaceEvent createUserStayUpdateEvent(long time, AcxUserStay userStay) {
        return new ASPlaceEvent(EventType.USERSTAY, time, userStay);
    }

    private ASPlaceEvent(EventType type, long time, double latitude, double longitude) {
        this.type = type;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userStay = null;
    }

    private ASPlaceEvent(EventType type, long time, AcxUserStay userStay) {
        this.type = type;
        this.time = time;
        this.latitude = 0;
        this.longitude = 0;
        this.userStay = userStay;
    }

    private ASPlaceEvent(EventType type, 
            long time, double latitude, double longitude, AcxUserStay userStay) {
        this.type = type;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userStay = userStay;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    private static final String JSON_KEY_TYPE = "type";
    private static final String JSON_KEY_TIME = "time";
    private static final String JSON_KEY_LATITUDE = "latitude";
    private static final String JSON_KEY_LONGITUDE = "longitude";
    private static final String JSON_KEY_USERSTAY = "userstay";

    public JSONObject toJson() throws JSONException {
        final JSONObject jObj = new JSONObject();
        jObj.put(JSON_KEY_TYPE, this.type.ordinal());
        jObj.put(JSON_KEY_TIME, this.time);
        jObj.put(JSON_KEY_LATITUDE, this.latitude);
        jObj.put(JSON_KEY_LONGITUDE, this.longitude);
        if (this.userStay != null) {
            jObj.put(JSON_KEY_USERSTAY, this.userStay.toJson());
        }
        return jObj;
    }

    public static ASPlaceEvent fromJson(JSONObject jObj) throws JSONException {
        EventType type = EventType.values()[jObj.getInt(JSON_KEY_TYPE)];
        long time = jObj.getLong(JSON_KEY_TIME);
        double latitude = jObj.getDouble(JSON_KEY_LATITUDE);
        double longitude = jObj.getDouble(JSON_KEY_LONGITUDE);
        JSONObject jObjUserStay = jObj.optJSONObject(JSON_KEY_USERSTAY);
        AcxUserStay userStay = (jObjUserStay != null) ? AcxUserStay.fromJson(jObjUserStay) : null;
        return new ASPlaceEvent(type, time, latitude, longitude, userStay);
    }
}
