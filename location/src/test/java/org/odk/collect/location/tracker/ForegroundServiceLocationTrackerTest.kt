package org.odk.collect.location.tracker

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.location.LocationListener
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.odk.collect.location.Location
import org.odk.collect.location.LocationClient
import org.odk.collect.location.LocationClientProvider
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ForegroundServiceLocationTrackerTest : LocationTrackerTest() {

    private val application: Application = ApplicationProvider.getApplicationContext()
    private val locationClient = FakeLocationClient()

    override val locationTracker: LocationTracker = ForegroundServiceLocationTracker(application)

    override fun runBackground() {
        RobolectricHelpers.runServices(true)
    }

    override fun setDeviceLocation(location: Location) {
        val androidLocation = android.location.Location("gps")
        androidLocation.latitude = location.latitude
        androidLocation.longitude = location.longitude
        androidLocation.altitude = location.altitude
        androidLocation.accuracy = location.accuracy

        locationClient.updateLocation(androidLocation)
    }

    @Before
    fun fakeLocationClient() {
        LocationClientProvider.setTestClient(locationClient)
    }

    @After
    fun teardown() {
        RobolectricHelpers.clearServices()
        LocationClientProvider.setTestClient(null)
    }
}

private class FakeLocationClient : LocationClient {

    private var started = false
    private var locationListener: LocationListener? = null
    private var locationClientListener: LocationClient.LocationClientListener? = null

    override fun start() {
        this.started = true
        locationClientListener?.onClientStart()
    }

    override fun stop() {
        this.started = false
        locationClientListener?.onClientStop()
    }

    override fun requestLocationUpdates(locationListener: LocationListener) {
        if (!started) {
            throw IllegalStateException("Can't request location updated before starting!")
        }

        this.locationListener = locationListener
    }

    override fun stopLocationUpdates() {
        TODO("Not yet implemented")
    }

    override fun setListener(locationClientListener: LocationClient.LocationClientListener?) {
        this.locationClientListener = locationClientListener
    }

    override fun setPriority(priority: LocationClient.Priority) {
        TODO("Not yet implemented")
    }

    override fun getLastLocation(): android.location.Location? {
        TODO("Not yet implemented")
    }

    override fun isLocationAvailable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isMonitoringLocation(): Boolean {
        TODO("Not yet implemented")
    }

    override fun canSetUpdateIntervals(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setUpdateIntervals(updateInterval: Long, fastestUpdateInterval: Long) {
        TODO("Not yet implemented")
    }

    fun updateLocation(location: android.location.Location) {
        if (started) {
            locationListener?.onLocationChanged(location)
        }
    }
}
