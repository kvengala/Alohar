/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.alohar.context.api.AcxPlaceManager;
import com.alohar.context.api.AcxServiceManager;
import com.alohar.context.api.AcxServiceManager.AcxServerCallback;
import com.alohar.context.api.model.AcxError;
import com.alohar.context.api.model.AcxImportantPlaceSuggestion;
import com.alohar.context.api.model.AcxImportantPlaceSuggestion.AcxImportantPlaceSuggestionState;
import com.alohar.context.api.model.AcxImportantPlaceSuggestion.AcxImportantPlaceSuggestionType;
import com.alohar.context.api.model.AcxPlace;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class ASImportantPlacesSuggestionsActivity extends Activity {
    public static final String TAG = ASImportantPlacesSuggestionsActivity.class.getSimpleName();

    /**
     * Helper class to create a UI specific data model object to show the important places
     * suggestion in groups of Home and Workplaces.
     */
    private class ASPlaceSuggestionListObject {
        final private String placeStatus;
        final private AcxImportantPlaceSuggestion placeSuggestion;
        
        public ASPlaceSuggestionListObject(String thisPlaceStatus, AcxImportantPlaceSuggestion thisPlaceSuggestion) {
            placeStatus = thisPlaceStatus;
            placeSuggestion = thisPlaceSuggestion;
        }

        public String getPlaceStatus() {
            return placeStatus;
        }

        public AcxImportantPlaceSuggestion getPlaceSuggestion() {
            return placeSuggestion;
        }

    }

    
    private static final int GROUPHEADER_INDEX_HOME = 0;
    private static final int GROUPHEADER_INDEX_WORKPLACES = 1;

    private TextView statusLine;
    // header titles
    private final ArrayList<String> listGroupHeaderData = new ArrayList<String>() ;
    // child data in format of header title, child title
    private final HashMap<String, List<ASPlaceSuggestionListObject>> listGroupChildData
            = new HashMap<String, List<ASPlaceSuggestionListObject>>();
    private ExpandableListView expListView;
    private ASPlacesSuggestionExpListAdapter adapter;
    private ASImportantPlacesSuggestionsActionsDialog placeSuggestionActionDialog;

    private String groupHeaderHomeString;
    private String groupHeaderWorkplaceString;
    private String placesSuggestionErrorString;

    private static String placesSuggestionActionEmptyString;

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_suggestions);

        groupHeaderHomeString = getString(R.string.places_suggestion_home);
        groupHeaderWorkplaceString = getString(R.string.places_suggestion_work);

        placesSuggestionErrorString = getString(R.string.places_suggestions_error);
        placesSuggestionActionEmptyString = getString(R.string.places_suggestion_action_empty_place);

        statusLine = (TextView) findViewById(R.id.places_suggestions_status);
        statusLine.setVisibility(View.GONE);
        
        final FragmentManager fragmentManager = getFragmentManager();

        placeSuggestionActionDialog = new ASImportantPlacesSuggestionsActionsDialog();
        placeSuggestionActionDialog.setActivity(this);
        
        expListView = (ExpandableListView)findViewById(R.id.places_suggestions_explistview);
        expListView.setGroupIndicator(null);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            public boolean onGroupClick(ExpandableListView arg0, View itemView,
                    int itemPosition, long itemId) {
                expListView.expandGroup(itemPosition);
                return true;
            }
        });
        expListView.setOnChildClickListener(new OnChildClickListener() {
            
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {
                
                ASPlaceSuggestionListObject placeSuggestionListObject = (ASPlaceSuggestionListObject) (listGroupChildData.get(
                        listGroupHeaderData.get(groupPosition)).get(childPosition));
                AcxImportantPlaceSuggestion placeSuggestionClicked = placeSuggestionListObject.getPlaceSuggestion();
                if (placeSuggestionClicked != null) {
                    placeSuggestionActionDialog.setPlaceSuggestion(placeSuggestionClicked);
                    placeSuggestionActionDialog.show(fragmentManager, "ASPlaceSuggestionActionDialog");
                }
                return true;
            }
        });
        adapter = new ASPlacesSuggestionExpListAdapter(this);
        expListView.setAdapter(adapter);
        
        fetchImportantPlaceSuggestionList();
    }

    /**
     * Helper function to showcase the usage of API
     * to fetch the list of important place suggestions
     */
    private void fetchImportantPlaceSuggestionList() {
        Log.d(TAG, "fetchImportantPlaceSuggestionList()");

        AcxPlaceManager placeManager = AcxServiceManager.getInstance().getPlaceManager();
        placeManager.fetchPendingImportantPlaceSuggestionList(new AcxServerCallback<List<AcxImportantPlaceSuggestion>>() {

            @Override
            public void onSuccess(List<AcxImportantPlaceSuggestion> placeSuggestionsList) {
                Log.d(TAG, "fetchPendingImportantPlaceSuggestionList(): Success, listsize: " + placeSuggestionsList.size());
                final List<AcxImportantPlaceSuggestion> homeSuggestionList
                        = new ArrayList<AcxImportantPlaceSuggestion>();
                final List<AcxImportantPlaceSuggestion> workSuggestionList
                        = new ArrayList<AcxImportantPlaceSuggestion>();
                for (AcxImportantPlaceSuggestion placeSuggestion : placeSuggestionsList) {
                    AcxImportantPlaceSuggestionType type = placeSuggestion.getType();
                    if (type.equals(AcxImportantPlaceSuggestionType.HOME)) {
                        // Add to homelist
                        homeSuggestionList.add(placeSuggestion);
                    } else if (type.equals(AcxImportantPlaceSuggestionType.WORKPLACE)) {
                        // Add to workPlacelist
                        workSuggestionList.add(placeSuggestion);
                    }
                }

                listGroupHeaderData.add(GROUPHEADER_INDEX_HOME, groupHeaderHomeString);
                listGroupHeaderData.add(GROUPHEADER_INDEX_WORKPLACES, groupHeaderWorkplaceString);

                final List<ASPlaceSuggestionListObject> homeSuggestionObjectsList = 
                        getSuggestionObjectsList(getString(R.string.places_suggestion_still_determining_home), homeSuggestionList);
                final List<ASPlaceSuggestionListObject> workPlacesSuggestionObjectsList = 
                        getSuggestionObjectsList(getString(R.string.places_suggestion_still_determining_work), workSuggestionList);
                listGroupChildData.clear();
                listGroupChildData.put(listGroupHeaderData.get(GROUPHEADER_INDEX_HOME), homeSuggestionObjectsList);
                listGroupChildData.put(listGroupHeaderData.get(GROUPHEADER_INDEX_WORKPLACES), workPlacesSuggestionObjectsList);
                // Now runOnUIThread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusLine.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            /*
             * (non-Javadoc)
             * @see com.alohar.context.api.AcxServiceManager.AcxServerCallback#onError(com.alohar.context.api.model.AcxError)
             */
            @Override
            public void onError(AcxError error) {
                final String errorStr = error.toString();
                Log.e(TAG, "fetchPendingImportantPlaceSuggestionList() Error: " + errorStr);
                final String message = String.format(placesSuggestionErrorString, error.toString());

                listGroupHeaderData.clear();
                listGroupChildData.clear();

                // Now runOnUIThread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusLine.setVisibility(View.VISIBLE);
                        statusLine.setText(message);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public static class ASImportantPlacesSuggestionsActionsDialog extends DialogFragment {
        
        AcxImportantPlaceSuggestion placeSuggestionForAction;
        TextView actionStatusView;
        ASImportantPlacesSuggestionsActivity activity;

        public void setActivity(ASImportantPlacesSuggestionsActivity activity) {
            this.activity = activity;
        }

        public void setPlaceSuggestion(AcxImportantPlaceSuggestion placeSuggestion) {
            placeSuggestionForAction = placeSuggestion;

        }
        
        /*
         * (non-Javadoc)
         * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (getShowsDialog()) {  // **The key check**
                return super.onCreateView(inflater, container, savedInstanceState);
            } else {
                View view = activity.getLayoutInflater().inflate(
                        R.layout.dialogfragment_places_suggestion_actions, null);
                return configureDialogView(view);
            }
        }

        /*
         * (non-Javadoc)
         * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            
            // Return custom dialog...
            Dialog dialog = super.onCreateDialog(savedInstanceState);

            View view = activity.getLayoutInflater().inflate(
                    R.layout.dialogfragment_places_suggestion_actions, null);
            configureDialogView(view);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(view);

            return dialog;
        }

        private View configureDialogView(View view) {
            actionStatusView = (TextView)view.findViewById(R.id.place_suggestion_action_status);
            actionStatusView.setVisibility(View.GONE);

            Button placeSuggestionAccepted = (Button)view.findViewById(R.id.place_suggestion_action_accept);
            Button placeSuggestionRejected = (Button)view.findViewById(R.id.place_suggestion_action_reject);

            if (placeSuggestionForAction != null) {
                final AcxPlace place = placeSuggestionForAction.getPlace();
                final EditText placeNameView = (EditText)view.findViewById(R.id.place_suggestion_action_placename);
                final TextView placeAddressView = (TextView)view.findViewById(R.id.place_suggestion_action_placeaddress);
                if (place != null) {
                    placeNameView.setText(place.getName());
                    placeAddressView.setText(place.getAddress());
                }

                placeSuggestionAccepted.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "placeSuggestionAccepted: " + placeSuggestionForAction.getType());
                        // Method to showcase API usage for accepting a suggestion for an
                        // important place like Home or Workplace.
                        acceptImportantPlaceSuggestion(placeSuggestionForAction,
                                placeNameView.getText().toString());
                    }
                });

                placeSuggestionRejected.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "placeSuggestionRejected: " + placeSuggestionForAction.getType());
                        // Method to showcase API usage for rejecting a suggestion for an
                        // important place like Home or Workplace.
                        rejectImportantPlaceSuggestion(placeSuggestionForAction);
                    }
                });
            } else {
                placeSuggestionAccepted.setEnabled(false);
                placeSuggestionRejected.setEnabled(false);
                actionStatusView.setVisibility(View.VISIBLE);
                actionStatusView.setText(placesSuggestionActionEmptyString);
            }
            return view;
        }

        /**
         * Helper function to showcase the usage of API for accepting a suggestion
         * for a place of importance like Home and Workplace.
         *
         * @param suggestion The {@link AcxImportantPlaceSuggestion} object received in
         *        {@link AcxPlaceManager}'s API fetchPendingImportantPlaceSuggestionList()
         * @param placeName The name of the place provided by user.
         */
        private void acceptImportantPlaceSuggestion(final AcxImportantPlaceSuggestion suggestion,
                final String placeName) {

            AcxPlaceManager placeManager = AcxServiceManager.getInstance().getPlaceManager();

            placeManager.acceptImportantPlaceSuggestion(suggestion, placeName,
                    new AcxServerCallback<AcxImportantPlaceSuggestion>() {

                @Override
                public void onSuccess(AcxImportantPlaceSuggestion placeSuggestion) {
                    Log.d(TAG, "acceptImportantPlaceSuggestion : onSuccess() for "
                            + placeSuggestion.toString());

                    final String message = getString(R.string.places_suggestion_accept_success);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            activity.finish();
                        }
                    });
                }

                /*
                 * (non-Javadoc)
                 * @see com.alohar.context.api.AcxServiceManager.AcxServerCallback#onError(com.alohar.context.api.model.AcxError)
                 */
                @Override
                public void onError(final AcxError error) {
                    Log.e(TAG, "acceptImportantPlaceSuggestion : onError()" + error.toString()
                            + " for " + suggestion.toString());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            actionStatusView.setVisibility(View.VISIBLE);
                            actionStatusView.setText(error.toString());
                        }
                    });
                }
            });
        }

        /**
         * Helper function to showcase the usage of API for rejecting a suggestion
         * for a place of importance like Home and Workplace.
         *
         * @param suggestion The {@link AcxImportantPlaceSuggestion} object received in
         *        {@link AcxPlaceManager}'s API fetchPendingImportantPlaceSuggestionList()
         */
        private void rejectImportantPlaceSuggestion(final AcxImportantPlaceSuggestion suggestion) {

            AcxPlaceManager placeManager = AcxServiceManager.getInstance().getPlaceManager();

            placeManager.rejectImportantPlaceSuggestion(suggestion,
                    new AcxServerCallback<AcxImportantPlaceSuggestion>() {

                @Override
                public void onSuccess(AcxImportantPlaceSuggestion placeSuggestion) {
                    Log.d(TAG, "rejectImportantPlaceSuggestion : onSuccess() for "
                            + placeSuggestion.toString());

                    final String message = getString(R.string.places_suggestion_reject_success);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            activity.finish();
                        }
                    });

                }

                /*
                 * (non-Javadoc)
                 * @see com.alohar.context.api.AcxServiceManager.AcxServerCallback#onError(com.alohar.context.api.model.AcxError)
                 */
                @Override
                public void onError(final AcxError error) {
                    Log.e(TAG, "rejectImportantPlaceSuggestion : onError()" + error.toString());

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            actionStatusView.setVisibility(View.VISIBLE);
                            actionStatusView.setText(error.toString());
                        }
                    });
                }
            });
        }
    } // End of ASImportantPlacesSuggestionsActionsDialog

    /**
     * Helper function to generate the list of data objects for the extendable listview adapter.
     *
     * @param emptyListText
     * @param placeSuggestionsList
     * @return the List of {@link ASPlaceSuggestionListObject}
     */
    private List<ASPlaceSuggestionListObject> getSuggestionObjectsList(
            String emptyListText,
            List<AcxImportantPlaceSuggestion> placeSuggestionsList) {
        final List<ASPlaceSuggestionListObject> asPlaceSuggestionsList = new ArrayList<ASPlaceSuggestionListObject>();
        if (placeSuggestionsList.isEmpty()) {
            ASPlaceSuggestionListObject listObject = new ASPlaceSuggestionListObject(emptyListText, null);
            asPlaceSuggestionsList.add(listObject);
        } else {
            for (AcxImportantPlaceSuggestion placeSuggestion : placeSuggestionsList) {
                ASPlaceSuggestionListObject listObject = new ASPlaceSuggestionListObject(null, placeSuggestion);
                asPlaceSuggestionsList.add(listObject);
            }
        }
        return asPlaceSuggestionsList;
    }

    public class ASPlacesSuggestionExpListAdapter extends BaseExpandableListAdapter {

        protected  Context mContext;

        /*
         * Class to help optimize the reuse of list items.
         */
        protected class ViewHolderHeader {
            public TextView groupHeader;
        }

        /*
         * Class to help optimize the reuse of list items.
         */
        protected class PlaceViewHolder {
            public TextView placeDetails;
            public TextView placeSuggestionUnconfirmed;
        }

        public ASPlacesSuggestionExpListAdapter(Context context) {
            mContext = context;
        }

        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getChild(int, int)
         */
        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return listGroupChildData.get(
                    listGroupHeaderData.get(groupPosition)).get(childPosititon);
        }
     
        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getChildId(int, int)
         */
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }
     
        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getChildView(int groupPosition, final int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
     
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_placesuggestion, null);
                PlaceViewHolder stayviewHolder = getPlaceItemViewHolder(convertView);
                convertView.setTag(stayviewHolder);
            }

            PlaceViewHolder pViewHolder = null;
            ASPlaceSuggestionListObject placeSuggestionListObject = (ASPlaceSuggestionListObject) (listGroupChildData.get(
                    listGroupHeaderData.get(groupPosition)).get(childPosition));

            if (placeSuggestionListObject != null) {
                pViewHolder = (PlaceViewHolder) convertView.getTag();
                final AcxImportantPlaceSuggestion placeSuggestion =
                        placeSuggestionListObject.getPlaceSuggestion();
                if (placeSuggestion != null) {
                    // Place Suggestion available.
                    final AcxPlace place = placeSuggestion.getPlace();
                    if (place != null) {
                        if (placeSuggestion.getState().equals(AcxImportantPlaceSuggestionState.PENDING)) {
                            pViewHolder.placeDetails.setText(ASUtility.getPlaceString(place));
                            pViewHolder.placeSuggestionUnconfirmed.setVisibility(View.VISIBLE);
                            pViewHolder.placeSuggestionUnconfirmed.setText(getString(R.string.places_unconfirmed));
                        }
                    }
                } else {
                    // PlaceSuggestion not found yet.
                    pViewHolder.placeSuggestionUnconfirmed.setVisibility(View.GONE);
                    pViewHolder.placeDetails.setText(placeSuggestionListObject.getPlaceStatus());
                }
            }

            return convertView;
        }
     
        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
         */
        @Override
        public int getChildrenCount(int groupPosition) {
            List<ASPlaceSuggestionListObject> children = listGroupChildData.get(
                    listGroupHeaderData.get(groupPosition));
            if (children != null) {
                return children.size();
            } else {
                return 0;
            }
        }
     
        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getGroup(int)
         */
        @Override
        public Object getGroup(int groupPosition) {
            return listGroupHeaderData.get(groupPosition);
        }

        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getGroupCount()
         */
        @Override
        public int getGroupCount() {
            return listGroupHeaderData.size();
        }

        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getGroupId(int)
         */
        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
     
        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            String headerTitle = (String)getGroup(groupPosition);
            Log.w(TAG, "getGroupView: position " + groupPosition + "::" +headerTitle);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.places_explist_group_item, null);
                ViewHolderHeader viewHolder = new ViewHolderHeader();
                viewHolder.groupHeader = (TextView) convertView.findViewById(R.id.places_group_header);
                convertView.setTag(viewHolder);
            }
     
            ViewHolderHeader holder = (ViewHolderHeader) convertView.getTag();
            if (headerTitle != null) {
                holder.groupHeader.setText(headerTitle);
            }

            expListView.expandGroup(groupPosition);
            return convertView;
        }

        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#hasStableIds()
         */
        @Override
        public boolean hasStableIds() {
            return false;
        }

        /*
         * (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
         */
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        /*
         * Updates the adapter data when it needs to be refreshed
         */
        public void updateListData(ArrayList<String> groupHeaders,
                HashMap<String, ArrayList<ASPlaceSuggestionListObject>> groupChilds) {
            listGroupHeaderData.clear();
            listGroupHeaderData.addAll(groupHeaders);
            listGroupChildData.clear();
            listGroupChildData.putAll(groupChilds);
        }

        /*
         * Helper method to generate the {@code PlaceViewHolder} object
         * required in the {@code ASPlacesAdapter}'s getView() method.
         */
        private PlaceViewHolder getPlaceItemViewHolder(View rowView) {
            PlaceViewHolder viewHolder = new PlaceViewHolder();
            viewHolder.placeDetails = (TextView) rowView.findViewById(R.id.place_details);
            viewHolder.placeSuggestionUnconfirmed = (TextView) rowView.findViewById(
                    R.id.place_suggestion_unconfirmed);
            return viewHolder;
        }
    }
}
