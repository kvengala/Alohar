/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alohar.context.api.AcxServiceManager;

public class ASMainActivity extends Activity {

    private static final String TAG = ASMainActivity.class.getSimpleName();

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {

        /* (non-Javadoc)
         * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    // nav drawer title
    private CharSequence drawerTitle;
    // used to store app title
    private CharSequence title;
    // slide menu items
    private String[] navMenuTitles;

    private static final int HOME_FRAGMENT_POS = 0;
    private static final int STAYS_LIST_FRAGMENT_POS = 1;
    private static final int PLACES_FRAGMENT_POS = 2;
    private static final int EVENTS_FRAGMENT_POS = 3;
    private static final int ABOUT_FRAGMENT_POS = 4;

    /**
     * Display fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
        case HOME_FRAGMENT_POS:
            fragment = new ASHomeFragment();
            break;
        case STAYS_LIST_FRAGMENT_POS:
            fragment = new ASStaysListFragment();
            break;
        case PLACES_FRAGMENT_POS:
            fragment = new ASImportantPlacesFragment();
            break;
        case EVENTS_FRAGMENT_POS:
            fragment = new ASEventsFragment();
            break;
        case ABOUT_FRAGMENT_POS:
            fragment = new ASAboutFragment();
            break;
        default:
            break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            drawerListView.setItemChecked(position, true);
            drawerListView.setSelection(position);
            setTitle(navMenuTitles[position]);
            drawerLayout.closeDrawer(drawerListView);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AcxServiceManager acxManager = AcxServiceManager.getInstance();
        String aloharUid = acxManager.isSignedIn() ? acxManager.getAloharUid() : null;
        if ((aloharUid == null) || aloharUid.isEmpty()) {
            
        	// Go to the login/register activity.
            Intent intent = new Intent(this, ASLoginRegisterActivity.class);
            startActivity(intent);

            // Close the main activity.
            finish();
            return;
        }
                
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_main);

        title = drawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView = (ListView) findViewById(R.id.list_slidermenu);

        ArrayList<ASNavDrawerItem> navDrawerItems = new ArrayList<ASNavDrawerItem>();

        // adding nav drawer items to array
        // Home
        navDrawerItems.add(new ASNavDrawerItem(navMenuTitles[HOME_FRAGMENT_POS],
                navMenuIcons.getResourceId(HOME_FRAGMENT_POS, -1)));
        // stays list
        navDrawerItems.add(new ASNavDrawerItem(navMenuTitles[STAYS_LIST_FRAGMENT_POS],
                navMenuIcons.getResourceId(STAYS_LIST_FRAGMENT_POS, -1)));
        // Places
        navDrawerItems.add(new ASNavDrawerItem(navMenuTitles[PLACES_FRAGMENT_POS],
                navMenuIcons.getResourceId(PLACES_FRAGMENT_POS, -1)));
        // Motion
        navDrawerItems.add(new ASNavDrawerItem(navMenuTitles[EVENTS_FRAGMENT_POS],
                navMenuIcons.getResourceId(EVENTS_FRAGMENT_POS, -1)));
        // Settings
        navDrawerItems.add(new ASNavDrawerItem(navMenuTitles[ABOUT_FRAGMENT_POS],
                navMenuIcons.getResourceId(ABOUT_FRAGMENT_POS, -1)));

        // Recycle the typed array
        navMenuIcons.recycle();

        drawerListView.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        ASNavDrawerListAdapter adapter = new ASNavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        drawerListView.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.apptheme_ic_navigation_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name) { // nav drawer close - description for accessibility

            public void onDrawerClosed(View view) {
                getActionBar().setTitle(title);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(drawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPostCreate(android.os.Bundle)
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        return super.onPrepareOptionsMenu(menu);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#setTitle(java.lang.CharSequence)
     */
    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        getActionBar().setTitle(this.title);
    }
}
