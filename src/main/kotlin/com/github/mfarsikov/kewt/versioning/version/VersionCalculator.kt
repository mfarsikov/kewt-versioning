package com.github.mfarsikov.kewt.versioning.version

import com.github.mfarsikov.kewt.versioning.git.GitReader
import com.github.mfarsikov.kewt.versioning.plugin.BranchConfig
import com.github.mfarsikov.kewt.versioning.plugin.KewtConfiguration
import com.github.mfarsikov.kewt.versioning.plugin.ReleaseType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class VersionCalculator(
    private val config: KewtConfiguration,
    private val gitReader: GitReader
) {

    companion object {
        private val logger :Logger= LoggerFactory.getLogger(VersionCalculator::class.java)
    }

    fun currentVersionString(): String {
        val currentVersion = currentVersion()
        val stringifier = brachConfigFor(currentVersion.branchName ?: "").stringify
        return stringifier(currentVersion).replace('/', '-')
    }

    private fun brachConfigFor(branchName: String): BranchConfig {
        return config.branches.firstOrNull { branchConfig -> branchConfig.regexes.any { it.matches(branchName) } }
            ?: throw RuntimeException("kewtVersioning: there is no matching regex for branch: $branchName, among: ${config.branches.flatMap { it.regexes }}")
    }

    fun currentVersion(): DetailedVersion {
        try {

            val status = gitReader.status()

            val branchConfig = brachConfigFor(status.branch ?: "")

            val semanticVersion = version(status.branchTags) ?: SemanticVersion(0, 0, 0)

            val isSnapshot = version(status.commitTags) == null

            val branchTypeIncrementer = when (branchConfig.incrementer) {
                com.github.mfarsikov.kewt.versioning.plugin.Incrementer.MINOR -> Incrementer.Minor
                com.github.mfarsikov.kewt.versioning.plugin.Incrementer.MAJOR -> Incrementer.Major
                com.github.mfarsikov.kewt.versioning.plugin.Incrementer.PATCH -> Incrementer.Patch
            }

            return DetailedVersion(
                lastSpecifiedVersion = semanticVersion,
                incrementer = if (isSnapshot || status.isDirty) branchTypeIncrementer else Incrementer.NoOp,
                branchName = status.branch,
                isSnapshot = isSnapshot || status.isDirty,
                isDirty = status.isDirty,
                sha = status.sha
            )
        } catch (ex: Exception) {
            logger.error("Cannot read version from Git. Fallback to 'unknown' version", ex)
            return DetailedVersion(
                lastSpecifiedVersion = SemanticVersion(0, 1, 0),
                incrementer = Incrementer.NoOp,
                branchName = "unknown",
                isSnapshot = false,
                isDirty = false,
                sha = "unknown",
            )
        }
    }

    private fun version(tags: List<String>): SemanticVersion? = tags
        .filter { it.startsWith(config.prefix + config.separator) }
        .map { extractVersion(it.substringAfter(config.prefix + config.separator)) }
        .maxOrNull()

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
