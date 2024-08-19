package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.git.GitReader
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
                prefix = "v-"
                remoteName = "origin"
                userName = "\${GITHUB_USER_NAME}"
                password = "\${GITHUB_PASSWORD}"
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
                println(calculator(project).currentVersionString())
            }
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
                    git,
                config.versioning
            )
        }

        private fun resolveEnv(s: String): String {
            val regex = "\\\$\\{(.*)}".toRegex()
            return regex.find(s)?.groupValues?.get(1)?.let { System.getenv(it) } ?: s
        }
    }
}
