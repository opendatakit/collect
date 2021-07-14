/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.preferences.screens

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment
import org.odk.collect.android.preferences.dialogs.ChangeAdminPasswordDialog
import org.odk.collect.android.preferences.dialogs.ChangeAdminPasswordViewModel
import org.odk.collect.android.preferences.dialogs.EnterAdminPasswordViewModel
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.utilities.DialogUtils
import org.odk.collect.android.utilities.MultiClickGuard
import org.odk.collect.android.utilities.ToastUtils
import org.odk.collect.android.version.VersionInformation
import javax.inject.Inject

class ProjectPreferencesFragment :
    BaseProjectPreferencesFragment(),
    Preference.OnPreferenceClickListener {

    @Inject
    lateinit var versionInformation: VersionInformation

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        setHasOptionsMenu(true)

        val changeAdminPasswordViewModel = ViewModelProvider(requireActivity()).get(
            ChangeAdminPasswordViewModel::class.java
        )
        changeAdminPasswordViewModel.passwordSet.observe(
            this,
            { isPasswordSet: Boolean ->
                if (isPasswordSet) {
                    projectPreferencesViewModel.setStateUnlocked()
                    recreatePreferences()
                } else {
                    projectPreferencesViewModel.setStateNotProtected()
                    removeDisabledPrefs()
                }
                requireActivity().invalidateOptionsMenu()
            }
        )

        val enterAdminPasswordViewModel = ViewModelProvider(requireActivity()).get(
            EnterAdminPasswordViewModel::class.java
        )
        enterAdminPasswordViewModel.passwordEntered.observe(
            this,
            { isPasswordCorrect: Boolean ->
                if (isPasswordCorrect) {
                    projectPreferencesViewModel.setStateUnlocked()
                    requireActivity().invalidateOptionsMenu()
                    recreatePreferences()
                } else {
                    ToastUtils.showShortToast(R.string.admin_password_incorrect)
                }
            }
        )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.project_preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        recreatePreferences()
    }

    private fun recreatePreferences() {
        preferenceScreen = null
        addPreferencesFromResource(R.xml.project_preferences)

        findPreference<Preference>(PROTOCOL_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(PROJECT_DISPLAY_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(USER_INTERFACE_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(MAPS_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(FORM_MANAGEMENT_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(USER_AND_DEVICE_IDENTITY_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(EXPERIMENTAL_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(AdminKeys.KEY_CHANGE_ADMIN_PASSWORD)!!.onPreferenceClickListener = this
        findPreference<Preference>(PROJECT_MANAGEMENT_PREFERENCE_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(ACCESS_CONTROL_PREFERENCE_KEY)!!.onPreferenceClickListener = this

        if (versionInformation.isRelease) {
            findPreference<Preference>(EXPERIMENTAL_PREFERENCE_KEY)!!.isVisible = false
        }

        setPreferencesVisibility()
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                PROTOCOL_PREFERENCE_KEY -> displayPreferences(ServerPreferencesFragment())
                PROJECT_DISPLAY_PREFERENCE_KEY -> displayPreferences(ProjectDisplayPreferencesFragment())
                USER_INTERFACE_PREFERENCE_KEY -> displayPreferences(UserInterfacePreferencesFragment())
                MAPS_PREFERENCE_KEY -> displayPreferences(MapsPreferencesFragment())
                FORM_MANAGEMENT_PREFERENCE_KEY -> displayPreferences(FormManagementPreferencesFragment())
                USER_AND_DEVICE_IDENTITY_PREFERENCE_KEY -> displayPreferences(IdentityPreferencesFragment())
                EXPERIMENTAL_PREFERENCE_KEY -> displayPreferences(ExperimentalPreferencesFragment())
                AdminKeys.KEY_CHANGE_ADMIN_PASSWORD -> {
                    if (projectPreferencesViewModel.isStateLocked()) {
                        DialogUtils.showIfNotShowing(AdminPasswordDialogFragment::class.java, requireActivity().supportFragmentManager)
                    } else {
                        DialogUtils.showIfNotShowing(
                            ChangeAdminPasswordDialog::class.java, requireActivity().supportFragmentManager
                        )
                    }
                }
                PROJECT_MANAGEMENT_PREFERENCE_KEY -> {
                    if (projectPreferencesViewModel.isStateLocked()) {
                        DialogUtils.showIfNotShowing(AdminPasswordDialogFragment::class.java, requireActivity().supportFragmentManager)
                    } else {
                        displayPreferences(ProjectManagementPreferencesFragment())
                    }
                }
                ACCESS_CONTROL_PREFERENCE_KEY -> {
                    if (projectPreferencesViewModel.isStateLocked()) {
                        DialogUtils.showIfNotShowing(AdminPasswordDialogFragment::class.java, requireActivity().supportFragmentManager)
                    } else {
                        displayPreferences(AccessControlPreferencesFragment())
                    }
                }
            }
            return true
        }
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        when {
            projectPreferencesViewModel.isStateLocked() -> {
                menu.findItem(R.id.menu_locked).isVisible = true
                menu.findItem(R.id.menu_unlocked).isVisible = false
            }
            projectPreferencesViewModel.isStateUnlocked() -> {
                menu.findItem(R.id.menu_locked).isVisible = false
                menu.findItem(R.id.menu_unlocked).isVisible = true
            }
            else -> {
                menu.findItem(R.id.menu_locked).isVisible = false
                menu.findItem(R.id.menu_unlocked).isVisible = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.project_preferences_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_locked) {
            DialogUtils.showIfNotShowing(AdminPasswordDialogFragment::class.java, requireActivity().supportFragmentManager)
            return true
        }
        return false
    }

    private fun displayPreferences(fragment: Fragment?) {
        if (fragment != null) {
            fragment.arguments = arguments
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.preferences_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    fun preventOtherWaysOfEditingForm() {
        val fragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.preferences_fragment_container) as FormEntryAccessPreferencesFragment
        fragment.preventOtherWaysOfEditingForm()
    }

    private fun setPreferencesVisibility() {
        if (projectPreferencesViewModel.isStateUnlocked()) {
            return
        }

        val preferenceScreen = preferenceScreen
        if (!hasAtLeastOneSettingEnabled(AdminKeys.serverKeys)) {
            preferenceScreen.removePreference(findPreference("protocol"))
        }
        if (!hasAtLeastOneSettingEnabled(AdminKeys.userInterfaceKeys)) {
            preferenceScreen.removePreference(findPreference("user_interface"))
        }
        if (!hasAtLeastOneSettingEnabled(listOf(AdminKeys.KEY_MAPS))) {
            preferenceScreen.removePreference(findPreference("maps"))
        }
        if (!hasAtLeastOneSettingEnabled(AdminKeys.formManagementKeys)) {
            preferenceScreen.removePreference(findPreference("form_management"))
        }
        if (!hasAtLeastOneSettingEnabled(AdminKeys.identityKeys)) {
            preferenceScreen.removePreference(findPreference("user_and_device_identity"))
        }
    }

    private fun hasAtLeastOneSettingEnabled(keys: Collection<String>): Boolean {
        for (key in keys) {
            val value = settingsProvider.getAdminSettings().getBoolean(key)
            if (value) {
                return true
            }
        }
        return false
    }

    companion object {
        private const val PROTOCOL_PREFERENCE_KEY = "protocol"
        private const val PROJECT_DISPLAY_PREFERENCE_KEY = "project_display"
        private const val USER_INTERFACE_PREFERENCE_KEY = "user_interface"
        private const val MAPS_PREFERENCE_KEY = "maps"
        private const val FORM_MANAGEMENT_PREFERENCE_KEY = "form_management"
        private const val USER_AND_DEVICE_IDENTITY_PREFERENCE_KEY = "user_and_device_identity"
        private const val EXPERIMENTAL_PREFERENCE_KEY = "experimental"
        private const val PROJECT_MANAGEMENT_PREFERENCE_KEY = "project_management"
        private const val ACCESS_CONTROL_PREFERENCE_KEY = "access_control"
    }
}
