package com.github.mfarsikov.kewt.versioning

import org.gradle.api.Plugin
import org.gradle.api.Project

class KewtVersioningPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val ext = project.extensions.create("kewtVersioning", KewtVersioningConfig::class.java)
        project.tasks.register("greeting") { task ->
            task.doLast {

                println(ext.message)
            }
        }
    }
}

open class KewtVersioningConfig {
    var message = "rst"
}
