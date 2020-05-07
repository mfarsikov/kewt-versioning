package com.github.mfarsikov.kewt.versioning

import org.gradle.api.Project
import org.gradle.api.Plugin

class KewtVersioningPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("greeting") { task ->
            task.doLast {
                println("Hello from plugin 'com.github.mfarsikov.kewt.versioning.greeting'")
            }
        }
    }
}
