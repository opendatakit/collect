package org.odk.collect.android.preferences.dialogs

import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.TestActivityScenario
import org.odk.collect.android.utilities.AdminPasswordProvider
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class AdminPasswordDialogFragmentTest {
    @Before
    fun setup() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesAdminPasswordProvider(settingsProvider: SettingsProvider): AdminPasswordProvider {
                return StubAdminPasswordProvider()
            }
        })
    }

    @Test
    fun `Entering correct password sets isPasswordCorrect value in view model to true`() {
        val activityScenario = TestActivityScenario.launch(
            SpyAdminPasswordDialogCallbackActivity::class.java
        )
        activityScenario.onActivity { activity: SpyAdminPasswordDialogCallbackActivity ->
            val fragment = AdminPasswordDialogFragment()
            fragment.show(activity.supportFragmentManager, "tag")
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            fragment.binding.editText.setText("password")
            (fragment.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertThat(activity.isPasswordCorrect, equalTo(true))
        }
    }

    @Test
    fun `Entering incorrect password sets isPasswordCorrect value in view model to false`() {
        val activityScenario = TestActivityScenario.launch(
            SpyAdminPasswordDialogCallbackActivity::class.java
        )
        activityScenario.onActivity { activity: SpyAdminPasswordDialogCallbackActivity ->
            val fragment = AdminPasswordDialogFragment()
            fragment.show(activity.supportFragmentManager, "tag")
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            fragment.binding.editText.setText("not the password")
            (fragment.dialog as AlertDialog?)!!.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertThat(activity.isPasswordCorrect, equalTo(false))
        }
    }

    private class StubAdminPasswordProvider : AdminPasswordProvider(null) {
        override fun isAdminPasswordSet(): Boolean {
            return true
        }

        override fun getAdminPassword(): String {
            return "password"
        }
    }

    private class SpyAdminPasswordDialogCallbackActivity : FragmentActivity() {
        var isPasswordCorrect = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(R.style.Theme_AppCompat) // Needed for androidx.appcompat.app.AlertDialog

            val adminPasswordViewModel = ViewModelProvider(this).get(
                EnterAdminPasswordViewModel::class.java
            )
            adminPasswordViewModel.passwordEntered.observe(
                this,
                { isPasswordCorrect: Boolean ->
                    this.isPasswordCorrect = isPasswordCorrect
                }
            )
        }
    }
}
