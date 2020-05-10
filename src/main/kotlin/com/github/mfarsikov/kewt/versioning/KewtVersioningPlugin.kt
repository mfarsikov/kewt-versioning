package com.github.mfarsikov.kewt.versioning

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.eclipse.jgit.lib.Constants
import org.gradle.internal.impldep.org.eclipse.jgit.revwalk.RevWalk
import java.io.File

class KewtVersioningPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val ext = project.extensions.create("kewtVersioning", KewtVersioningConfig::class.java)
        project.tasks.register("greeting", GreetTask::class.java)
    }
}

open class GreetTask : DefaultTask() {

    val c = project.extensions.getByType(KewtVersioningConfig::class.java)

    @TaskAction
    fun greet() {

        val repository = Git.open(File("./")).repository
        repository.resolve(Constants.HEAD)
        RevWalk(repository)
    }
}

fun main() {
    val repository = Git.open(File("./")).repository
    RevWalk(repository)
    println(repository.resolve(Constants.HEAD).name)
//    RevWalk(repository)
}

open class KewtVersioningConfig {
    var message = "rst"
}
