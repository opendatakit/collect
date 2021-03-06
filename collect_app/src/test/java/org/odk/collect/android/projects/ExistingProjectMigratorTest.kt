package org.odk.collect.android.projects

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.TempFiles
import java.io.File

@RunWith(AndroidJUnit4::class)
class ExistingProjectMigratorTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val component = DaggerUtils.getComponent(context)
    private val existingProjectMigrator = component.existingProjectMigrator()
    private val storagePathProvider = component.storagePathProvider()
    private val projectsRepository = component.projectsRepository()
    private val settingsProvider = component.settingsProvider()
    private val currentProjectProvider = component.currentProjectProvider()
    private val rootDir = storagePathProvider.odkRootDirPath

    @Test
    fun `creates existing project with details based on its url`() {
        PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()
            .putString(GeneralKeys.KEY_SERVER_URL, "https://my-server.com")
            .apply()

        existingProjectMigrator.run()

        assertThat(projectsRepository.getAll().size, `is`(1))

        val project = projectsRepository.getAll()[0]
        assertThat(project.name, `is`("my-server.com"))
        assertThat(project.icon, `is`("M"))
        assertThat(project.color, `is`("#53bdd4"))
    }

    @Test
    fun `moves files from root`() {
        val legacyRootDirs = listOf(
            File(rootDir, "forms"),
            File(rootDir, "instances"),
            File(rootDir, "metadata"),
            File(rootDir, "layers"),
            File(rootDir, ".cache"),
            File(rootDir, "settings")
        )

        legacyRootDirs.forEach {
            it.mkdir()
            TempFiles.createTempFile(it, "file", ".temp")
        }

        existingProjectMigrator.run()
        val existingProject = currentProjectProvider.getCurrentProject()

        legacyRootDirs.forEach {
            assertThat(it.exists(), `is`(false))
        }

        storagePathProvider.getProjectDirPaths(existingProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))
            assertThat(dir.listFiles()!!.isEmpty(), `is`(false))
        }
    }

    @Test
    fun `still copies other files if a directory is missing`() {
        val legacyRootDirsWithoutForms = listOf(
            File(rootDir, "instances"),
            File(rootDir, "metadata"),
            File(rootDir, "layers"),
            File(rootDir, ".cache"),
            File(rootDir, "settings")
        )

        legacyRootDirsWithoutForms.forEach {
            it.mkdir()
            TempFiles.createTempFile(it, "file", ".temp")
        }

        existingProjectMigrator.run()
        val existingProject = currentProjectProvider.getCurrentProject()
        storagePathProvider.getProjectDirPaths(existingProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))

            if (it.endsWith("forms")) {
                assertThat(dir.listFiles()!!.isEmpty(), `is`(true))
            } else {
                assertThat(dir.listFiles()!!.isEmpty(), `is`(false))
            }
        }
    }

    @Test
    fun `migrates general and admin settings`() {
        val oldGeneralSettings = PreferenceManager.getDefaultSharedPreferences(context)
        oldGeneralSettings.edit().putString("generalKey", "generalValue").apply()
        val oldAdminSettings = context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        oldAdminSettings.edit().putString("adminKey", "adminValue").apply()

        existingProjectMigrator.run()
        val existingProject = currentProjectProvider.getCurrentProject()

        val generalSettings = settingsProvider.getGeneralSettings(existingProject.uuid)
        assertThat(
            generalSettings.getString("generalKey"),
            `is`("generalValue")
        )
        val adminSettings = settingsProvider.getAdminSettings(existingProject.uuid)
        assertThat(adminSettings.getString("adminKey"), `is`("adminValue"))
    }

    @Test
    fun `has key`() {
        assertThat(existingProjectMigrator.key(), `is`(MetaKeys.EXISTING_PROJECT_IMPORTED))
    }
}
