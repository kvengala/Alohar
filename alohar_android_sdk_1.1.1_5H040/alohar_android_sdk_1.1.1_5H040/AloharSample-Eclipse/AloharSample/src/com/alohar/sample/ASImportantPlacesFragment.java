/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.alohar.context.api.AcxPlaceManager;
import com.alohar.context.api.AcxServiceManager;
import com.alohar.context.api.AcxServiceManager.AcxServerCallback;
import com.alohar.context.api.model.AcxError;
import com.alohar.context.api.model.AcxImportantPlaces;
import com.alohar.context.api.model.AcxPlace;
import com.alohar.context.api.model.AcxPlaceStatistics;
import com.alohar.context.api.model.AcxUserStay;

public class ASImportantPlacesFragment extends Fragment{
    
    private static final String TAG = ASImportantPlacesFragment.class.getSimpleName();

    private static final int GROUPHEADER_INDEX_HOME = 0;
    private static final int GROUPHEADER_INDEX_WORKPLACES = 1;
    private static final int GROUPHEADER_INDEX_MOSTSTAYEDPLACES = 2;

    private static final String GROUPHEADER_NAME_HOME = "Home";
    private static final String GROUPHEADER_NAME_WORK = "Work";
    private static final String GROUPHEADER_NAME_MOST_STAYED_PLACES = "Places with most UserStays";
    
    private String placeLabelString;

    private Activity activity;
    private ASPlacesExpListAdapter adapter;
    private TextView statusLine;
    private ExpandableListView expListView;

    /**
     * Helper class to create a UI specific data model object to show the important places
     * in groups of Home, Workplaces and MostStayedPlaces.
     */
    private class ASPlaceListObject {
        final private String placeStatus;
        final private AcxPlaceStatistics placeStatistics;
        
        public ASPlaceListObject(String thisPlaceStatus, AcxPlaceStatistics thisPlaceStatictics) {
            placeStatus = thisPlaceStatus;
            placeStatistics = thisPlaceStatictics;
        }

        public String getPlaceStatus() {
            return placeStatus;
        }

        public AcxPlaceStatistics getPlaceStatistics() {
            return placeStatistics;
        }

    }

    // header titles
    protected final ArrayList<String> listGroupHeaderData = new ArrayList<String>() ; 
    // child data in format of header title, child title
    protected final HashMap<String, List<ASPlaceListObject>> listGroupChildData
            = new HashMap<String, List<ASPlaceListObject>>();

    private String homePlacesEmptyString;
    private String workPlacesEmptyString;
    private String mostStayedPlacesEmptyString;
    private String unknownGroupEmptyString;
    private String placesErrorString;

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        placeLabelString = getString(R.string.place_label_string);
        homePlacesEmptyString = getString(R.string.places_home_determining_or_confirmation_pending);
        workPlacesEmptyString = getString(R.string.places_workplace_determining_or_confirmation_pending);
        mostStayedPlacesEmptyString = getString(R.string.places_moststayedplaces_determining);
        unknownGroupEmptyString = getString(R.string.places_unknown_group_places_not_found);
        placesErrorString = getString(R.string.places_error);
    }

    /* (non-Javadoc)
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_places, container, false);
        activity = getActivity();

        statusLine = (TextView) root.findViewById(R.id.places_status);
        statusLine.setVisibility(View.GONE);

        TextView placeSuggestionsLink = (TextView)root.findViewById(R.id.places_link_to_suggestions);
        placeSuggestionsLink.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ASImportantPlacesSuggestionsActivity.class);
                startActivity(intent);
            }
        });

        expListView = (ExpandableListView)root.findViewById(R.id.places_explistview);
        expListView.setGroupIndicator(null);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            public boolean onGroupClick(ExpandableListView arg0, View itemView,
                    int itemPosition, long itemId) {
                expListView.expandGroup(itemPosition);
                return true;
            }
        });
        adapter = new ASPlacesExpListAdapter(activity);
        expListView.setAdapter(adapter);


        return root;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

        //Method to showcase API usage for fetching important places.
        importantPlaces();
    }

    /**
     * Helper function to showcase the API call for fetching the important places like
     * Home, WorkPlaces and Most stayed places.
     */
    private void importantPlaces() {
        AcxPlaceManager placeManager = AcxServiceManager.getInstance().getPlaceManager();
        placeManager.fetchImportantPlaces(new AcxServerCallback<AcxImportantPlaces>() {
            @Override
            public void onSuccess(AcxImportantPlaces importantPlaces) {

                Log.d(TAG, "fetchImportantPlaces() Success()");
                final List<AcxPlaceStatistics> mostStayedPlaces = importantPlaces.getMostStayedPlaceStatisticsList();
                final List<AcxPlaceStatistics> homePlaces = importantPlaces.getHomeStatisticsList();
                final List<AcxPlaceStatistics> workPlaces = importantPlaces.getWorkplaceStatisticsList();

                final List<ASPlaceListObject> mostStayedPlacesList = getPlacesList(mostStayedPlaces, GROUPHEADER_INDEX_MOSTSTAYEDPLACES);
                final List<ASPlaceListObject> homePlacesList = getPlacesList(homePlaces, GROUPHEADER_INDEX_HOME);
                final List<ASPlaceListObject> workPlacesList = getPlacesList(workPlaces, GROUPHEADER_INDEX_WORKPLACES);

                listGroupHeaderData.clear();
                listGroupHeaderData.add(GROUPHEADER_INDEX_HOME, GROUPHEADER_NAME_HOME);
                listGroupHeaderData.add(GROUPHEADER_INDEX_WORKPLACES, GROUPHEADER_NAME_WORK);
                listGroupHeaderData.add(GROUPHEADER_INDEX_MOSTSTAYEDPLACES, GROUPHEADER_NAME_MOST_STAYED_PLACES);

                listGroupChildData.clear();
                listGroupChildData.put(listGroupHeaderData.get(GROUPHEADER_INDEX_HOME), homePlacesList);
                listGroupChildData.put(listGroupHeaderData.get(GROUPHEADER_INDEX_WORKPLACES), workPlacesList);
                listGroupChildData.put(listGroupHeaderData.get(GROUPHEADER_INDEX_MOSTSTAYEDPLACES), mostStayedPlacesList);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusLine.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(AcxError error) {
                Log.e(TAG, "fetchImportantPlaces() onError(): " + error.toString());
                final String message = String.format(placesErrorString, error.toString());

                listGroupHeaderData.clear();
                listGroupChildData.clear();

                activity.runOnUiThread(new Runnable() {
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


    /**
     * Helper function to generate the list of data objects for the extendable listview adapter.
     *
     * @param placesStatisticsList
     * @param groupIndex
     * @return the List of {@link ASPlaceListObject}
     */
    private List<ASPlaceListObject> getPlacesList(List<AcxPlaceStatistics> placesStatisticsList, int groupIndex) {
        final List<ASPlaceListObject> asPlacesList = new ArrayList<ASPlaceListObject>();
        if (placesStatisticsList.isEmpty()) {
            ASPlaceListObject listObject = new ASPlaceListObject(getEmptyListText(groupIndex), null);
            asPlacesList.add(listObject);
        } else {
            for (AcxPlaceStatistics placeStat : placesStatisticsList) {
                ASPlaceListObject listObject = new ASPlaceListObject(null, placeStat);
                asPlacesList.add(listObject);
            }
        }
        return asPlacesList;
    }

    /**
     * Helper function to get the String to display for
     * empty Home, WorkPlaces and MostStayedPlaces list..
     * 
     * @param groupIndex signifies whether it is for Home or WorkPlaces or MostStayedPlaces group.
     * @return the String for the corresponding group.
     */
    private String getEmptyListText(int groupIndex) {
        String emptyListText = unknownGroupEmptyString;
        switch (groupIndex) {
        case GROUPHEADER_INDEX_HOME:
            emptyListText = homePlacesEmptyString;
            break;
        case GROUPHEADER_INDEX_WORKPLACES:
            emptyListText = workPlacesEmptyString;
            break;
        case GROUPHEADER_INDEX_MOSTSTAYEDPLACES:
            emptyListText = mostStayedPlacesEmptyString;
            break;
        }
        return emptyListText;
    }

    public class ASPlacesExpListAdapter extends BaseExpandableListAdapter {

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
            public TextView userStayCount;
            public TextView mostRecentUserStayTime;
        }

        public ASPlacesExpListAdapter(Context context) {
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_place_statistics, null);
                PlaceViewHolder stayviewHolder = getPlaceItemViewHolder(convertView);
                convertView.setTag(stayviewHolder);
            }

            PlaceViewHolder pViewHolder = null;
            ASPlaceListObject placeListObject = (ASPlaceListObject) (listGroupChildData.get(
                    listGroupHeaderData.get(groupPosition)).get(childPosition));
            
            if (placeListObject != null) {
                pViewHolder = (PlaceViewHolder) convertView.getTag();
                final AcxPlaceStatistics placeStats = placeListObject.getPlaceStatistics();
                if (placeStats == null) {
                    pViewHolder.placeDetails.setText(placeListObject.getPlaceStatus());
                    pViewHolder.userStayCount.setVisibility(View.GONE);
                    pViewHolder.mostRecentUserStayTime.setVisibility(View.GONE);
                } else {
                    final int userStayCount = placeStats.getUserStayCount();
                    final AcxUserStay lastUserStay = placeStats.getLastUserStay();

                    String mostRecentUserStayTime = "";
                    if (lastUserStay != null) {
                        mostRecentUserStayTime = ASUtility.getDateTimeString(
                                lastUserStay.getStartTimeInUtcMillis());
                    }

                    AcxPlace place = placeStats.getPlace();
                    if (place != null) {
                        pViewHolder.placeDetails.setText(
                                ASUtility.getPlaceString(place, placeLabelString));
                    }
                    pViewHolder.userStayCount.setVisibility(View.VISIBLE);
                    pViewHolder.mostRecentUserStayTime.setVisibility(View.VISIBLE);
                    pViewHolder.userStayCount.setText("userStayCount: " + userStayCount);
                    pViewHolder.mostRecentUserStayTime.setText("userStayMostRecent.startDate: "
                            + mostRecentUserStayTime);
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
            List<ASPlaceListObject> children = listGroupChildData.get(
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
                HashMap<String, ArrayList<ASPlaceListObject>> groupChilds) {
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
            viewHolder.placeDetails = (TextView) rowView.findViewById(R.id.placestat_placedetails);
            viewHolder.userStayCount = (TextView) rowView.findViewById(R.id.placestat_userstaycount);
            viewHolder.mostRecentUserStayTime = (TextView) rowView.findViewById(R.id.placestat_mostrecentuserstay);
            return viewHolder;
        }
    }
}
