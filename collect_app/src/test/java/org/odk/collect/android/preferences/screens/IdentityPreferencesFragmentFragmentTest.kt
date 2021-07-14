package org.odk.collect.android.preferences.screens

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.ViewModel
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.ProjectPreferencesViewModel
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.shared.Settings

@RunWith(AndroidJUnit4::class)
class IdentityPreferencesFragmentFragmentTest {
    private lateinit var generalSettings: Settings
    private lateinit var adminSettings: Settings

    private val adminPasswordProvider = mock<AdminPasswordProvider> {
        on { isAdminPasswordSet } doReturn false
    }
    private val projectPreferencesViewModel = ProjectPreferencesViewModel(adminPasswordProvider)

    private val versionInformation = mock<VersionInformation>()

    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectPreferencesViewModel(adminPasswordProvider: AdminPasswordProvider): ProjectPreferencesViewModel.Factory {
                return object : ProjectPreferencesViewModel.Factory(adminPasswordProvider) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return projectPreferencesViewModel as T
                    }
                }
            }

            override fun providesVersionInformation(): VersionInformation {
                return versionInformation
            }
        })

        generalSettings = TestSettingsProvider.getGeneralSettings()
        adminSettings = TestSettingsProvider.getAdminSettings()
    }

    @Test
    fun `If 'Form Metadata' option is enabled in protected settings should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_FORM_METADATA)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Form Metadata' option is disabled in protected settings should be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA), nullValue())
        }
    }

    @Test
    fun `If 'Form Metadata' option is enabled in protected settings should be visible in Unocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_FORM_METADATA)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Form Metadata' option is disabled in protected settings should be visible in Unocked mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_FORM_METADATA)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Form Metadata' option is enabled in protected settings should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_FORM_METADATA)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Form Metadata' option is disabled in protected settings should be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_CHANGE_FORM_METADATA, false)
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference(GeneralKeys.KEY_FORM_METADATA), nullValue())
        }
    }

    @Test
    fun `If 'Collect anonymous usage data' option is enabled in protected settings should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_ANALYTICS)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Collect anonymous usage data' option is disabled in protected settings should be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_ANALYTICS, false)
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS), nullValue())
        }
    }

    @Test
    fun `If 'Collect anonymous usage data' option is enabled in protected settings should be visible in Unocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_ANALYTICS)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Collect anonymous usage data' option is disabled in protected settings should be visible in Unocked mode`() {
        adminSettings.save(AdminKeys.KEY_ANALYTICS, false)
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_ANALYTICS)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Collect anonymous usage data' option is enabled in protected settings should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_ANALYTICS)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `If 'Collect anonymous usage data' option is disabled in protected settings should be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_ANALYTICS, false)
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference(GeneralKeys.KEY_ANALYTICS), nullValue())
        }
    }

    @Test
    fun `'Collect anonymous usage data' option should be checked if it's enabled in settings`() {
        generalSettings.save(GeneralKeys.KEY_ANALYTICS, true)
        whenever(versionInformation.isBeta).thenReturn(false)

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_ANALYTICS)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `'Collect anonymous usage data' option should be unchecked if it's disabled in settings`() {
        generalSettings.save(GeneralKeys.KEY_ANALYTICS, false)
        whenever(versionInformation.isBeta).thenReturn(false)

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_ANALYTICS)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `'Collect anonymous usage data' option should be disabled in beta`() {
        whenever(versionInformation.isBeta).thenReturn(true)

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(GeneralKeys.KEY_ANALYTICS)!!.isEnabled, `is`(false))
            assertThat(fragment.findPreference<CheckBoxPreference>(GeneralKeys.KEY_ANALYTICS)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `'Collect anonymous usage data' option should be enabled if it's not beta`() {
        whenever(versionInformation.isBeta).thenReturn(false)

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(GeneralKeys.KEY_ANALYTICS)!!.isEnabled, `is`(true))
        }
    }

    @Test
    fun `Clicking on the 'Collect anonymous usage data' option should change its state`() {
        whenever(versionInformation.isBeta).thenReturn(false)

        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_ANALYTICS)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the 'Collect anonymous usage data' option`() {
        val scenario = FragmentScenario.launch(IdentityPreferencesFragment::class.java)
        scenario.onFragment { fragment: IdentityPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_ANALYTICS)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }
}
