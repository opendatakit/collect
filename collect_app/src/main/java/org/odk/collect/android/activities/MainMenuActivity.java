/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.activities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.DeleteFormsListener;
import org.odk.collect.android.listeners.DeleteInstancesListener;
import org.odk.collect.android.listeners.FetchUserGroupListener;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.listeners.FormListDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.tasks.DeleteFormsTask;
import org.odk.collect.android.tasks.DeleteInstancesTask;
import org.odk.collect.android.tasks.DownloadFormListTask;
import org.odk.collect.android.tasks.DownloadFormsTask;
import org.odk.collect.android.tasks.FetchUserGroupDataTask;
import org.odk.collect.android.utilities.CDL;
import org.odk.collect.android.utilities.CompatibilityUtils;
import org.odk.collect.android.utilities.TextUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends Activity implements FormListDownloaderListener,FormDownloaderListener,
        FetchUserGroupListener, DeleteInstancesListener, DeleteFormsListener {
    private static final String t = "MainMenuActivity";

    private static final int PASSWORD_DIALOG = 1;
    private static final int LOGOUT_DIALOG = 2;

    // menu options
    private static final int MENU_PREFERENCES = Menu.FIRST;
    private static final int MENU_ADMIN = Menu.FIRST + 1;
    private static final String CSV_HEADER = "Groups";
    private static final String COLDTRACE = "CT";
    private static final String STOVETRACE = "ST";

    // buttons
    private Button mEnterDataButton;
    private Button mManageFilesButton;
    private Button mSendDataButton;
    private Button mReviewDataButton;
    private Button mGetFormsButton;

    private View mReviewSpacer;
    private View mGetFormsSpacer;

    private AlertDialog mAlertDialog;
    private SharedPreferences mAdminPreferences;

    private int mCompletedCount;
    private int mSavedCount;

    private Cursor mFinalizedCursor;
    private Cursor mSavedCursor;

    private IncomingHandler mHandler = new IncomingHandler(this);
    private MyContentObserver mContentObserver = new MyContentObserver();

    private static boolean EXIT = true;
    private ProgressDialog mProgressDialog;

    private HashMap<String, FormDetails> mFormNamesAndURLs = new HashMap<String, FormDetails>();
    private DownloadFormListTask mDownloadFormListTask;
    private static final String FORMNAME = "formname";
    private static final String FORMDETAIL_KEY = "formdetailkey";
    private static final String FORMID_DISPLAY = "formiddisplay";

    private static final String FORM_ID_KEY = "formid";
    private static final String FORM_VERSION_KEY = "formversion";
    ArrayList<HashMap<String, String>> mFormList = new ArrayList<HashMap<String, String>>();
    private DownloadFormsTask mDownloadFormsTask;
    private SharedPreferences preferences;
    private DeleteInstancesTask mDeleteInstancesTask = null;
    private ArrayList<Long> mEditedFormList = new ArrayList<>();
    private ArrayList<Long> mLocalFormList = new ArrayList<>();
    private DeleteFormsTask mDeleteFormsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // must be at the beginning of any activity that can be called from an
        // external intent
        Log.i(t, "Starting up, creating directories");
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        setContentView(R.layout.main_menu);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mProgressDialog = new ProgressDialog(this);
        {
            // dynamically construct the "ODK Collect vA.B" string
            TextView mainMenuMessageLabel = (TextView) findViewById(R.id.main_menu_header);
            mainMenuMessageLabel.setText(Collect.getInstance()
                    .getVersionedAppName());
        }

        setTitle(getString(R.string.app_name) + " > "
                + getString(R.string.main_menu));

        File f = new File(Collect.ODK_ROOT + "/collect.settings");
        if (f.exists()) {
            boolean success = loadSharedPreferencesFromFile(f);
            if (success) {
                Toast.makeText(this,
                        "Settings successfully loaded from file",
                        Toast.LENGTH_LONG).show();
                f.delete();
            } else {
                Toast.makeText(
                        this,
                        "Sorry, settings file is corrupt and should be deleted or replaced",
                        Toast.LENGTH_LONG).show();
            }
        }

        mReviewSpacer = findViewById(R.id.review_spacer);
        mGetFormsSpacer = findViewById(R.id.get_forms_spacer);

        mAdminPreferences = this.getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        // enter data button. expects a result.
        mEnterDataButton = (Button) findViewById(R.id.enter_data);
        mEnterDataButton.setText(getString(R.string.enter_data_button));
        mEnterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "fillBlankForm", "click");
                Intent i = new Intent(getApplicationContext(),
                        FormChooserList.class);
                startActivity(i);
            }
        });

        // review data button. expects a result.
        mReviewDataButton = (Button) findViewById(R.id.review_data);
        mReviewDataButton.setText(getString(R.string.review_data_button));
        mReviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "editSavedForm", "click");
                Intent i = new Intent(getApplicationContext(),
                        InstanceChooserList.class);
                startActivity(i);
            }
        });

        // send data button. expects a result.
        mSendDataButton = (Button) findViewById(R.id.send_data);
        mSendDataButton.setText(getString(R.string.send_data_button));
        mSendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "uploadForms", "click");
                Intent i = new Intent(getApplicationContext(),
                        InstanceUploaderList.class);
                startActivity(i);
            }
        });

        // manage forms button. no result expected.
        mGetFormsButton = (Button) findViewById(R.id.get_forms);
        mGetFormsButton.setText(getString(R.string.get_updated_form));
        mGetFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Collect.getInstance().getActivityLogger()
						.logAction(this, "downloadBlankForms", "click");
				SharedPreferences sharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(MainMenuActivity.this);
				String protocol = sharedPreferences.getString(
						PreferencesActivity.KEY_PROTOCOL, getString(R.string.protocol_odk_default));
				Intent i = null;
				if (protocol.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
					i = new Intent(getApplicationContext(),
							GoogleDriveActivity.class);
				} else {
					i = new Intent(getApplicationContext(),
							FormDownloadList.class);
				}
				startActivity(i);*/
                downloadFormList();

            }
        });

        // manage forms button. no result expected.
        mManageFilesButton = (Button) findViewById(R.id.manage_forms);
        mManageFilesButton.setText(getString(R.string.manage_files));
        mManageFilesButton.setVisibility(View.GONE);
        mManageFilesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "deleteSavedForms", "click");
                Intent i = new Intent(getApplicationContext(),
                        FileManagerTabs.class);
                startActivity(i);
            }
        });

        // count for finalized instances
        String selection = InstanceColumns.STATUS + "=? or "
                + InstanceColumns.STATUS + "=?";
        String selectionArgs[] = {InstanceProviderAPI.STATUS_COMPLETE,
                InstanceProviderAPI.STATUS_SUBMISSION_FAILED};

        try {
            mFinalizedCursor = managedQuery(InstanceColumns.CONTENT_URI, null,
                    selection, selectionArgs, null);
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (mFinalizedCursor != null) {
            startManagingCursor(mFinalizedCursor);
        }
        mCompletedCount = mFinalizedCursor != null ? mFinalizedCursor.getCount() : 0;
        getContentResolver().registerContentObserver(InstanceColumns.CONTENT_URI, true, mContentObserver);
//		mFinalizedCursor.registerContentObserver(mContentObserver);

        // count for finalized instances
        String selectionSaved = InstanceColumns.STATUS + "=?";
        String selectionArgsSaved[] = {InstanceProviderAPI.STATUS_INCOMPLETE};

        try {
            mSavedCursor = managedQuery(InstanceColumns.CONTENT_URI, null,
                    selectionSaved, selectionArgsSaved, null);
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        if (mSavedCursor != null) {
            startManagingCursor(mSavedCursor);
        }
        mSavedCount = mSavedCursor != null ? mSavedCursor.getCount() : 0;
        // don't need to set a content observer because it can't change in the
        // background
        int checkLogin = preferences.getInt(PreferencesActivity.KEY_FROM_LOGIN, 0);
        if (checkLogin == 1) {
            Editor editor = preferences.edit();
            editor.putInt(PreferencesActivity.KEY_FROM_LOGIN, 0);
            editor.apply();
            downloadFormList();
        }
        updateButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        boolean edit = sharedPreferences.getBoolean(
                AdminPreferencesActivity.KEY_EDIT_SAVED, true);
        if (!edit) {
            mReviewDataButton.setVisibility(View.GONE);
            mReviewSpacer.setVisibility(View.GONE);
        } else {
            mReviewDataButton.setVisibility(View.VISIBLE);
            mReviewSpacer.setVisibility(View.VISIBLE);
        }

        boolean send = sharedPreferences.getBoolean(
                AdminPreferencesActivity.KEY_SEND_FINALIZED, true);
        if (!send) {
            mSendDataButton.setVisibility(View.GONE);
        } else {
            mSendDataButton.setVisibility(View.VISIBLE);
        }

        boolean get_blank = sharedPreferences.getBoolean(
                AdminPreferencesActivity.KEY_GET_BLANK, true);
        if (!get_blank) {
            mGetFormsButton.setVisibility(View.GONE);
            mGetFormsSpacer.setVisibility(View.GONE);
        } else {
            mGetFormsButton.setVisibility(View.VISIBLE);
            mGetFormsSpacer.setVisibility(View.VISIBLE);
        }
        //Remove delete all form button
        /*boolean delete_saved = sharedPreferences.getBoolean(
                AdminPreferencesActivity.KEY_DELETE_SAVED, true);
        if (!delete_saved) {
            mManageFilesButton.setVisibility(View.GONE);
        } else {
            mManageFilesButton.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Collect.getInstance().getActivityLogger()
                .logAction(this, "onCreateOptionsMenu", "show");
        super.onCreateOptionsMenu(menu);

        CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_PREFERENCES, 0, R.string.logout_preferences)
                        .setIcon(R.drawable.ic_logout),
                MenuItem.SHOW_AS_ACTION_ALWAYS);
        /*CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_ADMIN, 0, R.string.admin_preferences)
                        .setIcon(R.drawable.ic_menu_login),
                MenuItem.SHOW_AS_ACTION_NEVER);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                Collect.getInstance()
                        .getActivityLogger()
                        .logAction(this, "onOptionsItemSelected",
                                "MENU_PREFERENCES");
                showDialog(LOGOUT_DIALOG);
                /*Intent ig = new Intent(this, PreferencesActivity.class);
                startActivity(ig);*/
                return true;
            /*case MENU_ADMIN:
                Collect.getInstance().getActivityLogger()
                        .logAction(this, "onOptionsItemSelected", "MENU_ADMIN");
                String pw = mAdminPreferences.getString(
                        AdminPreferencesActivity.KEY_ADMIN_PW, "");
                if ("".equalsIgnoreCase(pw)) {
                    Intent i = new Intent(getApplicationContext(),
                            AdminPreferencesActivity.class);
                    startActivity(i);
                } else {
                    showDialog(PASSWORD_DIALOG);
                    Collect.getInstance().getActivityLogger()
                            .logAction(this, "createAdminPasswordDialog", "show");
                }
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        Collect.getInstance().getActivityLogger()
                .logAction(this, "createErrorDialog", "show");
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Collect.getInstance()
                                .getActivityLogger()
                                .logAction(this, "createErrorDialog",
                                        shouldExit ? "exitApplication" : "OK");
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PASSWORD_DIALOG:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog passwordDialog = builder.create();

                passwordDialog.setTitle(getString(R.string.enter_admin_password));
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTransformationMethod(PasswordTransformationMethod
                        .getInstance());
                passwordDialog.setView(input, 20, 10, 20, 10);

                passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
                                String pw = mAdminPreferences.getString(
                                        AdminPreferencesActivity.KEY_ADMIN_PW, "");
                                if (pw.compareTo(value) == 0) {
                                    Intent i = new Intent(getApplicationContext(),
                                            AdminPreferencesActivity.class);
                                    startActivity(i);
                                    input.setText("");
                                    passwordDialog.dismiss();
                                } else {
                                    Toast.makeText(
                                            MainMenuActivity.this,
                                            getString(R.string.admin_password_incorrect),
                                            Toast.LENGTH_SHORT).show();
                                    Collect.getInstance()
                                            .getActivityLogger()
                                            .logAction(this, "adminPasswordDialog",
                                                    "PASSWORD_INCORRECT");
                                }
                            }
                        });

                passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logAction(this, "adminPasswordDialog",
                                                "cancel");
                                input.setText("");
                                return;
                            }
                        });

            case LOGOUT_DIALOG:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                final AlertDialog logoutDialog = builder1.create();
                logoutDialog.setCanceledOnTouchOutside(false);
                logoutDialog.setTitle(getString(R.string.logout_alert_title));
                logoutDialog.setMessage(getString(R.string.logout_alert_msg));

                logoutDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.logout_preferences),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                deleteLocalForms();
                            }
                        });

                logoutDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logAction(this, "adminPasswordDialog",
                                                "cancel");
                                return;
                            }
                        });

                logoutDialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return logoutDialog;


        }
        return null;
    }

    private void updateButtons() {
        if (mFinalizedCursor != null && !mFinalizedCursor.isClosed()) {
            mFinalizedCursor.requery();
            mCompletedCount = mFinalizedCursor.getCount();
            if (mCompletedCount > 0) {
                mSendDataButton.setText(getString(R.string.send_data_button, mCompletedCount));
            } else {
                mSendDataButton.setText(getString(R.string.send_data));
            }
        } else {
            mSendDataButton.setText(getString(R.string.send_data));
            Log.w(t, "Cannot update \"Send Finalized\" button label since the database is closed. Perhaps the app is running in the background?");
        }

        if (mSavedCursor != null && !mSavedCursor.isClosed()) {
            mSavedCursor.requery();
            mSavedCount = mSavedCursor.getCount();
            if (mSavedCount > 0) {
                mReviewDataButton.setText(getString(R.string.review_data_button,
                        mSavedCount));
            } else {
                mReviewDataButton.setText(getString(R.string.review_data));
            }
        } else {
            mReviewDataButton.setText(getString(R.string.review_data));
            Log.w(t, "Cannot update \"Edit Form\" button label since the database is closed. Perhaps the app is running in the background?");
        }
    }

    /**
     * notifies us that something changed
     */
    private class MyContentObserver extends ContentObserver {

        public MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHandler.sendEmptyMessage(0);
        }
    }

    /*
     * Used to prevent memory leaks
     */
    static class IncomingHandler extends Handler {
        private final WeakReference<MainMenuActivity> mTarget;

        IncomingHandler(MainMenuActivity target) {
            mTarget = new WeakReference<MainMenuActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MainMenuActivity target = mTarget.get();
            if (target != null) {
                target.updateButtons();
            }
        }
    }

    private boolean loadSharedPreferencesFromFile(File src) {
        // this should probably be in a thread if it ever gets big
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(
                    this).edit();
            prefEdit.clear();
            // first object is preferences
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    prefEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    prefEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.commit();

            // second object is admin options
            Editor adminEdit = getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0).edit();
            adminEdit.clear();
            // first object is preferences
            Map<String, ?> adminEntries = (Map<String, ?>) input.readObject();
            for (Entry<String, ?> entry : adminEntries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    adminEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    adminEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    adminEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    adminEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    adminEdit.putString(key, ((String) v));
            }
            adminEdit.commit();

            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Starts the download task and shows the progress dialog.
     */
    private void downloadFormList() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected()) {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
        } else {

            mFormNamesAndURLs = new HashMap<String, FormDetails>();
            if (mProgressDialog != null) {
                // This is needed because onPrepareDialog() is broken in 1.6.
                mProgressDialog.setMessage(getString(R.string.please_wait));
            }
            mProgressDialog.show();

            if (mDownloadFormListTask != null &&
                    mDownloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
                return; // we are already doing the download!!!
            } else if (mDownloadFormListTask != null) {
                mDownloadFormListTask.setDownloaderListener(null);
                mDownloadFormListTask.cancel(true);
                mDownloadFormListTask = null;
            }

            mDownloadFormListTask = new DownloadFormListTask();
            mDownloadFormListTask.setDownloaderListener(this);
            mDownloadFormListTask.execute();
        }
    }

    @Override
    public void formListDownloadingComplete(HashMap<String, FormDetails> result) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mDownloadFormListTask.setDownloaderListener(null);
        mDownloadFormListTask = null;

        if (result == null) {
            Log.e("", "Formlist Downloading returned null.  That shouldn't happen");
            // Just displayes "error occured" to the user, but this should never happen.
            /*createAlertDialog(getString(R.string.load_remote_form_error),
                    getString(R.string.error_occured), EXIT);*/
            return;
        }

        if (result.containsKey(DownloadFormListTask.DL_AUTH_REQUIRED)) {
            // need authorization
            /*showDialog(AUTH_DIALOG);*/
        } else if (result.containsKey(DownloadFormListTask.DL_ERROR_MSG)) {
            // Download failed
            String dialogMessage =
                    getString(R.string.list_failed_with_error,
                            result.get(DownloadFormListTask.DL_ERROR_MSG).errorStr);
            String dialogTitle = getString(R.string.load_remote_form_error);
            /*createAlertDialog(dialogTitle, dialogMessage, DO_NOT_EXIT);*/
        } else {
            // Everything worked. Clear the list and add the results.
            mFormNamesAndURLs = result;

            /*mFormList.clear();*/

            ArrayList<String> ids = new ArrayList<String>(mFormNamesAndURLs.keySet());
            for (int i = 0; i < result.size(); i++) {
                String formDetailsKey = ids.get(i);
                FormDetails details = mFormNamesAndURLs.get(formDetailsKey);
                HashMap<String, String> item = new HashMap<String, String>();
                if (details.formName.startsWith(currentOption())) {
                    item.put(FORMNAME, details.formName);
                    item.put(FORMID_DISPLAY,
                            ((details.formVersion == null) ? "" : (getString(R.string.version) + " " + details.formVersion + " ")) +
                                    "ID: " + details.formID);
                    item.put(FORMDETAIL_KEY, formDetailsKey);
                    item.put(FORM_ID_KEY, details.formID);
                    item.put(FORM_VERSION_KEY, details.formVersion);

                    // Insert the new form in alphabetical order.
                    if (mFormList.size() == 0) {
                        mFormList.add(item);
                    } else {
                        int j;
                        for (j = 0; j < mFormList.size(); j++) {
                            HashMap<String, String> compareMe = mFormList.get(j);
                            String name = compareMe.get(FORMNAME);
                            if (name.compareTo(mFormNamesAndURLs.get(ids.get(i)).formName) > 0) {
                                break;
                            }
                        }
                        mFormList.add(j, item);
                    }
                } else {
                    Log.i(t, "Unsort forms " + details.formName);
                }
            }

            /*mFormListAdapter.notifyDataSetChanged();
            mDownloadButton.setEnabled(!(selectedItemCount() == 0));*/
            downloadSelectedFiles(mFormList);
        }
    }

    private String currentOption() {

        String option;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int selected = preferences.getInt(PreferencesActivity.KEY_OPTION_SELECTED, 0);
        switch (selected) {
            case 2:
                option = STOVETRACE;
                break;
            case R.id.rb_coldtrace:
                // falls through
            default:
                option = COLDTRACE;
                break;
        }
        return option;
    }

    @SuppressWarnings("unchecked")
    private void downloadSelectedFiles(ArrayList<HashMap<String, String>> formList) {
        int totalCount = 0;
        ArrayList<FormDetails> filesToDownload = new ArrayList<FormDetails>();

        for (int i = 0; i < formList.size(); i++) {
            HashMap<String, String> item = formList.get(i);
            filesToDownload.add(mFormNamesAndURLs.get(item.get(FORMDETAIL_KEY)));
        }

        totalCount = filesToDownload.size();

        if (totalCount > 0) {
            // show dialog box
            mProgressDialog.show();

            mDownloadFormsTask = new DownloadFormsTask();
            mDownloadFormsTask.setDownloaderListener(this);
            mDownloadFormsTask.execute(filesToDownload);
        } else {
            Toast.makeText(getApplicationContext(), R.string.noselect_error, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void formsDownloadingComplete(HashMap<FormDetails, String> result) {

        if (mDownloadFormsTask != null) {
            mDownloadFormsTask.setDownloaderListener(null);
        }

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            // should always be true here
            mProgressDialog.dismiss();
        }

        Set<FormDetails> keys = result.keySet();
        StringBuilder b = new StringBuilder();
        for (FormDetails k : keys) {
            b.append(k.formName +
                    " (" +
                    ((k.formVersion != null) ?
                            (this.getString(R.string.version) + ": " + k.formVersion + " ")
                            : "") +
                    "ID: " + k.formID + ") - " +
                    result.get(k));
            b.append("\n\n");
        }

        String token = preferences.getString(PreferencesActivity.KEY_TOKEN, "");
        if (!token.equalsIgnoreCase("Guest")) {
            mProgressDialog.show();
            FetchUserGroupDataTask task = new FetchUserGroupDataTask();
            task.setFetchUserGroupListener(this);
            task.execute(preferences.getString(PreferencesActivity.KEY_TOKEN, ""));
        }
    }

    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        String mAlertMsg = getString(R.string.fetching_file, currentFile, progress, total);
        mProgressDialog.setMessage(mAlertMsg);
    }


    private void deleteSelectedInstances() {
        mEditedFormList.clear();

        Cursor sendFormManagerCursor = managedQuery(InstanceColumns.CONTENT_URI, null, null, null,
                InstanceColumns.DISPLAY_NAME + " ASC");

        while (sendFormManagerCursor.moveToNext()) {
            mEditedFormList.add(sendFormManagerCursor.getLong(sendFormManagerCursor.getColumnIndex(InstanceColumns._ID)));
        }
        if (mEditedFormList.size() > 0) {
            if (mDeleteInstancesTask == null) {
                mDeleteInstancesTask = new DeleteInstancesTask();
                mDeleteInstancesTask.setContentResolver(getContentResolver());
                mDeleteInstancesTask.setDeleteListener(this);
                mDeleteInstancesTask.execute(mEditedFormList.toArray(new Long[mEditedFormList
                        .size()]));
            } else {
                Toast.makeText(this, getString(R.string.file_delete_in_progress),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            logout();
        }
    }


    @Override
    public void deleteComplete(int deletedInstances) {
        Collect.getInstance().getActivityLogger().logAction(this, "deleteComplete", Integer.toString(deletedInstances));
        if (deletedInstances == mEditedFormList.size()) {
            mDeleteInstancesTask = null;
            mEditedFormList.clear();
            deleteLocalForms();
            logout();
        } else {
            // had some failures
            Log.e(t, "Failed to delete "
                    + (mLocalFormList.size() - deletedInstances) + " instances");
            /*Toast.makeText(
                    this,
                    getString(R.string.file_deleted_error, mEditedFormList.size()
                            - deletedInstances, mEditedFormList.size()),
                    Toast.LENGTH_LONG).show();*/
        }
    }


    private void deleteLocalForms() {

        String sortOrder = FormsProviderAPI.FormsColumns.DISPLAY_NAME + " ASC, " + FormsProviderAPI.FormsColumns.JR_VERSION + " DESC";
        Cursor formManagerCursor = managedQuery(FormsProviderAPI.FormsColumns.CONTENT_URI, null, null, null, sortOrder);

        while (formManagerCursor.moveToNext()) {
            mLocalFormList.add(formManagerCursor.getLong(formManagerCursor.getColumnIndex(FormsProviderAPI.FormsColumns._ID)));
        }

        if (mDeleteFormsTask == null) {
            mDeleteFormsTask = new DeleteFormsTask();
            mDeleteFormsTask.setContentResolver(getContentResolver());
            mDeleteFormsTask.setDeleteListener(this);
            mDeleteFormsTask.execute(mLocalFormList
                    .toArray(new Long[mLocalFormList.size()]));
        }
    }

    @Override
    public void deleteDownloadedForms(int deletedForms) {
        mDeleteFormsTask = null;
        mLocalFormList.clear();
        deleteSelectedInstances();
        logout();
    }

    private void logout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Editor edit = preferences.edit();
        edit.putString(PreferencesActivity.KEY_TOKEN, "");
        edit.apply();
        startActivity(new Intent(MainMenuActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void fetchUserGroupDataCompleteListener(String result) {
        if (result.contains(CSV_HEADER)) {
            convertJsonToCsv(result, CSV_HEADER);
        } else if (result.contains("message")) {
            Toast.makeText(this, "User group data not available", Toast.LENGTH_LONG).show();
        } else { //TODO: Need to hard code the data if local server is not available
            convertJsonToCsv(jsonSample, CSV_HEADER);
        }

        mProgressDialog.dismiss();
    }

    private void convertJsonToCsv(String jsonData, String header) {
        try {
            JSONObject output = new JSONObject(jsonData);
            JSONArray docs = output.getJSONArray(header);
            String csv = CDL.toString(docs);
            writeToFile(Collect.ODK_ROOT, csv);
        } catch (FileNotFoundException e) {
            Log.e(t, "---> " + e.getLocalizedMessage());
        } catch (JSONException ex) {
            Log.e(t, "---> " + ex.getMessage());
        }
    }

    private void writeToFile(String filePath, String inputData) throws FileNotFoundException {
        File folder = new File(filePath);
        String fileName = TextUtils.CSV_FILE;
        if (!folder.exists()) {
            folder.mkdir();
        }
        File tmpFile = new File(filePath, fileName);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(tmpFile));
            writer.write(inputData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(writer);
        }
    }

    private void close(BufferedWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String jsonSample = "{\n" +
            "  \"status\": 200,\n" +
            "  \"Groups\": [\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"pirbhavti_chaundoli\",\n" +
            "          \"district\": \"\",\n" +
            "          \"equipmentname\": \"ACE_14005\",\n" +
            "          \"facility Id\": \"57dbf2f524476c5d1c95d451\",\n" +
            "          \"facilityname\": \"new some demo\",\n" +
            "          \"groupname\": \"ACE\",\n" +
            "          \"imei\": \"869336019635919\",\n" +
            "          \"equipment Id\": \"5708495bec1ff25b8427aaaa\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"Notarpalli\",\n" +
            "          \"equipmentname\": \"Rambha Padhan_321604\",\n" +
            "          \"facility Id\": \"57084aa3ec1ff25b8427b1cc\",\n" +
            "          \"facilityname\": \"Rebati Padhan_258503\",\n" +
            "          \"groupname\": \"Biolite\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"57084a94ec1ff25b8427b149\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"Rebati Padhan_Notarpalli\",\n" +
            "          \"district\": \"Notarpalli\",\n" +
            "          \"equipmentname\": \"Rebati Padhan_258503\",\n" +
            "          \"facility Id\": \"57084aa3ec1ff25b8427b1cc\",\n" +
            "          \"facilityname\": \"Rebati Padhan_258503\",\n" +
            "          \"groupname\": \"Biolite\",\n" +
            "          \"imei\": \"911337059258503\",\n" +
            "          \"equipment Id\": \"57084aa3ec1ff25b8427b1cd\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"chaundoli\",\n" +
            "          \"equipmentname\": \"ct phone swap fahim_691399\",\n" +
            "          \"facility Id\": \"570849c7ec1ff25b8427ad6e\",\n" +
            "          \"facilityname\": \"ramadhar_635703\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"57084969ec1ff25b8427ab21\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"chaundoli\",\n" +
            "          \"equipmentname\": \"ct phone swap sandeep_693171\",\n" +
            "          \"facility Id\": \"570849ccec1ff25b8427ad90\",\n" +
            "          \"facilityname\": \"pirbhavti_635919\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"5708496aec1ff25b8427ab2d\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"azamgarh\",\n" +
            "          \"equipmentname\": \"solar test 20 watt anita_633195\",\n" +
            "          \"facility Id\": \"570849a0ec1ff25b8427ac76\",\n" +
            "          \"facilityname\": \"solar test 20 watt anita_633195\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"570849a0ec1ff25b8427ac77\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"\",\n" +
            "          \"equipmentname\": \"taiwan modified test bihari_634029\",\n" +
            "          \"facility Id\": \"570849b0ec1ff25b8427ace2\",\n" +
            "          \"facilityname\": \"taiwan modified test bihari_634029\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"570849b0ec1ff25b8427ace3\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"azamgarh\",\n" +
            "          \"equipmentname\": \"ramadhar_635703\",\n" +
            "          \"facility Id\": \"570849a0ec1ff25b8427ac76\",\n" +
            "          \"facilityname\": \"solar test 20 watt anita_633195\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"570849c7ec1ff25b8427ad6f\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"chaundoli\",\n" +
            "          \"equipmentname\": \"pirbhavti_635919\",\n" +
            "          \"facility Id\": \"570849ccec1ff25b8427ad90\",\n" +
            "          \"facilityname\": \"pirbhavti_635919\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"570849ccec1ff25b8427ad91\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            ",\n" +
            "\"Groups\": [\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"pirbhavti_chaundoli\",\n" +
            "          \"district\": \"\",\n" +
            "          \"equipmentname\": \"ACE_14005\",\n" +
            "          \"facility Id\": \"57dbf2f524476c5d1c95d451\",\n" +
            "          \"facilityname\": \"new some demo\",\n" +
            "          \"groupname\": \"ACE\",\n" +
            "          \"imei\": \"869336019635919\",\n" +
            "          \"equipment Id\": \"5708495bec1ff25b8427aaaa\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"Notarpalli\",\n" +
            "          \"equipmentname\": \"Rambha Padhan_321604\",\n" +
            "          \"facility Id\": \"57084aa3ec1ff25b8427b1cc\",\n" +
            "          \"facilityname\": \"Rebati Padhan_258503\",\n" +
            "          \"groupname\": \"Biolite\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"57084a94ec1ff25b8427b149\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"Rebati Padhan_Notarpalli\",\n" +
            "          \"district\": \"Notarpalli\",\n" +
            "          \"equipmentname\": \"Rebati Padhan_258503\",\n" +
            "          \"facility Id\": \"57084aa3ec1ff25b8427b1cc\",\n" +
            "          \"facilityname\": \"Rebati Padhan_258503\",\n" +
            "          \"groupname\": \"Biolite\",\n" +
            "          \"imei\": \"911337059258503\",\n" +
            "          \"equipment Id\": \"57084aa3ec1ff25b8427b1cd\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"chaundoli\",\n" +
            "          \"equipmentname\": \"ct phone swap fahim_691399\",\n" +
            "          \"facility Id\": \"570849c7ec1ff25b8427ad6e\",\n" +
            "          \"facilityname\": \"ramadhar_635703\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"57084969ec1ff25b8427ab21\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"chaundoli\",\n" +
            "          \"equipmentname\": \"ct phone swap sandeep_693171\",\n" +
            "          \"facility Id\": \"570849ccec1ff25b8427ad90\",\n" +
            "          \"facilityname\": \"pirbhavti_635919\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"5708496aec1ff25b8427ab2d\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"azamgarh\",\n" +
            "          \"equipmentname\": \"solar test 20 watt anita_633195\",\n" +
            "          \"facility Id\": \"570849a0ec1ff25b8427ac76\",\n" +
            "          \"facilityname\": \"solar test 20 watt anita_633195\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"570849a0ec1ff25b8427ac77\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"\",\n" +
            "          \"equipmentname\": \"taiwan modified test bihari_634029\",\n" +
            "          \"facility Id\": \"570849b0ec1ff25b8427ace2\",\n" +
            "          \"facilityname\": \"taiwan modified test bihari_634029\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"570849b0ec1ff25b8427ace3\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"azamgarh\",\n" +
            "          \"equipmentname\": \"ramadhar_635703\",\n" +
            "          \"facility Id\": \"570849a0ec1ff25b8427ac76\",\n" +
            "          \"facilityname\": \"solar test 20 watt anita_633195\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"570849c7ec1ff25b8427ad6f\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"equipments\": [\n" +
            "        {\n" +
            "          \"devicename\": \"None\",\n" +
            "          \"district\": \"chaundoli\",\n" +
            "          \"equipmentname\": \"pirbhavti_635919\",\n" +
            "          \"facility Id\": \"570849ccec1ff25b8427ad90\",\n" +
            "          \"facilityname\": \"pirbhavti_635919\",\n" +
            "          \"groupname\": \"fieldtesting\",\n" +
            "          \"imei\": \"Unknown\",\n" +
            "          \"equipment Id\": \"570849ccec1ff25b8427ad91\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
