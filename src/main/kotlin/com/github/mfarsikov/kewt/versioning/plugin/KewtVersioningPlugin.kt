package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.git.GitReader
import com.github.mfarsikov.kewt.versioning.version.Incrementer
import com.github.mfarsikov.kewt.versioning.version.VersionCalculator
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

class KewtVersioningPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("kewtVersioning", KewtVersioningExtension::class.java, project)

        // Default config
        project.extensions.getByType(KewtVersioningExtension::class.java).apply {
            gitPath = project.rootDir
            prefix = "v"
            separator = "-"
            remoteName = "origin"
            userName = "\${GITHUB_USER_NAME}"
            password = "\${GITHUB_PASSWORD}"
            releaseTaskEnabled = true
            branches {
                add {
                    regexes = mutableListOf("master".toRegex())
                    incrementer = Incrementer.Minor
                    stringify {
                        useBranch = false
                        useTimestamp = false
                        useSha = false
                    }
                }
                add {
                    regexes = mutableListOf(".*".toRegex())
                    incrementer = Incrementer.Minor
                    stringify {
                        useTimestamp = false
                        useSha = false
                    }
                }
            }
        }

        project.tasks.register("currentVersion", CurrentVersionTask::class.java)
        project.tasks.register("releaseMajor", ReleaseTagTask::class.java, ReleaseType.MAJOR)
        project.tasks.register("releaseMinor", ReleaseTagTask::class.java, ReleaseType.MINOR)
        project.tasks.register("releasePatch", ReleaseTagTask::class.java, ReleaseType.PATCH)
        project.tasks.register("release", ReleaseTagTask::class.java, ReleaseType.DEFAULT)
    }

    companion object {
        fun calculator(project: Project): VersionCalculator {
            val config = project.extensions.getByType(KewtVersioningExtension::class.java)

            val git = GitReader(
                    gitPath = config.gitPath,
                    remoteName = config.remoteName,
                    user = resolveEnv(config.userName),
                    password = resolveEnv(config.password)
            )
            return VersionCalculator(
                    config,
                    git
            )
        }

        private fun resolveEnv(s: String): String {
            val regex = "\\\$\\{(.*)}".toRegex()
            return regex.find(s)?.groupValues?.get(1)?.let { System.getenv(it) } ?: s
        }
    }
}

open class CurrentVersionTask : DefaultTask() {

    @TaskAction
    fun currentVersion() {
        val versionCalculator = KewtVersioningPlugin.calculator(project)
        println("version: ${versionCalculator.currentVersionString()}")
    }
}

open class ReleaseTagTask @Inject constructor(
        @get:Input
        val type: ReleaseType
) : DefaultTask() {

    @TaskAction
    fun releaseTag() {
        if (project.extensions.findByType(KewtVersioningExtension::class.java)!!.releaseTaskEnabled) {
            KewtVersioningPlugin.calculator(project).release(type)
        }
    }
}

enum class ReleaseType { MAJOR, MINOR, PATCH, DEFAULT }