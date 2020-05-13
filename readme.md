# Kewt versioning Gradle plugin

Project versioning using Git tags

## Quick start
`build.gradle.kts`
```kotlin
plugins {
    id("com.github.mfarsikov.kewt-versioning") version "0.2.0"
}
version = kewtVersioning.version
```

## Tasks
* `release` create next tag in Git according to branch configuration (actually it is a `currentVersion` without a 
`-SNAPSHOT`)
* `currentVersion` prints current version
* `releaseMajor`, `releaseMinor`,`releasePatch` create new tag in git increasing appropriate version. 
Ignores branch configurations (`releaseMajor` increases major version even if for current branch default incrementer is 
Patch).

## Configuration
`build.gradle.kts`
```kotlin
kewtVersioning {
    gitPath = project.rootDir
    prefix = "v"
    separator = "-"
    releaseTaskEnabled = true

    branches = mutableListOf(
        BranchConfig().apply{
                regexes = mutableListOf("master".toRegex())
                incrementer = Incrementer.Minor
                stringify = stringify(useSha = false, useBranch = false)
        }       
    )
}
```
* `gitPath` - path to `.git` folder. Default is current directory `gitPath=project.rootDir`.
* `prefix` and `separator` - are used for Git tags. By default `prefix="v"` and `separator="-"`. Tags look like 
this: `version-0.0.1`. Submodules can use different tags to have independent versioning. 
* `releaseTaskEnabled` allows turning off release task for current submodule.
* `branches` - per branch configuration. By default, this list has configuration for single branch (master).
  * `regexes` - list of regexes for branch names. Default is `mutableListOf("master".toRegex())`. Hint: to avoid a lot 
  of escape symbols use triple double-quotes in Kotlin
  * `incrementer` - default incrementer for the matched branch. Default vaalue is `Incrementer.Minor`. Could be Major, Minor, Patch. Each branch could have its own
   default version increment strategy. Master and feature branches could increment their minor versions (0.1.0 -> 0.2.0).
   Release and fix branches could increment patch versions (0.1.0 -> 0.1.1) 
  * `stringify` - version string configuration. Version name could include branch name, snapshot sign, dirty sign and 
  commit SHA signature. There is a builder `stringify(useBranch, useSnapshot, useDirty, useSha)`, but if it is not 
  enough the `stringify` property is of `(DetailedVersion) -> String` type, so implementation could be provided in place. 
