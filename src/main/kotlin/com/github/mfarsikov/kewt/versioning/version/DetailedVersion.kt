package com.github.mfarsikov.kewt.versioning.version

data class DetailedVersion(
        val lastSpecifiedVersion: SemanticVersion,
        val incrementer: Incrementer,
        val branchName: String?,
        val isSnapshot: Boolean,
        val isDirty: Boolean,
        val sha: String,
) {
    val currentVersion: SemanticVersion
        get() = incrementer.increment(lastSpecifiedVersion)
}