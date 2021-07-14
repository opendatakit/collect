package org.odk.collect.location.tracker

import org.odk.collect.location.Location

/**
 * Provides a way to track the location of a device.
 */
interface LocationTracker {

    /**
     * The last location tracked. Will return `null` if a location hasn't been determined
     * or [LocationTracker.start] hasn't been called yet.
     */
    fun getCurrentLocation(): Location?

    /**
     * Starts tracking the device's location at a fixed interval.
     */
    fun start()

    /**
     * Stops tracking location. Does not reset the value returned by [LocationTracker.getCurrentLocation].
     */
    fun stop()
}
