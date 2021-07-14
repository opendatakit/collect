package org.odk.collect.androidshared.ui

import android.app.Activity
import android.os.Bundle

/**
 * This Activity will close as soon as it is started. This means it can be used as the content
 * intent of a notification so clicking it will effectively return to the screen the user
 * was last on (knowing what that Activity was).
 */
class ReturnToAppActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
