/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

/**
 * Displays a date picker dialog.
 */
public class ASDatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private ArrayList<ASDatePickerDateSetListener> listeners =
            new ArrayList<ASDatePickerDateSetListener>();

    /*
     * {@code interface} to communicate the date picked to the Activity/Fragment that
     * owns the DialogFragment.
     */
    public interface ASDatePickerDateSetListener {
        void onADDateSelected(DatePicker view, int year, int month, int day);
    }

    /*
     * (non-Javadoc)
     * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    /*
     * (non-Javadoc)
     * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget.DatePicker, int, int, int)
     */
    @Override
    public void onDateSet(final DatePicker view, final int year, final int month, final int day) {
        for (ASDatePickerDateSetListener listener: listeners) {
            listener.onADDateSelected(view, year, month, day);
        }
    }

    /**
     * Registers listener for date picker action/selection.
     *
     * @param datePickerDateSetListener The {@code ASDatePickerDateSetListener} object to register.
     */
    public void registerADDatePickerDateSetListener(ASDatePickerDateSetListener datePickerDateSetListener) {
        listeners.add(datePickerDateSetListener);
    }

    /**
     * Unregisters listener for date picker action/selection.
     *
     * @param listener The {@code ASDatePickerDateSetListener} object to unregister.
     */
    public void unregisterADDatePickerDateSetListener(ASDatePickerDateSetListener listener) {
        listeners.remove(listener);
    }
}