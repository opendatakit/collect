package org.odk.collect.android.preferences.screens

import android.content.Context
import android.os.Looper
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.ViewModel
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.FormUpdateMode
import org.odk.collect.android.preferences.ProjectPreferencesViewModel
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.shared.Settings
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class FormManagementPreferencesFragmentTest {
    private lateinit var context: Context
    private lateinit var generalSettings: Settings
    private lateinit var adminSettings: Settings

    private val adminPasswordProvider = mock<AdminPasswordProvider> {
        on { isAdminPasswordSet } doReturn false
    }
    private val projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)

    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectPreferencesViewModel(adminPasswordProvider: AdminPasswordProvider): ProjectPreferencesViewModel.Factory {
                return object : ProjectPreferencesViewModel.Factory(adminPasswordProvider) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return projectPreferencesViewModel as T
                    }
                }
            }
        })

        CollectHelpers.setupDemoProject()
        context = ApplicationProvider.getApplicationContext()
        generalSettings = TestSettingsProvider.getGeneralSettings()
        adminSettings = TestSettingsProvider.getAdminSettings()
    }

    @Test
    fun `When Google Drive used as server shows update mode as manual and disable prefs`() {
        generalSettings.save(GeneralKeys.KEY_PROTOCOL, GeneralKeys.PROTOCOL_GOOGLE_SHEETS)
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))

        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_FORM_UPDATE_MODE)!!.summary,
                `is`(context.getString(R.string.manual))
            )
            assertThat(
                generalSettings.getString(GeneralKeys.KEY_FORM_UPDATE_MODE),
                `is`(FormUpdateMode.MATCH_EXACTLY.getValue(context))
            )
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_FORM_UPDATE_MODE)!!.isEnabled,
                `is`(false)
            )
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isEnabled,
                `is`(false)
            )
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)!!.isEnabled,
                `is`(false)
            )
        }
    }

    @Test
    fun `When 'Manual Updates' enabled disables prefs`() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MANUAL.getValue(context))
        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isEnabled,
                `is`(false)
            )
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)!!.isEnabled,
                `is`(false)
            )
        }
    }

    @Test
    fun `When 'Previously Downloaded Only 'enabled disables prefs`() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY.getValue(context))
        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isEnabled,
                `is`(true)
            )
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)!!.isEnabled,
                `is`(true)
            )
        }
    }

    @Test
    fun `When 'Match Exactly' enabled disables prefs`() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))
        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isEnabled,
                `is`(true)
            )
            assertThat(
                f.findPreference<Preference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)!!.isEnabled,
                `is`(false)
            )
        }
    }

    @Test
    fun `When 'Match Exactly' enabled and 'Automatic Download' disabled shows 'Automatic Download' as checked`() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))
        generalSettings.save(GeneralKeys.KEY_AUTOMATIC_UPDATE, false)
        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            val automaticDownload = f.findPreference<CheckBoxPreference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)
            assertThat(automaticDownload!!.isChecked, `is`(true))
            assertThat(
                generalSettings.getBoolean(GeneralKeys.KEY_AUTOMATIC_UPDATE),
                `is`(false)
            )
        }
    }

    @Test
    fun `When 'Manual Updates' enabled and 'Automatic Download' enabled shows 'Automatic Download' as not checked`() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MANUAL.getValue(context))
        generalSettings.save(GeneralKeys.KEY_AUTOMATIC_UPDATE, true)
        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            val automaticDownload = f.findPreference<CheckBoxPreference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)
            assertThat(automaticDownload!!.isChecked, `is`(false))
            assertThat(
                generalSettings.getBoolean(GeneralKeys.KEY_AUTOMATIC_UPDATE),
                `is`(true)
            )
        }
    }

    @Test
    fun `When Google Drive used as server and 'Automatic Download' enabled shows 'Automatic Download' as not checked`() {
        generalSettings.save(GeneralKeys.KEY_PROTOCOL, GeneralKeys.PROTOCOL_GOOGLE_SHEETS)
        generalSettings.save(GeneralKeys.KEY_AUTOMATIC_UPDATE, true)
        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            val automaticDownload = f.findPreference<CheckBoxPreference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)
            assertThat(automaticDownload!!.isChecked, `is`(false))
            assertThat(
                generalSettings.getBoolean(GeneralKeys.KEY_AUTOMATIC_UPDATE),
                `is`(true)
            )
        }
    }

    @Test
    fun `When 'Manual Updates' enabled and 'Automatic Download' disabled setting to 'Previously Downloaded' resets 'Automatic Download'`() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MANUAL.getValue(context))
        generalSettings.save(GeneralKeys.KEY_AUTOMATIC_UPDATE, false)
        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            val updateMode = f.findPreference<ListPreference>(GeneralKeys.KEY_FORM_UPDATE_MODE)
            updateMode!!.value = FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY.getValue(context)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            val automaticDownload = f.findPreference<CheckBoxPreference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)
            assertThat(automaticDownload!!.isChecked, `is`(false))
            assertThat(
                generalSettings.getBoolean(GeneralKeys.KEY_AUTOMATIC_UPDATE),
                `is`(false)
            )
        }
    }

    @Test
    fun `Changing Form Update Mode should not cause any crash if related preferences are disabled in Admin Settings`() {
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        val scenario = FragmentScenario.launch(
            FormManagementPreferencesFragment::class.java
        )
        scenario.onFragment { f: FormManagementPreferencesFragment ->
            assertThat(
                f.findPreference(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK),
                nullValue()
            )
            assertThat(
                f.findPreference(GeneralKeys.KEY_AUTOMATIC_UPDATE),
                nullValue()
            )
            val updateMode = f.findPreference<ListPreference>(GeneralKeys.KEY_FORM_UPDATE_MODE)
            updateMode!!.value = FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY.getValue(context)
            updateMode.value = FormUpdateMode.MATCH_EXACTLY.getValue(context)
            updateMode.value = FormUpdateMode.MANUAL.getValue(context)
        }
    }

    @Test
    fun `Enabled preferences should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_AUTOSEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOMATIC_UPDATE), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOSEND), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_DELETE_AFTER_SEND), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_COMPLETED_DEFAULT), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIGH_RESOLUTION), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_IMAGE_SIZE), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_GUIDANCE_HINT), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_INSTANCE_SYNC), nullValue())
        }
    }

    @Test
    fun `Enabled preferences should be visible in Unlocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_AUTOSEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be visible in Unlocked mode`() {
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_AUTOSEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Enabled preferences should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_AUTOMATIC_UPDATE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_AUTOSEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_DELETE_AFTER_SEND)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_COMPLETED_DEFAULT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_HIGH_RESOLUTION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_IMAGE_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_GUIDANCE_HINT)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_INSTANCE_SYNC)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK, false)
        adminSettings.save(AdminKeys.KEY_AUTOMATIC_UPDATE, false)
        adminSettings.save(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS, false)
        adminSettings.save(AdminKeys.KEY_AUTOSEND, false)
        adminSettings.save(AdminKeys.KEY_DELETE_AFTER_SEND, false)
        adminSettings.save(AdminKeys.KEY_DEFAULT_TO_FINALIZED, false)
        adminSettings.save(AdminKeys.KEY_CONSTRAINT_BEHAVIOR, false)
        adminSettings.save(AdminKeys.KEY_HIGH_RESOLUTION, false)
        adminSettings.save(AdminKeys.KEY_IMAGE_SIZE, false)
        adminSettings.save(AdminKeys.KEY_GUIDANCE_HINT, false)
        adminSettings.save(AdminKeys.KEY_INSTANCE_FORM_SYNC, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(FormManagementPreferencesFragment::class.java)
        scenario.onFragment { fragment: FormManagementPreferencesFragment ->
            assertThat(fragment.findPreference(GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOMATIC_UPDATE), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_AUTOSEND), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_DELETE_AFTER_SEND), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_COMPLETED_DEFAULT), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_CONSTRAINT_BEHAVIOR), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_HIGH_RESOLUTION), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_IMAGE_SIZE), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_GUIDANCE_HINT), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_INSTANCE_SYNC), nullValue())
        }
    }
}
