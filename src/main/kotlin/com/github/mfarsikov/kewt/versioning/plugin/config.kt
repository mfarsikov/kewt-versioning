package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.version.DetailedVersion
import com.github.mfarsikov.kewt.versioning.version.Incrementer
import org.gradle.api.Project
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

open class KewtVersioningExtension(project: Project) {

    var gitPath = project.rootDir
    var prefix = "v"
    var separator = "-"
    var remoteName = "origin"
    var userName = "\${GITHUB_USER_NAME}"
    var password = "\${GITHUB_PASSWORD}"
    var releaseTaskEnabled: Boolean = true

    var branches: MutableList<BranchConfig> = mutableListOf(
            BranchConfig(),
            BranchConfig().apply {
                regexes = mutableListOf(".*".toRegex())
                stringify = smartVersionStringifier(useSha = false, useTimestamp = false)
            }
    )

    val version = VersionHolder(project)
}

open class BranchConfig {
    var regexes: MutableList<Regex> = mutableListOf("master".toRegex())
    var incrementer: Incrementer = Incrementer.Minor
    var stringify: (DetailedVersion) -> String = smartVersionStringifier(useSha = false, useTimestamp = false, useBranch = false)

    /**
     * Use SHA only if it is a snapshot. Otherwise commit is tagged, no need in SHA.
     * Use timestamp only if is dirty. Otherwise the commit itself has a timestamp.
     */
    fun smartVersionStringifier(
            useBranch: Boolean = true,
            useSnapshot: Boolean = true,
            useDirty: Boolean = true,
            useSha: Boolean? = null,
            useTimestamp: Boolean? = null,
            timeZone: ZoneId = ZoneOffset.systemDefault()
    ): (DetailedVersion) -> String = { version ->
        val branchName = version.branchName.takeIf { useBranch }?.let { "-$it" } ?: ""
        val snapshot = if (version.isSnapshot && useSnapshot) "-SNAPSHOT" else ""
        val dirty = if (version.isDirty && useDirty) "-dirty" else ""
        val sha = version.sha.takeIf { version.isSnapshot && useSha != false || useSha == true }?.let { "-$it" } ?: ""
        val timestamp = if (version.isDirty && useTimestamp != false || useTimestamp == true) "-${ZonedDateTime.now(timeZone)}".replace(":", "-") else ""
        "${version.currentVersion}${branchName}${snapshot}${sha}${dirty}${timestamp}"
    }
}
