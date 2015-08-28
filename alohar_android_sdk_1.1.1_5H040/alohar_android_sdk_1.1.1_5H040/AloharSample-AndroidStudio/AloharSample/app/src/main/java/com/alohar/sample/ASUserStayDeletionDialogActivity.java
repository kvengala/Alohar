/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import com.alohar.context.api.AcxServiceManager;
import com.alohar.context.api.AcxUserStayManager;
import com.alohar.context.api.AcxServiceManager.AcxServerCallback;
import com.alohar.context.api.model.AcxError;
import com.alohar.context.api.model.AcxUserStay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * {@code ASUserStayDeletionDialogActivity} class demonstrates the usage of the following API:
 * 1. Delete a UserStay.
 */
public class ASUserStayDeletionDialogActivity extends Activity {

    private final static String TAG = ASUserStayDeletionDialogActivity.class.getSimpleName();

    private FragmentManager fragmentManager;
    private UserStayDeletionProgressDialogFragment userStayDeletionProgressDialog;
    private TextView userStayDeletionErrorText;

    private static String stayDeletionConfirmTitleString;
    private static String stayDeletionConfirmMessageString;
    private static String actionDialogYesString;
    private static String actionDialogNoString;
    private static String stayDeletionInProgressString;

    private String stayDeletionSuccessMessageString;
    private String stayDeletionErrorNotUserstayString;
    private String stayDeletionCancelledString;


    /**
     * {@code UserStayDeletionConfirmationDialogFragment} implements the dialog that
     * provides the confirmation dialog for proceeding with the deletion of the user stay.
     */
    public static class UserStayDeletionConfirmDialogFragment extends DialogFragment {

        private ASUserStayDeletionDialogActivity activity;

        public void setActivity(ASUserStayDeletionDialogActivity activity) {
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
            title.setText(stayDeletionConfirmTitleString);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCustomTitle(header)
                    .setMessage(stayDeletionConfirmMessageString)
                    .setCancelable(false)
                    .setPositiveButton(actionDialogYesString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Call helper function that showcases the usage of the UserStay deletion API.
                            activity.onUserStayDeletionConfirmed();
                        }
                    })
                    .setNegativeButton(actionDialogNoString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            activity.onUserStayDeletionCancelled();
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
           activity.onUserStayDeletionCancelled();
        }

    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userstay_deletion_dialog);

        stayDeletionConfirmTitleString = getString(R.string.userstay_deletion_confirmation_title);
        stayDeletionConfirmMessageString = getString(R.string.userstay_deletion_confirmation_message);
        actionDialogYesString = getString(R.string.action_dialog_yes);
        actionDialogNoString = getString(R.string.action_dialog_no);
        stayDeletionSuccessMessageString = getString(R.string.userstay_deletion_success_msg);
        stayDeletionErrorNotUserstayString = getString(R.string.userstay_deletion_not_userstay_object);
        stayDeletionCancelledString = getString(R.string.userstay_deletion_cancelled);
        stayDeletionInProgressString = getString(R.string.userstay_deletion_in_progress);

        fragmentManager = getFragmentManager();

        UserStayDeletionConfirmDialogFragment userstayDeletionConfirmDialog = new UserStayDeletionConfirmDialogFragment();
        userstayDeletionConfirmDialog.setActivity(this);
        userstayDeletionConfirmDialog.show(fragmentManager, "UserStayDeletionConfirmDialog");

        userStayDeletionProgressDialog = new UserStayDeletionProgressDialogFragment();
        userStayDeletionErrorText = (TextView)findViewById(R.id.userstay_deletion_error_text);
        
    }

    /**
     * Helper function to showcase the API for deletion of a UserStay.
     * {@code AcxUserStayManager}'s api {@code deleteUserStay()}
     */
    private void onUserStayDeletionConfirmed() {
        Object obj = getIntent()
                .getParcelableExtra(ASStaysListFragment.INTENT_EXTRA_KEY_USERSTAY);

        if (obj instanceof AcxUserStay) {
            // Show progress dialog while deletion is in progress
            userStayDeletionProgressDialog.show(fragmentManager, "UserStayDeletionProgressDialog");

            Log.d(TAG, "onUserStayDeletionConfirmed()");
            AcxUserStay userStayTobeDeleted = (AcxUserStay) obj;
            AcxUserStayManager userStayManager = AcxServiceManager.getInstance().getUserStayManager();
            userStayManager.deleteUserStay(userStayTobeDeleted.getId(), 
                    new AcxServerCallback<Void>(){

                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "UserStay Deletion Success");

                    // Dismiss the progress dialog.
                    userStayDeletionProgressDialog.dismiss();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ASUserStayDeletionDialogActivity.this,
                                    stayDeletionSuccessMessageString, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }

                @Override
                public void onError(AcxError error) {

                    // Dismiss the progress dialog.
                    userStayDeletionProgressDialog.dismiss();

                    final String message = error.getCode() + "::" + error.getMessage();
                    Log.e(TAG, "UserStay Deletion Error: " + message);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userStayDeletionErrorText.setVisibility(View.VISIBLE);
                            userStayDeletionErrorText.setText(message);
                        }
                    });
                }
            });
        } else {
            Log.e(TAG, "UserStay Deletion Error : Not a UserStay object, cannot delete.");
            userStayDeletionErrorText.setVisibility(View.VISIBLE);
            userStayDeletionErrorText.setText(stayDeletionErrorNotUserstayString);
        }
    }

    /**
     * Helper function to handle cancellation of the UserStay deletion action.
     */
    private void onUserStayDeletionCancelled() {
        Log.d(TAG, "onUserStayDeletionCancelled()");
        Toast.makeText(this, stayDeletionCancelledString, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * {@code UserStayDeletionProgressDialog} implements the dialog that
     * provides the confirmation dialog for proceeding with the deletion of the user stay.
     */
    public static class UserStayDeletionProgressDialogFragment extends DialogFragment {

        public UserStayDeletionProgressDialogFragment() {
            // use empty constructors.
        }

        /*
         * (non-Javadoc)
         * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
         */
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {

            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(stayDeletionInProgressString);
            dialog.setCancelable(false);  
            return dialog;
        }
    }

}
