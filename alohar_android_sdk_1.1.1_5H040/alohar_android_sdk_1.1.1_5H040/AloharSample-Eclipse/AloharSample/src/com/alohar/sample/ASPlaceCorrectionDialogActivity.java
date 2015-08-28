/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * {@code ASPlaceCorrectionDialogActivity} class demonstrates the usage of the following APIs:
 * 1. get candidates list of places for a given stay.
 * 2. correct the place corresponding to a selected stay using a place from the candidates list
 * 3. correct the place corresponding to a selected stay by manually editing the name and address of the place.
 */
public class ASPlaceCorrectionDialogActivity extends Activity {

    private final static String TAG = ASPlaceCorrectionDialogActivity.class.getSimpleName();

    private FragmentManager fragmentManager;

    /**
     * {@code PlaceCorrectionChoiceDialogFragment} implements the dialog that
     * provides the selectable options for place correction"
     *     - either  manually
     *     - or via selecting a place from a list of candidates near the current stay.
     */
    public static class PlaceCorrectionChoiceDialogFragment extends DialogFragment {

        private ASPlaceCorrectionDialogActivity activity;

        public PlaceCorrectionChoiceDialogFragment() {

        }

        public void setActivity(ASPlaceCorrectionDialogActivity activity) {
            this.activity = activity;
        }

        /*
         * (non-Javadoc)
         * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View header = View.inflate(getActivity(),R.layout.context_menu_header, null);
            TextView title = (TextView) header.findViewById(R.id.context_menu_title);
            title.setText(getString(R.string.place_correction_choose_method));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCustomTitle(header)
                   .setItems(R.array.place_correction_choice_array, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                           // The 'which' argument contains the index position
                           // of the selected item
                           Log.d(TAG, "onClick(): which: " + which);
                           String[] optionsArray = getActivity().getResources().getStringArray(
                                   R.array.place_correction_choice_array);
                           String whichOption = optionsArray[which];
                           if (whichOption.equals(getActivity().getString(R.string.place_correction_option_manual))) {
                               activity.onManualCorrectionSelected();
                           } else  if (whichOption.equals(getActivity().getString(R.string.place_correction_option_candidates))) {
                               activity.onCandidatesListCorrectionSelected();
                           }
                   }
            });
            return builder.create();
        }

        /*
         * (non-Javadoc)
         * @see android.app.DialogFragment#onCancel(android.content.DialogInterface)
         */
        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            activity.onPlaceCorrectionCancelled();
        }
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_correction_dialog);

        fragmentManager = getFragmentManager();

        PlaceCorrectionChoiceDialogFragment correctionChoiceDialog =
                new PlaceCorrectionChoiceDialogFragment();
        correctionChoiceDialog.setActivity(this);
        correctionChoiceDialog.show(fragmentManager, "PlaceCorrectionChoiceDialog");
    }

    /**
     * Helper function to launch the fragment that implements the flow
     * for manual correction of a place.
     */
    private void onManualCorrectionSelected() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ASPlaceCorrectionManualFragment fragment = new ASPlaceCorrectionManualFragment();
        fragmentTransaction.add(R.id.place_correction_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Helper function to launch the fragment that implements the flow
     * for correcting a place by selecting from a list of candidates.
     */
    private void onCandidatesListCorrectionSelected() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ASPlaceCorrectionFromCandidatesFragment fragment = new ASPlaceCorrectionFromCandidatesFragment();
        fragmentTransaction.add(R.id.place_correction_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Helper function to handle "Back" press or cancellation of the
     *  place correction option selection dialog.
     */
    private void onPlaceCorrectionCancelled() {
        finish();
    }

}
