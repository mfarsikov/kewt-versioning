package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.version.DetailedVersion
import com.github.mfarsikov.kewt.versioning.version.Incrementer
import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File

open class KewtVersioningExtension(project: Project) {

    var gitPath = project.rootDir
    var prefix = "v"
    var separator = "-"
    var releaseTaskEnabled: Boolean = true

    var branches: MutableList<BranchConfig> = mutableListOf(BranchConfig())

    val version = VersionHolder(project)
}

open class BranchConfig {
    var regexes: MutableList<Regex> = mutableListOf("master".toRegex())
    var incrementer: Incrementer = Incrementer.Minor
    var stringify: (DetailedVersion) -> String = stringify(useSha = false, useBranch = false)

    fun stringify(
            useBranch: Boolean = true,
            useSnapshot: Boolean = true,
            useDirty: Boolean = true,
            useSha: Boolean = true
    ): (DetailedVersion) -> String = { version ->
        val branchName = version.branchName.takeIf { useBranch }?.let { "-$it" } ?: ""
        val snapshot = if (version.isSnapshot && useSnapshot) "-SNAPSHOT" else ""
        val dirty = if (version.isDirty && useDirty) "-dirty" else ""
        val sha = version.sha.takeIf { useSha }?.let { "-$it" } ?: ""
        "${version.currentVersion}${branchName}${snapshot}${dirty}${sha}"
    }
}
