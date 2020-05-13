package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.version.VersionCalculator
import org.gradle.api.Project
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * Used for providing version lazily, to avoid version calculation on gradle configuration stage.
 * Actual calculation is triggered by toString()
 *
 */
//class VersionHolder(project: Project) {
//    private val version by lazy {
//        VersionCalculator(project.extensions.getByType(KewtVersioningExtension::class.java))
//                .currentVersionString()
//    }
//
//    override fun toString() = version
//}
//

class VersionHolder(private val project: Project) : Serializable {
    private var version: String? = null

    override fun equals(other: Any?): Boolean {
        return toString() == other.toString()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    @Synchronized
    override fun toString(): String {
        if (version == null) {
            version = VersionCalculator(project.extensions.getByType(KewtVersioningExtension::class.java))
                    .currentVersionString()
        }
        return version!!
    }

    private fun writeObject(oos: ObjectOutputStream) {
        oos.writeObject(toString())
    }

    private fun readObject(`in`: ObjectInputStream) {
        version = `in`.readObject() as String
    }

}