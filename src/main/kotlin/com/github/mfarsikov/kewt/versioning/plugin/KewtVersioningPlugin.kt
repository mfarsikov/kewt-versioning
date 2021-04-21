package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.git.GitReader
import com.github.mfarsikov.kewt.versioning.plugin.configDsl.BranchConfigListBlock
import com.github.mfarsikov.kewt.versioning.version.VersionCalculator
import org.gradle.api.Plugin
import org.gradle.api.Project

class KewtVersioningPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("kewtVersioning", KewtVersioningExtension::class.java, project)

        // Default config
        project.extensions.getByType(KewtVersioningExtension::class.java).apply {
            configuration {
                gitPath = project.rootDir
                prefix = "v"
                separator = "-"
                remoteName = "origin"
                userName = "\${GITHUB_USER_NAME}"
                password = "\${GITHUB_PASSWORD}"
                releaseTaskEnabled = true
                branches {

                    add {
                        regexes = mutableListOf("master".toRegex(), "main".toRegex())
                        incrementer = Incrementer.MINOR
                        stringify = stringifier(useBranch = false, useTimestamp = false, useSha = false)
                    }

                    add {
                        regexes = mutableListOf(".*".toRegex())
                        incrementer = Incrementer.MINOR
                        stringify  = stringifier(useTimestamp = false, useSha = false)
                    }
                }
            }
        }

        project.tasks.register("currentVersion") {
            it.group = "Versioning"
            it.description = "Print current version to console"
            it.doLast {
                val versionCalculator = calculator(project)
                println("version: ${versionCalculator.currentVersionString()}")
            }
        }

        project.tasks.register("release") {
            it.group = "Versioning"
            it.description = "Create next tag in git using configuration, and push it to remote if configured"
            it.doLast {
                release(it.project, ReleaseType.DEFAULT)
            }
        }

        project.tasks.register("releaseMajor") {
            it.group = "Versioning"
            it.description = "Create next tag in git increasing a major version, and push it to remote if configured"
            it.doLast {
                release(it.project, ReleaseType.MAJOR)
            }
        }

        project.tasks.register("releaseMinor") {
            it.group = "Versioning"
            it.description = "Create next tag in git increasing a minor version, and push it to remote if configured"
            it.doLast {
                release(it.project, ReleaseType.MINOR)
            }
        }

        project.tasks.register("releasePatch") {
            it.group = "Versioning"
            it.description = "Create next tag in git increasing a patch version, and push it to remote if configured"
            it.doLast {
                release(it.project, ReleaseType.PATCH)
            }
        }
    }

    private fun release(project: Project, type: ReleaseType) {
        if (project.extensions.findByType(KewtVersioningExtension::class.java)!!.configuration.releaseTaskEnabled) {
            calculator(project).release(type)
        }
    }

    companion object {
        fun calculator(project: Project): VersionCalculator {
            val config = project.extensions.getByType(KewtVersioningExtension::class.java).configuration

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

enum class ReleaseType { MAJOR, MINOR, PATCH, DEFAULT }