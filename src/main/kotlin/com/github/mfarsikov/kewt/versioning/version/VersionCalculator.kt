package com.github.mfarsikov.kewt.versioning.version

import com.github.mfarsikov.kewt.versioning.git.GitReader
import com.github.mfarsikov.kewt.versioning.plugin.BranchConfig
import com.github.mfarsikov.kewt.versioning.plugin.KewtVersioningExtension
import com.github.mfarsikov.kewt.versioning.plugin.ReleaseType
import org.slf4j.LoggerFactory

class VersionCalculator(
        private val config: KewtVersioningExtension
) {
    private val gitReader = GitReader(config.gitPath)

    fun currentVersionString(): String {
        val currentVersion = currentVersion()
        val stringifier = brachConfigFor(currentVersion.branchName).stringify
        return stringifier(currentVersion).replace("/", "-")
    }

    private fun brachConfigFor(branchName:String): BranchConfig {
        return config.branches.first { branchConfig -> branchConfig.regexes.any { it.matches(branchName) } }
    }

    fun currentVersion(): DetailedVersion {

        val status = gitReader.status()

        val branchConfig = brachConfigFor(status.branch)

        val semanticVersion = version(status.branchTags) ?: SemanticVersion(0, 1, 0)

        val isSnapshot = version(status.commitTags) == null

        val branchTypeIncrementer = branchConfig.incrementer

        return DetailedVersion(
                lastSpecifiedVersion = semanticVersion,
                incrementer = if (isSnapshot || status.isDirty) branchTypeIncrementer else Incrementer.NoOp,
                branchName = status.branch,
                isSnapshot = isSnapshot,
                isDirty = status.isDirty,
                sha = status.sha
        )
    }

    private fun version(tags: List<String>): SemanticVersion? = tags
            .filter { it.startsWith(config.prefix + config.separator) }
            .map { extractVersion(it.substringAfter(config.prefix + config.separator)) }
            .max()


    fun release(releaseType: ReleaseType) {

        //TODO sanityCheck
        val detailedVersion = currentVersion()

        if (detailedVersion.isDirty) {
            throw RuntimeException("Cannot release if there are uncommitted changes")
        }

        val incrementer = when (releaseType) {
            ReleaseType.MAJOR -> Incrementer.Major
            ReleaseType.MINOR -> Incrementer.Minor
            ReleaseType.PATCH -> Incrementer.Patch
            ReleaseType.DEFAULT -> detailedVersion.incrementer
        }

        val newVersion = incrementer.increment(detailedVersion.lastSpecifiedVersion)

        gitReader.tag("${config.prefix}${config.separator}$newVersion")
    }

    private fun extractVersion(version: String): SemanticVersion =
            version.split(".")
                    .map { it.toInt() }
                    .let { SemanticVersion(it[0], it[1], it[2]) }

}
