package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.plugin.configDsl.GroovyKewtVersioningExtensionDsl
import com.github.mfarsikov.kewt.versioning.plugin.configDsl.KewtConfigurationDsl
import groovy.lang.Closure
import org.gradle.api.Project

open class KewtVersioningExtension(project: Project) {

    var configuration: KewtConfiguration = KewtConfiguration()

    fun configuration(block: KewtConfigurationDsl.() -> Unit) {
        KewtConfigurationDsl(configuration).block()
    }

    fun groovyConfigurationDsl(action: Closure<GroovyKewtVersioningExtensionDsl>) {
        action.apply { delegate = GroovyKewtVersioningExtensionDsl(configuration) }.call()
    }

    val version = VersionHolder(project)
}

