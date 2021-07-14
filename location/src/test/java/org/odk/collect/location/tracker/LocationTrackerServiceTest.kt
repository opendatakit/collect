package org.odk.collect.location.tracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.ui.ReturnToAppActivity
import org.odk.collect.testshared.ServiceScenario

@RunWith(AndroidJUnit4::class)
class LocationTrackerServiceTest {

    @Test
    fun onStartCommand_startsServiceInForeground() {
        val service = ServiceScenario.launch(LocationTrackerService::class.java)
        assertThat(service.getForegroundNotification(), notNullValue())
    }

    @Test
    fun clickingForegroundNotification_navigatesBackToApp() {
        val service = ServiceScenario.launch(LocationTrackerService::class.java)
        val intent = service.getForegroundNotification()!!.contentIntent
        assertThat(intent.component?.className, equalTo(ReturnToAppActivity::class.qualifiedName))
    }
}
