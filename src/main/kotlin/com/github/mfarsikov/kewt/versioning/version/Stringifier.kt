package com.github.mfarsikov.kewt.versioning.version

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object Stringifier {
    /**
     * Use SHA only if it is a snapshot. Otherwise commit is tagged, no need in SHA.
     * Use timestamp only if is dirty. Otherwise the commit itself has a timestamp.
     */
    fun smartVersionStringifier(
            useBranch: Boolean? = null,
            useSnapshot: Boolean = true,
            useDirty: Boolean = true,
            useSha: Boolean? = null,
            useTimestamp: Boolean? = null,
            timeZone: ZoneId = ZoneOffset.UTC
    ): (DetailedVersion) -> String = { version ->
        val branchName = version.branchName.takeIf { useBranch == true }?.let { "-$it" } ?: ""
        val snapshot = if (version.isSnapshot && useSnapshot) "-SNAPSHOT" else ""
        val dirty = if (version.isDirty && useDirty) "-dirty" else ""
        val sha = version.sha.takeIf { version.isSnapshot && useSha != false || useSha == true }
            ?.let { "-$it" }
                ?: ""
        val timestamp = if (version.isDirty && useTimestamp != false || useTimestamp == true) "-${LocalDateTime.now(timeZone)}".replace(":", "-") else ""
        "${version.currentVersion}${branchName}${snapshot}${sha}${dirty}${timestamp}"
    }
}
