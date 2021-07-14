package org.odk.collect.location.tracker

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.location.Location

abstract class LocationTrackerTest {

    abstract val locationTracker: LocationTracker

    abstract fun runBackground()
    abstract fun setDeviceLocation(location: Location)

    @Test
    fun updatingDeviceLocation_beforeStarting_doesNothing() {
        setDeviceLocation(Location(1.0, 2.0, 3.0, 4.0f))
        runBackground()

        assertThat(locationTracker.getCurrentLocation(), equalTo(null))
    }

    @Test
    fun updatingDeviceLocation_whenStarted_updatesCurrentLocation() {
        locationTracker.start()
        runBackground()

        val location = Location(1.0, 2.0, 3.0, 4.0f)
        setDeviceLocation(location)
        assertThat(locationTracker.getCurrentLocation(), equalTo(location))
    }

    @Test
    fun updatingDeviceLocation_whenStopped_doesNothing() {
        locationTracker.start()
        locationTracker.stop()
        runBackground()

        setDeviceLocation(Location(1.0, 2.0, 3.0, 4.0f))
        assertThat(locationTracker.getCurrentLocation(), equalTo(null))
    }
}
