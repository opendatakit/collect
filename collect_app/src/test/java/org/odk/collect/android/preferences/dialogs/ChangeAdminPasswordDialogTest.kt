package org.odk.collect.android.preferences.dialogs

import android.content.DialogInterface
import android.text.InputType
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.TestSettingsProvider.getAdminSettings
import org.odk.collect.android.fragments.support.DialogFragmentHelpers.DialogFragmentTestActivity
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.TestActivityScenario
import org.odk.collect.testshared.RobolectricHelpers
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
class ChangeAdminPasswordDialogTest {
    private lateinit var fragmentManager: FragmentManager
    private lateinit var dialogFragment: ChangeAdminPasswordDialog
    private val adminSettings = getAdminSettings()

    @Before
    fun setup() {
        val activity = CollectHelpers.createThemedActivity(FragmentActivity::class.java)
        fragmentManager = activity.supportFragmentManager
        dialogFragment = ChangeAdminPasswordDialog()
    }

    @Test
    fun dialogIsCancellable() {
        launchDialog(fragmentManager)
        assertThat(
            Shadows.shadowOf(dialogFragment.dialog).isCancelable,
            equalTo(true)
        )
    }

    @Test
    fun clickingOkAfterSettingPassword_setsPasswordInSharedPreferences() {
        val dialog = launchDialog(fragmentManager)
        val passwordEditText = dialog.findViewById<EditText>(R.id.pwd_field)
        passwordEditText!!.setText("blah")
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        RobolectricHelpers.runLooper()
        assertThat(
            adminSettings.getString(AdminKeys.KEY_ADMIN_PW),
            equalTo("blah")
        )
    }

    @Test
    fun whenScreenIsRotated_passwordAndCheckboxValueIsRetained() {
        val activityScenario = TestActivityScenario.launch(DialogFragmentTestActivity::class.java)
        activityScenario.onActivity { activity: DialogFragmentTestActivity ->
            val dialog = launchDialog(activity.supportFragmentManager)
            (dialog.findViewById<View>(R.id.pwd_field) as EditText?)!!.setText("blah")
            (dialog.findViewById<View>(R.id.checkBox2) as CheckBox?)!!.isChecked = true
        }
        activityScenario.recreate()
        activityScenario.onActivity { activity: DialogFragmentTestActivity ->
            val restoredFragment =
                activity.supportFragmentManager.findFragmentByTag("TAG") as ChangeAdminPasswordDialog?
            val restoredDialog = restoredFragment!!.dialog as AlertDialog?
            assertThat(
                (restoredDialog!!.findViewById<View>(R.id.pwd_field) as EditText?)!!.text.toString(),
                equalTo("blah")
            )
            assertThat(
                (restoredDialog.findViewById<View>(R.id.checkBox2) as CheckBox?)!!.isChecked,
                equalTo(true)
            )
        }
    }

    @Test
    fun clickingOk_dismissesTheDialog() {
        val dialog = launchDialog(fragmentManager)
        assertTrue(dialog.isShowing)
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        RobolectricHelpers.runLooper()
        assertFalse(dialog.isShowing)
        assertTrue(Shadows.shadowOf(dialog).hasBeenDismissed())
    }

    @Test
    fun clickingCancel_dismissesTheDialog() {
        val dialog = launchDialog(fragmentManager)
        assertTrue(dialog.isShowing)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
        RobolectricHelpers.runLooper()
        assertFalse(dialog.isShowing)
        assertTrue(Shadows.shadowOf(dialog).hasBeenDismissed())
    }

    @Test
    fun checkingShowPassword_displaysPasswordAsText() {
        val dialog = launchDialog(fragmentManager)
        val passwordEditText = dialog.findViewById<EditText>(R.id.pwd_field)
        val passwordCheckBox = dialog.findViewById<CheckBox>(R.id.checkBox2)
        passwordCheckBox!!.isChecked = true
        assertThat(
            passwordEditText!!.inputType,
            equalTo(InputType.TYPE_TEXT_VARIATION_PASSWORD)
        )
    }

    @Test
    fun uncheckingShowPassword_displaysPasswordAsPassword() {
        val dialog = launchDialog(fragmentManager)
        val passwordEditText = dialog.findViewById<EditText>(R.id.pwd_field)
        val passwordCheckBox = dialog.findViewById<CheckBox>(R.id.checkBox2)
        passwordCheckBox!!.isChecked = false
        assertThat(
            passwordEditText!!.inputType,
            equalTo(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        )
    }

    private fun launchDialog(fragmentManager: FragmentManager?): AlertDialog {
        dialogFragment.show(fragmentManager!!, "TAG")
        RobolectricHelpers.runLooper()
        return ShadowDialog.getLatestDialog() as AlertDialog
    }
}
