package com.github.mfarsikov.kewt.versioning.plugin.configDsl

import com.github.mfarsikov.kewt.versioning.plugin.BranchConfig
import com.github.mfarsikov.kewt.versioning.plugin.Incrementer
import com.github.mfarsikov.kewt.versioning.plugin.KewtConfiguration
import com.github.mfarsikov.kewt.versioning.plugin.Versioning
import com.github.mfarsikov.kewt.versioning.version.DetailedVersion
import com.github.mfarsikov.kewt.versioning.version.Stringifier
import java.io.File
import java.time.ZoneId
import java.time.ZoneOffset

@DslMarker
annotation class KewtConfigDsl

@KewtConfigDsl
class KewtConfigurationDsl(val delegate: KewtConfiguration) : IKewtConfiguration by delegate {
    fun branches(block: BranchConfigListBlock.() -> Unit) {
        BranchConfigListBlock(delegate.branches).block()
    }
}

@KewtConfigDsl
class BranchConfigListBlock(val list: MutableList<BranchConfig>) {
    fun clear() {
        list.clear()
    }

    fun add(block: BranchConfigDsl.() -> Unit) {
        list.add(BranchConfigDsl(BranchConfig()).apply(block).delegate)
    }
}

@KewtConfigDsl
class BranchConfigDsl(val delegate: BranchConfig) : IBranchConfig by delegate {
    fun stringifier(
            useBranch: Boolean = true,
            useSnapshot: Boolean = true,
            useDirty: Boolean = true,
            useSha: Boolean? = null,
            useTimestamp: Boolean? = null,
            timeZone: ZoneId = ZoneOffset.systemDefault()
    ) = Stringifier.smartVersionStringifier(
            useBranch = useBranch,
            useSnapshot = useSnapshot,
            useDirty = useDirty,
            useSha = useSha,
            useTimestamp = useTimestamp,
            timeZone = timeZone
    )
}


interface IKewtConfiguration {
    var gitPath: File
    var prefix: String
    var remoteName: String
    var userName: String
    var password: String
    var branches: MutableList<BranchConfig>
    var versioning: Versioning
}

interface IBranchConfig {
    var regexes: MutableList<Regex>
    var incrementer: Incrementer
    var stringify: (DetailedVersion) -> String
}