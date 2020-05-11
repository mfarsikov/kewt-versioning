package com.github.mfarsikov.kewt.versioning.version

data class SemanticVersion(
        val major: Int,
        val minor: Int,
        val patch: Int
) : Comparable<SemanticVersion> {
    companion object {
        private val comparator = compareBy<SemanticVersion>({ it.major }, { it.minor }, { it.patch })
    }

    override fun compareTo(other: SemanticVersion): Int = comparator.compare(this, other)
    override fun toString() = "$major.$minor.$patch"
}