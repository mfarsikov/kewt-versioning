package com.github.mfarsikov.kewt.versioning.version

sealed class Incrementer {
    abstract fun increment(version: SemanticVersion): SemanticVersion

    object Major : Incrementer() {
        override fun increment(version: SemanticVersion) = SemanticVersion(version.major + 1, 0, 0)
    }

    object Minor : Incrementer() {
        override fun increment(version: SemanticVersion) = SemanticVersion(version.major, version.minor + 1, 0)
    }

    object Patch : Incrementer() {
        override fun increment(version: SemanticVersion) = SemanticVersion(version.major, version.minor, version.patch + 1)
    }

    object NoOp : Incrementer() {
        override fun increment(version: SemanticVersion) = version
    }
}