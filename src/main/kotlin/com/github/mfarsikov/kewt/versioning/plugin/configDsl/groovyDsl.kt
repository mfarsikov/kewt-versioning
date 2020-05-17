package com.github.mfarsikov.kewt.versioning.plugin.configDsl

import com.github.mfarsikov.kewt.versioning.plugin.BranchConfig
import com.github.mfarsikov.kewt.versioning.plugin.Incrementer
import com.github.mfarsikov.kewt.versioning.plugin.KewtConfiguration
import com.github.mfarsikov.kewt.versioning.version.DetailedVersion
import com.github.mfarsikov.kewt.versioning.version.Stringifier
import groovy.lang.Closure
import java.time.ZoneId
import java.time.ZoneOffset

class GroovyKewtVersioningExtensionDsl(val configuration: KewtConfiguration) : IKewtConfiguration by configuration {
    fun branches(action: Closure<GroovyBranchConfigListBlock>) {
        action.apply { delegate = GroovyBranchConfigListBlock(configuration.branches) }.call()
    }
}

class GroovyBranchConfigListBlock(val branchConfigList: MutableList<BranchConfig>) {
    fun clear() {
        branchConfigList.clear()
    }

    fun add(block: Closure<GroovyBranchConfigDsl>) {
        val branchConfig = GroovyBranchConfigDsl(GroovyBranchConfig())
        block.apply { delegate = branchConfig }.call()
        branchConfigList.add(branchConfig.delegate.toBranchConfig())
    }
}

class GroovyBranchConfigDsl(val delegate: GroovyBranchConfig) : IGroovyBranchConfig by delegate {


    fun stringifier(params: Map<String, Boolean>): (DetailedVersion) -> String = Stringifier.smartVersionStringifier(
            useBranch = params["useBranch"] ?: true,
            useSnapshot = params["useSnapshot"] ?: true,
            useDirty = params["useDirty"] ?: true,
            useSha = params["useSha"],
            useTimestamp = params["useTimestamp"],
            timeZone = if (params["useUtc"] == true) ZoneOffset.UTC else ZoneId.systemDefault()
    )
}
interface IGroovyBranchConfig{
    var regexes: MutableList<java.util.regex.Pattern>
    var incrementer: Incrementer
    var stringify: (DetailedVersion) -> String
}

open class GroovyBranchConfig:IGroovyBranchConfig {
    override var regexes: MutableList<java.util.regex.Pattern> = mutableListOf()
    override var incrementer: Incrementer = Incrementer.MINOR
    override lateinit var stringify: (DetailedVersion) -> String
}
fun GroovyBranchConfig.toBranchConfig():BranchConfig {
    val bc = BranchConfig()
    bc.regexes = regexes.map { it.toRegex() }.toMutableList()
    bc.incrementer = incrementer
    bc.stringify = stringify
    return bc
}

