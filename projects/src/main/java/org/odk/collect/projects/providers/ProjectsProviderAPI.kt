package org.odk.collect.projects.providers

import android.net.Uri

object ProjectsProviderAPI {
    const val AUTHORITY = "org.odk.collect.android.provider.odk.projects"

    val CONTENT_URI = Uri.parse("content://$AUTHORITY/projects")

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.project"

    // column names
    const val PROJECT_UUID = "uuid"

    const val PROJECT_NAME = "name"
}
