package org.odk.collect.android.preferences.screens

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.preference.CheckBoxPreference
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.preferences.FormUpdateMode
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.shared.Settings

@RunWith(AndroidJUnit4::class)
class MainMenuAccessPreferencesTest {
    private lateinit var context: Context
    private lateinit var generalSettings: Settings
    private lateinit var adminSettings: Settings

    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()
        context = ApplicationProvider.getApplicationContext()
        generalSettings = TestSettingsProvider.getGeneralSettings()
        adminSettings = TestSettingsProvider.getAdminSettings()
    }

    @Test
    fun `Edit Saved Form option should be checked if it's enabled in settings`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_EDIT_SAVED)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `Edit Saved Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(AdminKeys.KEY_EDIT_SAVED, false)

        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_EDIT_SAVED)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the Edit Saved Form option should change its state`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_EDIT_SAVED)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the Edit Saved Form option`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_EDIT_SAVED)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `Edit Saved Form option should be disabled if editing forms is disabled`() {
        adminSettings.save(AdminKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM, false)

        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_EDIT_SAVED)!!.isEnabled, `is`(false))
        }
    }

    @Test
    fun `Send Finalized Form option should be checked if it's enabled in settings`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_SEND_FINALIZED)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `Send Finalized Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(AdminKeys.KEY_SEND_FINALIZED, false)

        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_SEND_FINALIZED)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the Send Finalized Form option should change its state`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_SEND_FINALIZED)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the Send Finalized Form option`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_SEND_FINALIZED)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `View Sent Form option should be checked if it's enabled in settings`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_VIEW_SENT)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `View Sent Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(AdminKeys.KEY_VIEW_SENT, false)

        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_VIEW_SENT)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the View Sent Form option should change its state`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_VIEW_SENT)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the View Sent Form option`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_VIEW_SENT)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `Get Blank Form option should be checked if it's enabled in settings`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_GET_BLANK)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `Get Blank Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(AdminKeys.KEY_GET_BLANK, false)

        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_GET_BLANK)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the Get Blank Form option should change its state`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_GET_BLANK)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the Get Blank Form option`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_GET_BLANK)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `Delete Saved Form option should be checked if it's enabled in settings`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_DELETE_SAVED)!!.isChecked, `is`(true))
        }
    }

    @Test
    fun `Delete Saved Form option should be unchecked if it's disabled in settings`() {
        adminSettings.save(AdminKeys.KEY_DELETE_SAVED, false)

        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_DELETE_SAVED)!!.isChecked, `is`(false))
        }
    }

    @Test
    fun `Clicking on the Delete Saved Form option should change its state`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_DELETE_SAVED)
            assertThat(option!!.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))

            option.performClick()
            assertThat(option.isChecked, `is`(true))
        }
    }

    @Test
    fun `Fragment recreation should not change the state of the Delete Saved Form option`() {
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_DELETE_SAVED)
            assertThat(option!!.isChecked, `is`(true))
            scenario.recreate()
            assertThat(option.isChecked, `is`(true))

            option.performClick()
            assertThat(option.isChecked, `is`(false))
            scenario.recreate()
            assertThat(option.isChecked, `is`(false))
        }
    }

    @Test
    fun `When match exactly enabled shows the Get Blank Form option as unchecked and disabled`() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))

        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)

        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            val option = fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_GET_BLANK)
            assertThat(option!!.isEnabled, `is`(false))
            assertThat(option.isChecked, `is`(false))
            assertThat(adminSettings.getBoolean(AdminKeys.KEY_GET_BLANK), `is`(true))
        }
    }

    @Test
    fun `When match exactly enabled and google used as protocol the Get Blank Form option is enabled`() {
        generalSettings.save(GeneralKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))

        generalSettings.save(GeneralKeys.KEY_PROTOCOL, GeneralKeys.PROTOCOL_GOOGLE_SHEETS)
        val scenario = FragmentScenario.launch(MainMenuAccessPreferencesFragment::class.java)
        scenario.onFragment { fragment: MainMenuAccessPreferencesFragment ->
            assertThat(fragment.findPreference<CheckBoxPreference>(AdminKeys.KEY_GET_BLANK)!!.isEnabled, `is`(true))
        }
    }
}
