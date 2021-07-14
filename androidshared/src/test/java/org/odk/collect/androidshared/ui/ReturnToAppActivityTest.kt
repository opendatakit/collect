package org.odk.collect.androidshared.ui

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReturnToAppActivityTest {

    @Test
    fun finishesImmediately() {
        val scenario = ActivityScenario.launch(ReturnToAppActivity::class.java)
        assertThat(scenario.state, equalTo(Lifecycle.State.DESTROYED))
    }
}
