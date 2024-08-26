package com.github.mfarsikov.kewt.versioning.version

import com.github.mfarsikov.kewt.versioning.git.GitReader
import com.github.mfarsikov.kewt.versioning.plugin.BranchConfig
import com.github.mfarsikov.kewt.versioning.plugin.KewtConfiguration
import com.github.mfarsikov.kewt.versioning.plugin.Versioning
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class VersionCalculator(
    private val config: KewtConfiguration,
    private val gitReader: GitReader,
    private val versioning: Versioning,
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(VersionCalculator::class.java)
        private val incrementalVersionRegex = "\\d+".toRegex()
        private val semanticVersionRegex = "\\d+\\.\\d+\\.\\d+".toRegex()
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

            val version = version(status.branchTags, versioning) ?: when (versioning) {
                Versioning.SEMANTIC -> Version.SemanticVersion(0, 0, 0)
                Versioning.INCREMENTAL -> Version.IncrementalVersion(0)
            }

            val isSnapshot = version(status.commitTags, versioning) == null


            val branchTypeIncrementer = when (versioning) {
                Versioning.SEMANTIC -> when (branchConfig.incrementer) {
                    com.github.mfarsikov.kewt.versioning.plugin.Incrementer.MINOR -> Incrementer.Minor
                    com.github.mfarsikov.kewt.versioning.plugin.Incrementer.MAJOR -> Incrementer.Major
                    com.github.mfarsikov.kewt.versioning.plugin.Incrementer.PATCH -> Incrementer.Patch
                }

                Versioning.INCREMENTAL -> Incrementer.Major
            }

            return DetailedVersion(
                lastSpecifiedVersion = version,
                incrementer = if (isSnapshot || status.isDirty) branchTypeIncrementer else Incrementer.NoOp,
                branchName = status.branch,
                isSnapshot = isSnapshot || status.isDirty,
                isDirty = status.isDirty,
                sha = status.sha
            )
        } catch (ex: Exception) {
            logger.error("Cannot read version from Git. Fallback to 'unknown' version", ex)
            return DetailedVersion(
                lastSpecifiedVersion = Version.SemanticVersion(0, 1, 0),
                incrementer = Incrementer.NoOp,
                branchName = "unknown",
                isSnapshot = false,
                isDirty = false,
                sha = "unknown",
            )
        }
    }

    private fun version(tags: List<String>, versioning: Versioning): Version? =
        when (versioning) {
            Versioning.SEMANTIC -> tags
                .filter { it.startsWith(config.prefix) && it.substringAfter(config.prefix) matches semanticVersionRegex }
                .maxOfOrNull { extractSemanticVersion(it.substringAfter(config.prefix)) }

            Versioning.INCREMENTAL -> tags
                .filter { it.startsWith(config.prefix) && it.substringAfter(config.prefix) matches incrementalVersionRegex }
                .maxOfOrNull { extractIncrementalVersion(it.substringAfter(config.prefix)) }
        }

    private fun extractSemanticVersion(version: String): Version.SemanticVersion = version.split(".")
        .map { it.toInt() }
        .let { Version.SemanticVersion(it[0], it[1], it[2]) }

    private fun extractIncrementalVersion(version: String): Version.IncrementalVersion =
        Version.IncrementalVersion(version.toInt())
}
