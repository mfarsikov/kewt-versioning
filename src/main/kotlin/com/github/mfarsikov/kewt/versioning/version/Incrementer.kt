package com.github.mfarsikov.kewt.versioning.version

sealed class Incrementer {
  abstract fun increment(version: Version): Version

  object Major : Incrementer() {
    override fun increment(version: Version): Version =
      when (version) {
        is Version.SemanticVersion -> Version.SemanticVersion(version.major + 1, 0, 0)
        is Version.IncrementalVersion -> Version.IncrementalVersion(version.version + 1)
      }
  }

  object Minor : Incrementer() {
    override fun increment(version: Version): Version =
      when (version) {
        is Version.SemanticVersion -> Version.SemanticVersion(version.major, version.minor + 1, 0)
        is Version.IncrementalVersion -> Version.IncrementalVersion(version.version + 1)
      }
  }

  object Patch : Incrementer() {
    override fun increment(version: Version): Version =
      when (version) {
        is Version.SemanticVersion -> Version.SemanticVersion(version.major, version.minor, version.patch + 1)
        is Version.IncrementalVersion -> Version.IncrementalVersion(version.version + 1)
      }
  }

  object NoOp : Incrementer() {
    override fun increment(version: Version) = version
  }
}
