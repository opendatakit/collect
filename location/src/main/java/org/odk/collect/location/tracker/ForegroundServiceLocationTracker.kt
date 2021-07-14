package org.odk.collect.location.tracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.GoogleApiAvailability
import org.odk.collect.androidshared.ui.ReturnToAppActivity
import org.odk.collect.location.GoogleFusedLocationClient
import org.odk.collect.location.Location
import org.odk.collect.location.LocationClient
import org.odk.collect.location.LocationClientProvider
import org.odk.collect.location.R
import org.odk.collect.location.tracker.ForegroundServiceLocationTracker.Companion.notificationIcon
import org.odk.collect.strings.getLocalizedString

private var location: Location? = null

class ForegroundServiceLocationTracker(private val application: Application) : LocationTracker {

    init {
        location = null // Clear static location for new instance
    }

    override fun getCurrentLocation(): Location? {
        return location
    }

    override fun start() {
        application.startService(Intent(application, LocationTrackerService::class.java))
    }

    override fun stop() {
        application.stopService(Intent(application, LocationTrackerService::class.java))
    }

    companion object {

        @JvmStatic
        var notificationIcon: Int? = null
    }
}

class LocationTrackerService : Service() {

    private val locationClient: LocationClient by lazy {
        LocationClientProvider.getClient(
            this,
            { GoogleFusedLocationClient(application) },
            GoogleApiAvailability.getInstance()
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )

        locationClient.setListener(object : LocationClient.LocationClientListener {
            override fun onClientStart() {
                locationClient.requestLocationUpdates {
                    location = Location(it.latitude, it.longitude, it.altitude, it.accuracy)
                }
            }

            override fun onClientStartFailure() {
                // Ignored
            }

            override fun onClientStop() {
                // Ignored
            }
        })

        locationClient.start()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        locationClient.stop()
    }

    private fun createNotification() = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
        .setContentTitle(getLocalizedString(R.string.location_tracking_notification_title))
        .setSmallIcon(notificationIcon ?: R.drawable.ic_baseline_location_searching_24)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(createNotificationIntent())
        .build()

    private fun createNotificationIntent() =
        PendingIntent.getActivity(this, 0, Intent(this, ReturnToAppActivity::class.java), 0)

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                getLocalizedString(R.string.location_tracking_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                notificationChannel
            )
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL = "location_tracking"
    }
}
