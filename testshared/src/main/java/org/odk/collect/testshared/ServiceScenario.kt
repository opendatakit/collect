package org.odk.collect.testshared

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ServiceController
import java.util.function.Consumer

/**
 * Mimics [ActivityScenario]/[FragmentScenario] to provide a way to test [Service] in a similar
 * manner. This should allow a [Service] to be tested without explicitly depending on Robolectric.
 */
class ServiceScenario<T : Service?>(private val serviceController: ServiceController<T>) {

    fun startWithNewIntent(intent: Intent): ServiceScenario<T> {
        serviceController.withIntent(intent)
            .startCommand(0, 0)

        return this
    }

    fun moveToState(state: Lifecycle.State) {
        if (state == Lifecycle.State.DESTROYED) {
            serviceController.destroy()
        } else {
            TODO()
        }
    }

    fun getState(): Lifecycle.State {
        return if (shadowOf(serviceController.get()).isStoppedBySelf) {
            Lifecycle.State.DESTROYED
        } else {
            TODO()
        }
    }

    fun onService(action: Consumer<T>) {
        action.accept(serviceController.get())
    }

    fun getForegroundNotification(): NotificationDetails? {
        val shadowService = shadowOf(serviceController.get())
        return if (shadowService.isLastForegroundNotificationAttached) {
            val notificationId = shadowService.lastForegroundNotificationId
            val notificationManager = serviceController.get()!!
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val shadowNotificationManager = shadowOf(notificationManager)

            val notification = shadowNotificationManager.getNotification(notificationId)
            NotificationDetails(notification)
        } else {
            null
        }
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun <T : Service> launch(
            serviceClass: Class<T>,
            intent: Intent? = null
        ): ServiceScenario<T> {
            return ServiceScenario(
                Robolectric.buildService(serviceClass, intent)
                    .create()
                    .startCommand(0, 0)
            )
        }
    }
}
