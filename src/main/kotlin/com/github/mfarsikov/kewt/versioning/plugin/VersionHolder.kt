package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.version.VersionCalculator
import org.gradle.api.Project

/**
 * Used for providing version lazily, to avoid version calculation on gradle configuration stage.
 * Actual calculation is triggered by toString()
 *
 */
class VersionHolder(project: Project){
    private val version by lazy {
        VersionCalculator(project.extensions.getByType(KewtVersioningExtension::class.java))
                .currentVersionString()
    }

    override fun toString() = version
}