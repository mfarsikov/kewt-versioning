package com.github.mfarsikov.kewt.versioning.version

import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

object Stringifier {
    /**
     * Use SHA only if it is a snapshot. Otherwise commit is tagged, no need in SHA.
     * Use timestamp only if is dirty. Otherwise the commit itself has a timestamp.
     */
    fun smartVersionStringifier(
            params: StringifierParams
    ): (DetailedVersion) -> String = { version ->
        params.run {
            val branchName = version.branchName.takeIf { useBranch }?.let { "-$it" } ?: ""
            val snapshot = if (version.isSnapshot && useSnapshot) "-SNAPSHOT" else ""
            val dirty = if (version.isDirty && useDirty) "-dirty" else ""
            val sha = version.sha.takeIf { version.isSnapshot && useSha != false || useSha == true }?.let { "-$it" }
                    ?: ""
            val timestamp = if (version.isDirty && useTimestamp != false || useTimestamp == true) "-${ZonedDateTime.now(timeZone)}".replace(":", "-") else ""
            "${version.currentVersion}${branchName}${snapshot}${sha}${dirty}${timestamp}"
        }
    }
}

data class StringifierParams(
        val useBranch: Boolean = true,
        val useSnapshot: Boolean = true,
        val useDirty: Boolean = true,
        val useSha: Boolean? = null,
        val useTimestamp: Boolean? = null,
        val timeZone: ZoneId = ZoneOffset.systemDefault()
)