/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.alohar.context.api.model.AcxPlace;
import com.alohar.context.api.model.AcxUserStay;
import com.alohar.context.api.model.AcxPlace.AcxSourceProperties;

public class ASUtility {

    public static final String ONETAB = "    ";
    public static final String THREETABS = "            ";
    public static final String NOT_FOUND = "null";
    public static final String SELECTED_PLACE_LABEL = "selectedPlace:";

    private static Date date = new Date();

    /**
     * Helper function to get date and time in formatted String from milliseconds.
     *
     * @param timeInMilliseconds Time in milliseconds
     * @return The formatted {@code String} corresponding to input param of time in milliseconds.
     */
    public static String getDateTimeString(long timeInMilliseconds) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy h:mm:ssa", Locale.US);
        date.setTime(timeInMilliseconds);
        return format.format(date);
    }

    /**
     * Given year, month and day of the month, returns time in milliseconds
     * for the start of the day.
     * 
     * @param year Year value
     * @param month Month value in number - 0 for January, 1 for February .. 11 for December
     * @param day Day of the month value (between 1 and 31)
     *
     * @return Time in milliseconds for start of day at 00.00 hours
     */
    public static long getStartOfTheDayInMillis(final int year,
            final int month, final int day) {
        Calendar cal = new GregorianCalendar(year, month, day);
        return startOfDayInMillis(cal);
    }

    /**
     * Given year, month and day of the month, returns time in milliseconds
     * for the end of the day.
     * 
     * @param year Year value
     * @param month Month value in number - 0 for January, 1 for February .. 11 for December
     * @param day Day of the month value (between 1 and 31)
     *
     * @return Time in milliseconds for end of day at 11.59 hours
     */
    public static long getEndOfTheDayInMillis(final int year,
            final int month, final int day) {
        Calendar cal = new GregorianCalendar(year, month, day);
        return endOfDayInMillis(cal);
    }

    /**
     * Given a time in millisecs, returns time in milliseconds
     * for the start of the day
     *
     * @param timeInMilliSecs Time value in millisecs.
     * 
     * @return Time in milliseconds for start of day at 00:00:00: hours.
     */
    public static long getStartOfTheDayInMillis(final long timeInMilliSecs) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timeInMilliSecs);
        return startOfDayInMillis(cal);
    }

    /**
     * Given a time in millisecs, returns time in milliseconds
     * for the end of the day
     *
     * @param timeInMilliSecs Time value in millisecs.
     * 
     * @return Time in milliseconds for start of day at 23:59:59:999 hours.
     */
    public static long getEndOfTheDayInMillis(final long timeInMilliSecs) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timeInMilliSecs);
        return endOfDayInMillis(cal);
    }

    /*
     * Helper function
     */
    private static long startOfDayInMillis(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /*
     * Helper function
     */
    private static long endOfDayInMillis(Calendar cal) {
        cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR)+1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis() - 1);
    }

    /**
     * Helper function to get a string containing the details of a UserStay.
     *
     * @param userStay the {@link AcxUserStay} object.
     *
     * @return a string containing the details of the UserStay.
     */
    public static String getUserStayString(AcxUserStay userStay) {

        final long stayId = userStay.getId();
        final long lastUpdateTimeInUtcMillis = userStay.getLastUpdateTimeInUtcMillis();
        final long stayStartTimeInUtcMillis = userStay.getStartTimeInUtcMillis();
        final long stayEndTimeInUtcMillis = userStay.isEnded() ? userStay.getEndTimeInUtcMillis() : -1;
        final double stayCentroidLat = userStay.getLatitude();
        final double stayCentroidLon = userStay.getLongitude();
        final AcxPlace selectedPlace = userStay.getSelectedPlace();
        final List<AcxPlace> topCandidatePlaceList = userStay.getTopCandidatePlaceList();

        String lastUpdateTimeStr = lastUpdateTimeInUtcMillis <= 0 ?
                NOT_FOUND : ASUtility.getDateTimeString(lastUpdateTimeInUtcMillis);
        
        String startDateStr = stayStartTimeInUtcMillis <= 0 ?
                NOT_FOUND : ASUtility.getDateTimeString(stayStartTimeInUtcMillis);
        
        String endDateStr = stayEndTimeInUtcMillis <= 0 ?
                NOT_FOUND : ASUtility.getDateTimeString(stayEndTimeInUtcMillis);

        StringBuilder sbUserStay = new StringBuilder("stayID: ");
        sbUserStay.append(stayId);
        sbUserStay.append("\ncentroidCoordinates: ");
        sbUserStay.append(getLatLngString(stayCentroidLat, stayCentroidLon));
        sbUserStay.append("\nstartTime: ");
        sbUserStay.append(startDateStr);
        sbUserStay.append("\nendTime: ");
        sbUserStay.append(endDateStr);
        sbUserStay.append("\nlastUpdateTime: ");
        sbUserStay.append(lastUpdateTimeStr);
        sbUserStay.append("\n");
        if (selectedPlace != null) {
            sbUserStay.append(getPlaceString(selectedPlace, SELECTED_PLACE_LABEL));
        } else {
            sbUserStay.append(SELECTED_PLACE_LABEL);
            sbUserStay.append(" null");
        }
        sbUserStay.append("\ntopCandidatePlaces:\n");
        if (topCandidatePlaceList != null && topCandidatePlaceList.size() > 0) {
            sbUserStay.append(ASUtility.getTopCandidatePlacesString(topCandidatePlaceList));
        } else {
            sbUserStay.append(NOT_FOUND);
        }

        return sbUserStay.toString();

    }

    public static String getLatLngString(double lat, double lng) {
        return String.format("(%.6f, %.6f)", lat, lng);
    }

    /**
     * Helper function to get a string containing place details with an optional place label.
     *
     * @param place the {@link AcxPlace} object.
     * @param placeLabel the optional label to be put in first line if not null.
     * @return a string containing place details with the optional place label.
     */
    public static String getPlaceString(AcxPlace place, String placeLabel) {
        final double placeLat = place.getLatitude();
        final double placeLon = place.getLongitude();
        AcxSourceProperties sourceProperties = place.getSourceProperties();

        StringBuilder sbPlace = new StringBuilder();
        if (placeLabel != null) {
            sbPlace.append(placeLabel);
            sbPlace.append("\n");
        }
        if (placeLabel != null) {
            sbPlace.append(ONETAB);
        }
        sbPlace.append("ID: ");
        sbPlace.append(place.getId());
        sbPlace.append("\n");
        if (placeLabel != null) {
            sbPlace.append(ONETAB);
        }
        sbPlace.append("name: ");
        sbPlace.append(place.getName());
        sbPlace.append("\n");
        if (placeLabel != null) {
            sbPlace.append(ONETAB);
        }
        sbPlace.append("address: ");
        sbPlace.append(place.getAddress());
        sbPlace.append("\n");
        if (placeLabel != null) {
            sbPlace.append(ONETAB);
        }
        sbPlace.append("coordinates: ");
        sbPlace.append(getLatLngString(placeLat, placeLon));
        sbPlace.append("\n");
        if (placeLabel != null) {
            sbPlace.append(ONETAB);
        }
        sbPlace.append("categories: ");
        sbPlace.append(TextUtils.join(", ", place.getCategoryList()));
        sbPlace.append("\n");
        if (placeLabel != null) {
            sbPlace.append(ONETAB);
        }
        sbPlace.append(getSourcePropertiesString(sourceProperties));
        return sbPlace.toString();
    }

    /**
     * Helper function to get a string containing place details.
     *
     * @param place the {@link AcxPlace} object used to create place details string.
     * @return a string containing place details.
     */
    public static String getPlaceString(AcxPlace place) {
        return getPlaceString(place, null);
    }

    /**
     * Helper function.
     *
     * @param sourceProperties the {AcxSourceProperties} object used to generate the String
     *        with the source property values.
     * @return the String concatenation of the source properties - source ID and place ID.
     */
    public static String getSourcePropertiesString(AcxSourceProperties sourceProperties) {
        StringBuilder sbSource = new StringBuilder("sourceProperties: ");
        if (sourceProperties == null) {
            sbSource.append(NOT_FOUND);
        } else {
            String sourceId = sourceProperties.getSourceId();
            String sourcePlaceId = sourceProperties.getPlaceId();
            if ((sourceId.isEmpty() || sourceId.equals("")) 
                    && (sourcePlaceId.isEmpty() || sourcePlaceId.equals("")) ) {
                sbSource.append(NOT_FOUND);
            } else {
                sbSource.append(sourceId);
                if (!sourcePlaceId.equals("")) {
                    sbSource.append(" , ");
                    sbSource.append(sourcePlaceId);
                }
            }
        }
        return sbSource.toString();
    }

    /**
     * Helper function
     *
     * @param topCandidatePlaceList the List of top candidate {@link AcxPlace}
     * @return the String concatenation of the top places name and source information.
     */
    public static String getTopCandidatePlacesString(List<AcxPlace> topCandidatePlaceList) {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (AcxPlace candidatePlace : topCandidatePlaceList) {
            sb.append(ONETAB);
            sb.append("(");
            sb.append(i);
            sb.append(") ");
            sb.append(candidatePlace.getName());
            sb.append("\n");
            sb.append(THREETABS);
            sb.append(ASUtility.getSourcePropertiesString(
                    candidatePlace.getSourceProperties()));
            i++;
            if (i < topCandidatePlaceList.size()) {
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }

}
