package org.odk.collect.android.preferences.screens

import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.ViewModel
import androidx.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.ProjectPreferencesViewModel
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.odk.collect.shared.Settings

@RunWith(AndroidJUnit4::class)
class UserInterfacePreferencesFragmentTest {
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
        generalSettings = TestSettingsProvider.getGeneralSettings()
        adminSettings = TestSettingsProvider.getAdminSettings()
    }

    @Test
    fun `Enabled preferences should be visible in Locked mode`() {
        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in Locked mode`() {
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, false)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateLocked()

        val scenario = FragmentScenario.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference(GeneralKeys.KEY_APP_THEME), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_APP_LANGUAGE), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_FONT_SIZE), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_NAVIGATION), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_SHOW_SPLASH), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_SPLASH_PATH), nullValue())
        }
    }

    @Test
    fun `Enabled preferences should be visible in Unlocked mode`() {
        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be visible in Unlocked mode`() {
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, false)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateUnlocked()

        val scenario = FragmentScenario.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Enabled preferences should be visible in NotProtected mode`() {
        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_APP_THEME)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_APP_LANGUAGE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_FONT_SIZE)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_NAVIGATION)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_SHOW_SPLASH)!!.isVisible, `is`(true))
            assertThat(fragment.findPreference<Preference>(GeneralKeys.KEY_SPLASH_PATH)!!.isVisible, `is`(true))
        }
    }

    @Test
    fun `Disabled preferences should be hidden in NotProtected mode`() {
        adminSettings.save(AdminKeys.KEY_APP_THEME, false)
        adminSettings.save(AdminKeys.KEY_APP_LANGUAGE, false)
        adminSettings.save(AdminKeys.KEY_CHANGE_FONT_SIZE, false)
        adminSettings.save(AdminKeys.KEY_NAVIGATION, false)
        adminSettings.save(AdminKeys.KEY_SHOW_SPLASH_SCREEN, false)

        projectPreferencesViewModel.setStateNotProtected()

        val scenario = FragmentScenario.launch(UserInterfacePreferencesFragment::class.java)
        scenario.onFragment { fragment: UserInterfacePreferencesFragment ->
            assertThat(fragment.findPreference(GeneralKeys.KEY_APP_THEME), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_APP_LANGUAGE), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_FONT_SIZE), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_NAVIGATION), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_SHOW_SPLASH), nullValue())
            assertThat(fragment.findPreference(GeneralKeys.KEY_SPLASH_PATH), nullValue())
        }
    }
}
