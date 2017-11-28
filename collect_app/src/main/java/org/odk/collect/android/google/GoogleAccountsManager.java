/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.google;


import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class GoogleAccountsManager implements EasyPermissions.PermissionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1002;
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 5555;

    @Nullable
    private Fragment fragment;
    @Nullable
    private Activity activity;

    private Context context;
    private GoogleAccountCredential credential;
    private GoogleAccountSelectionListener listener;
    private DriveHelper driveHelper;
    private SheetsHelper sheetsHelper;

    private HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    public GoogleAccountsManager(@NonNull Activity activity, GoogleAccountSelectionListener listener) {
        this.activity = activity;
        this.listener = listener;

        initCredentials(activity);
    }

    public GoogleAccountsManager(@NonNull Fragment fragment, GoogleAccountSelectionListener listener) {
        this.fragment = fragment;
        this.listener = listener;

        initCredentials(fragment.getActivity());
    }

    public GoogleAccountsManager(@NonNull Context context) {
        initCredentials(context);
    }

    private void initCredentials(Context context) {
        this.context = context;

        credential = GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff());
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        listener.googleAccountSelected();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        ToastUtils.showShortToast("Permission denied");
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    public void chooseAccount(boolean shouldUseSavedAccount) {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {

            if (shouldUseSavedAccount) {
                String accountName = getGoogleAccountName();
                if (!accountName.isEmpty()) {
                    selectAccount(accountName);
                    return;
                }
            }

            showAccountPickerDialog();

        } else {
            EasyPermissions.requestPermissions(
                    context,
                    context.getString(R.string.request_permissions_google_account),
                    REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }

    public String getGoogleAccountName() {
        return (String) GeneralSharedPreferences
                .getInstance()
                .get(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT);
    }

    private void showAccountPickerDialog() {
        Intent intentChooseAccount = credential.newChooseAccountIntent();
        intentChooseAccount.putExtra("overrideTheme", 1);
        intentChooseAccount.putExtra("overrideCustomTheme", 0);

        if (fragment != null) {
            fragment.startActivityForResult(intentChooseAccount, REQUEST_ACCOUNT_PICKER);
        } else if (activity != null) {
            activity.startActivityForResult(intentChooseAccount, REQUEST_ACCOUNT_PICKER);
        }
    }

    private void selectAccount(String accountName) {
        credential.setSelectedAccountName(accountName);
        listener.googleAccountSelected();
    }

    public boolean isGoogleAccountSelected() {
        return credential.getSelectedAccountName() != null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Timber.e(e);
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
        }
    }

    public DriveHelper getDriveHelper() {
        if (driveHelper == null) {
            driveHelper = new DriveHelper(credential, transport, jsonFactory);
        }
        return driveHelper;
    }

    public SheetsHelper getSheetsHelper() {
        if (sheetsHelper == null) {
            sheetsHelper = new SheetsHelper(credential, transport, jsonFactory);
        }
        return sheetsHelper;
    }

    public void setSelectedAccountName(String accountName) {
        if (accountName != null) {
            GeneralSharedPreferences
                    .getInstance()
                    .save(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT, accountName);
            selectAccount(accountName);
        }
    }

    @Nullable
    public Activity getActivity() {
        return activity;
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    public GoogleAccountCredential getCredentials() {
        return credential;
    }

    public interface GoogleAccountSelectionListener {
        void googleAccountSelected();
    }
}
