/*
 * Copyright 2010-2015 Alohar Mobile Inc.
 * All Rights Reserved.
 * Contains Proprietary, Confidential & Trade Secret Information of Alohar Mobile Inc.
 */

package com.alohar.sample;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alohar.context.api.AcxServiceManager;

public class ASAboutFragment extends Fragment {

    private static final String LEGAL_URL = "https://developer.alohar.com/devportal/terms/";
    private static final String PRIVACY_URL = "https://developer.alohar.com/devportal/privacy/";

    /* (non-Javadoc)
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        TextView appVersionTextView = (TextView) root.findViewById(R.id.app_version_textview);
        TextView appIdTextView = (TextView) root.findViewById(R.id.app_id_textview);
        TextView sdkVersionTextView = (TextView) root.findViewById(R.id.sdk_version_textview);
        String versionName = null;
        String packageName = getActivity().getPackageName();
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(packageName, 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("ASAboutFragment", "onActivityCreated(): NameNotFoundException: " + e.getMessage());
        }
        
        appVersionTextView.setText(versionName);
        appIdTextView.setText(String.valueOf(ASConfig.APP_ID));

        AcxServiceManager acxManager = AcxServiceManager.getInstance();
        String sdkVersion = acxManager.getSdkVersion();
        
        sdkVersionTextView.setText(sdkVersion);

        TextView appLegal = (TextView) root.findViewById(R.id.legal_textview);
        appLegal.setPaintFlags(appLegal.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        appLegal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LEGAL_URL)));
            }
        });

        TextView appPrivacy = (TextView) root.findViewById(R.id.privacy_textview);
        appPrivacy.setPaintFlags(appPrivacy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        appPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL)));
            }
        });

        return root;
    }
}
