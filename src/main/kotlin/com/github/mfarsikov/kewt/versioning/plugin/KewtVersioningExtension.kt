package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.plugin.configDsl.KewtConfigurationDsl
import org.gradle.api.Project

open class KewtVersioningExtension(project: Project) {

    var configuration: KewtConfiguration = KewtConfiguration()

    fun configuration(block: KewtConfigurationDsl.() -> Unit) {
        KewtConfigurationDsl(configuration).block()
    }

    val version = VersionHolder(project)
}

