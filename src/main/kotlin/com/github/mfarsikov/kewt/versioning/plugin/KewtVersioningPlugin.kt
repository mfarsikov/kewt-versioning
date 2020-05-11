package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.version.VersionCalculator
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

class KewtVersioningPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("kewtVersioning", KewtVersioningExtension::class.java, project)

        project.tasks.register("currentVersion", CurrentVersionTask::class.java)
        project.tasks.register("releaseMajor", ReleaseTagTask::class.java, ReleaseType.MAJOR)
        project.tasks.register("releaseMinor", ReleaseTagTask::class.java, ReleaseType.MINOR)
        project.tasks.register("releasePatch", ReleaseTagTask::class.java, ReleaseType.PATCH)
        project.tasks.register("release", ReleaseTagTask::class.java, ReleaseType.DEFAULT)
    }
}

open class CurrentVersionTask : DefaultTask() {

    private val versionPlugin = VersionCalculator(project.extensions.getByType(KewtVersioningExtension::class.java))

    @TaskAction
    fun currentVersion() {
        println("version: ${versionPlugin.currentVersionString()}")
    }
}

open class ReleaseTagTask @Inject constructor(val type: ReleaseType) : DefaultTask() {

    private val versionPlugin = VersionCalculator(project.extensions.getByType(KewtVersioningExtension::class.java))

    @TaskAction
    fun releaseTag() {
        versionPlugin.release(type)
    }
}

enum class ReleaseType { MAJOR, MINOR, PATCH, DEFAULT }