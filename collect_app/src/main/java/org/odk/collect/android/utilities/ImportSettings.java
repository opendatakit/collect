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

package org.odk.collect.android.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORMLIST_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_GOOGLE_SHEETS_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SERVER_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_USERNAME;

/**
 * Created by shobhit on 12/4/17.
 */

public class ImportSettings {

    public static void fromJSON(JSONObject settingsJson) throws JSONException {

        Context context = Collect.getInstance();
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_PROTOCOL, settingsJson.getString(KEY_PROTOCOL));
        editor.putString(KEY_SERVER_URL, settingsJson.getString(KEY_SERVER_URL));
        editor.putString(KEY_GOOGLE_SHEETS_URL, settingsJson.getString(KEY_GOOGLE_SHEETS_URL));
        editor.putString(KEY_FORMLIST_URL, settingsJson.getString(KEY_FORMLIST_URL));
        editor.putString(KEY_SUBMISSION_URL, settingsJson.getString(KEY_SUBMISSION_URL));
        editor.putString(KEY_USERNAME, settingsJson.getString(KEY_USERNAME));
        editor.putString(KEY_PASSWORD, settingsJson.getString(KEY_PASSWORD));
        editor.apply();

        //settings import confirmation toast
        ToastUtils.showLongToast(context.getString(R.string.successfully_imported_settings));

    }
}
