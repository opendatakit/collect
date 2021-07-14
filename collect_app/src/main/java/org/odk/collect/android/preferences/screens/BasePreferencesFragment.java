package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.configure.SettingsChangeHandler;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.shared.Settings;
import org.odk.collect.android.preferences.source.SettingsProvider;

import javax.inject.Inject;

public abstract class BasePreferencesFragment extends PreferenceFragmentCompat implements Settings.OnSettingChangeListener {

    @Inject
    SettingsChangeHandler settingsChangeHandler;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        super.onDisplayPreferenceDialog(preference);

        // If we don't do this there is extra padding on "Cancel" and "OK" on
        // the preference dialogs. This appears to have something to with the `updateLocale`
        // calls in `CollectAbstractActivity` and weirdly only happens for English.
        DialogPreference dialogPreference = (DialogPreference) preference;
        dialogPreference.setNegativeButtonText(R.string.cancel);
        dialogPreference.setPositiveButtonText(R.string.ok);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        if (activity instanceof CollectAbstractActivity) {
            ((CollectAbstractActivity) activity).initToolbar(getPreferenceScreen().getTitle());
        }

        super.onViewCreated(view, savedInstanceState);
    }
}
