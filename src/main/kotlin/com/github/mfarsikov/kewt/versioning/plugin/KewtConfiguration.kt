package com.github.mfarsikov.kewt.versioning.plugin

import com.github.mfarsikov.kewt.versioning.plugin.configDsl.IBranchConfig
import com.github.mfarsikov.kewt.versioning.plugin.configDsl.IKewtConfiguration
import com.github.mfarsikov.kewt.versioning.version.DetailedVersion
import java.io.File
import kotlin.properties.Delegates

class KewtConfiguration : IKewtConfiguration {
    override var gitPath: File by Delegates.notNull()
    override var prefix: String by Delegates.notNull()
    override var remoteName: String by Delegates.notNull()
    override var userName: String by Delegates.notNull()
    override var password: String by Delegates.notNull()
    override var branches: MutableList<BranchConfig> = mutableListOf()
    override var versioning: Versioning = Versioning.SEMANTIC
}

class BranchConfig : IBranchConfig {
    override var regexes: MutableList<Regex> = mutableListOf()
    override var incrementer: Incrementer by Delegates.notNull()
    override var stringify: (DetailedVersion) -> String by Delegates.notNull()
}

enum class Incrementer { MINOR, MAJOR, PATCH }
enum class Versioning { SEMANTIC, INCREMENTAL }
