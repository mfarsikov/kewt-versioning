package com.github.mfarsikov.kewt.versioning.version

sealed class Version {

  data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
  ) :Version(), Comparable<SemanticVersion> {
    companion object {
      private val comparator = compareBy<SemanticVersion>({ it.major }, { it.minor }, { it.patch })
    }

    override fun compareTo(other: SemanticVersion): Int = comparator.compare(this, other)
    override fun toString() = "$major.$minor.$patch"
  }

  data class IncrementalVersion(
    val version: Int,
  ) :Version(), Comparable<IncrementalVersion> {
    companion object {
      private val comparator = compareBy<IncrementalVersion>({ it.version })
    }

    override fun compareTo(other: IncrementalVersion): Int = comparator.compare(this, other)
    override fun toString() = "$version"
  }
}