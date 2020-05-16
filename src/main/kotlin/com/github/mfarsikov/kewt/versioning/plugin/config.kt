package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.version.DetailedVersion
import com.github.mfarsikov.kewt.versioning.version.Incrementer
import com.github.mfarsikov.kewt.versioning.version.Stringifier
import org.gradle.api.Project
import java.io.File
import kotlin.properties.Delegates

open class KewtVersioningExtension(project: Project) {

    lateinit var gitPath: File
    lateinit var prefix: String
    lateinit var separator: String
    lateinit var remoteName: String
    lateinit var userName: String
    lateinit var password: String
    var releaseTaskEnabled: Boolean by Delegates.notNull()

    var branches: MutableList<BranchConfig> = mutableListOf()

    val version = VersionHolder(project)

    /**
     * DSL rabbit hole
     */
    operator fun MutableList<BranchConfig>.invoke(block: IBranchConfigListBlock.() -> Unit) {
        BranchConfigListBlock(this).block()
    }
}

open class BranchConfig {
    var regexes: MutableList<Regex> = mutableListOf()
    lateinit var incrementer: Incrementer
    lateinit var stringify: (DetailedVersion) -> String
}
