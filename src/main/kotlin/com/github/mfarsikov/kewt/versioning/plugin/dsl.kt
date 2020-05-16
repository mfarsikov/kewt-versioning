package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.version.DetailedVersion
import com.github.mfarsikov.kewt.versioning.version.Incrementer
import com.github.mfarsikov.kewt.versioning.version.Stringifier
import com.github.mfarsikov.kewt.versioning.version.StringifierParams
import java.time.ZoneId
import java.time.ZoneOffset

@DslMarker
annotation class KewtConfigDsl

@KewtConfigDsl
interface IBranchConfigListBlock {
    fun clear()
    fun add(block: BranchConfigDsl.() -> Unit)
}

internal class BranchConfigListBlock(val list: MutableList<BranchConfig>) : IBranchConfigListBlock {
    override fun clear() {
        list.clear()
    }

    override fun add(block: BranchConfigDsl.() -> Unit) {
        val c = BranchConfigDslImpl().apply(block).let {
            BranchConfig().apply {
                this.regexes = it.regexes
                this.stringify = it.stringify
                this.incrementer = it.incrementer
            }
        }
        list.add(c)
    }
}


@KewtConfigDsl
interface BranchConfigDsl {
    var regexes: MutableList<Regex>
    var incrementer: Incrementer
    var stringify: (DetailedVersion) -> String

    operator fun ((DetailedVersion) -> String).invoke(block: StringifierDsl.() -> Unit)
}

internal class BranchConfigDslImpl : BranchConfigDsl {
    override var regexes: MutableList<Regex> = mutableListOf()
    override var incrementer: Incrementer = Incrementer.Minor
    override var stringify: (DetailedVersion) -> String = Stringifier.smartVersionStringifier(StringifierDslImpl().toStringifierParams())

    override operator fun ((DetailedVersion) -> String).invoke(block: StringifierDsl.() -> Unit) {
        stringify = StringifierDslImpl().apply(block).let {
            Stringifier.smartVersionStringifier(it.toStringifierParams())
        }
    }
}

@KewtConfigDsl
interface StringifierDsl {
    var useBranch: Boolean
    var useSnapshot: Boolean
    var useDirty: Boolean
    var useSha: Boolean?
    var useTimestamp: Boolean?
    var timeZone: ZoneId
}

internal class StringifierDslImpl : StringifierDsl {
    override var useBranch: Boolean = true
    override var useSnapshot: Boolean = true
    override var useDirty: Boolean = true
    override var useSha: Boolean? = null
    override var useTimestamp: Boolean? = null
    override var timeZone: ZoneId = ZoneOffset.systemDefault()
}

private fun StringifierDslImpl.toStringifierParams() = StringifierParams(
        useBranch = useBranch,
        useSnapshot = useSnapshot,
        useDirty = useDirty,
        useSha = useSha,
        useTimestamp = useTimestamp,
        timeZone = timeZone
)