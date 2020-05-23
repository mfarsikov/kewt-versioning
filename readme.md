# Kewt versioning Gradle plugin
inspired by [Axion release plugin](https://github.com/allegro/axion-release-plugin)

Project versioning using Git tags with type safe Gradle Kotlin DSL.

![gradlePluginPortal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/mfarsikov/kewt-versioning/com.github.mfarsikov.kewt-versioning.gradle.plugin/maven-metadata.xml.svg?label=gradle%20plugin%20portal&style=flat-square)

## Quick start
<details open>
<summary>Kotlin</summary>

`build.gradle.kts`

```kotlin
plugins {
    id("com.github.mfarsikov.kewt-versioning") version "0.6.0"
}
version = kewtVersioning.version
```                                   
</details>

<details>
<summary>Groovy</summary>

`build.gradle`

```groovy
plugins {
    id 'com.github.mfarsikov.kewt-versioning' version '0.6.0'
}
version = kewtVersioning.version
```        
</details>

## Tasks
* `release` create next tag in Git according to branch configuration.
* `currentVersion` prints current version.
* `releaseMajor`, `releaseMinor`,`releasePatch` create new tag in git increasing appropriate version. 
Ignores branch configurations (`releaseMajor` increases major version even if for current branch default incrementer is 
Patch).

## Configuration
This is default configuration. It includes two pre-configured branches: `master` and all other (`.*`).
Order does matter. First matched config will be used.

<details open>
<summary>Kotlin</summary>
`build.gradle.kts`:

```kotlin
import com.github.mfarsikov.kewt.versioning.plugin.Incrementer

kewtVersioning.configuration {
    gitPath = project.rootDir  // default
    prefix = "v" // default
    separator = "-" // default
    remoteName = "origin" // default
    userName = "\${GITHUB_USER_NAME}" // default
    password = "\${GITHUB_PASSWORD}" // default
    releaseTaskEnabled = true // default
    branches {
        clear()
        add {
            regexes = mutableListOf("master".toRegex())
            incrementer = Incrementer.MINOR // default
            stringify = stingifier(
                useBranch = false,  // by default: true
                useSnapshot = true, // default
                useDirty = true, // default
                useSha = false,  // by default: null
                useTimestamp = false, // by default: null
                timeZone = ZoneOffset.systemDefault() // Default
            )
        }
        add {
            regexes = mutableListOf(".*".toRegex())
            stringify = stingifier(
                useTimestamp = false,
                useSha = false
            )
        }
    }
}
```
</details>

<details>
<summary>Groovy</summary>

`build.gradle`:

```groovy
import com.github.mfarsikov.kewt.versioning.plugin.Incrementer

kewtVersioning.groovyConfigurationDsl {
    gitPath = project.rootDir  // default
    prefix = 'v' // default
    separator = '-' // default
    remoteName = 'origin' // default
    userName = '${GITHUB_USER_NAME}' // default
    password = '${GITHUB_PASSWORD}' // default
    releaseTaskEnabled = true // default
    branches {
        clear()
        add {
            regexes = [~'master']
            incrementer = Incrementer.MINOR // default
            stringify = stringifier([
                    useBranch: false,  // by default: true
                    useSnapshot: true, // default
                    useDirty: true, // default
                    useSha: false,  // by default: null
                    useTimestamp: false, // by default: null
                    useUtc: false //Default
            ])
        }
        add {
            regexes = [~'.*']
            stringify = stringifier([
                    useTimestamp: false,
                    useSha: false
            ])
        }
    }
}
```
</details>

* `gitPath` - path to `.git` folder. Default is project root `gitPath=project.rootDir`.
* `prefix` and `separator` - are used for Git tags. By default `prefix="v"` and `separator="-"`. Tags look like 
this: `v-0.0.1`. Submodules can use different tags to have independent versioning. 
* `remoteName` remote repository name. Default is `"origin"`. To prevent pushing tags to remote could be reset to `null`
* `userName` and `password` used for HTTPS connection to remote repository. If value has prefix `${` and postfix `}` (string in Kotlin `"\${MY_PWD}"`) it will be resolved from environment variables.
 Could contain plain values (highly not recommended). 
* `releaseTaskEnabled` allows turning off release task for current submodule.
* `branches` - per branch configuration. By default, this list has two configurations: first matches master branch, the 
second matches rest of branches.
  * `regexes` - list of regexes for branch names. Default is `mutableListOf("master".toRegex())`. Hint: to avoid a lot 
  of escape symbols use triple double-quotes in Kotlin
  * `incrementer` - default incrementer for the matched branch. Default value is `Incrementer.Minor`. Could be Major, Minor, Patch. Each branch could have its own
   default version increment strategy. Master and feature branches could increment their minor versions (0.1.0 -> 0.2.0).
   Release and fix branches could increment patch versions (0.1.0 -> 0.1.1) 
  * `stringify` - version string configuration. Version name could include branch name, snapshot sign, dirty sign, 
  commit SHA signature and timestamp. There is a builder `smartVersionStringifier(useBranch, useSnapshot, useDirty, useSha, useTimestamp, timeZone)`, but if it is not 
  enough the `stringify` property is of `(DetailedVersion) -> String` type, so **implementation could be provided in place**. 
  Like:
  ```kotlin
  stringify = { version: DetailedVersion -> "here-could-be-your-prefix-${version.sha}"}
  ``` 

### Stringifier
This function is responsible for creating string version generator, that will be used in gradle script.

Parameters:
* `useBranch` if true (default) version includes branch name
* `useSnapshot` if true (default) and current commit is not tagged by version tag, then version includes `-SNAPSHOT` suffix
* `useDirty` if true (default) and there are uncommitted changes, then version includes `-dirty` suffix
* `useSha` if null (default) version includes commit SHA if it is a snapshot. If true version always includes commit SHA
* `useTimestamp` if null (default) version includes timestamp if it is dirty. If true version always includes timestamp
* `timeZone` time zone used in timestamp. Default value is system default. Most likely alternative is `jata.time.ZoneOffset.UTC`

Examples for configurations and output (SHA is shortened for brevity)

|                                         | Released (the tag is present on current commit)           | Snapshot (current commit is ahead of tag)                                 | Dirty (uncommitted changes)                                                                         |
|---------                                |--------------                                             |-----------------------------------------------------------                |--------------------------------------------------------------------------------|
| default                                 | `0.4.0-master`                                            | `0.4.0-master-SNAPSHOT-dbef6a`                                            | `0.4.0-master-SNAPSHOT-dbef6a-dirty-2020-05-16T20-34-46.771+03-00[Europe-Kiev]`|
| `useBranch = false`                     | `0.4.0`                                                   | `0.4.0-SNAPSHOT-dbef6a`                                                   | `0.4.0-SNAPSHOT-dbef6a-dirty-2020-05-16T20-34-46.771+03-00[Europe-Kiev]`       |
| `useSnapshot = false`                   | `0.4.0-master`                                            | `0.4.0-master-dbef6a`                                                     | `0.4.0-master-dbef6a-dirty-2020-05-16T20-34-46.771+03-00[Europe-Kiev]`         |
| `useDirty = false`                      | `0.4.0-master`                                            | `0.4.0-master-SNAPSHOT-dbef6a`                                            | `0.4.0-master-SNAPSHOT-dbef6a-2020-05-16T20-34-46.771+03-00[Europe-Kiev]`      |
| `useSha = true`                         | `0.4.0-master-dbef6a`                                     | `0.4.0-master-SNAPSHOT-dbef6a`                                            | `0.4.0-master-SNAPSHOT-dbef6a-2020-05-16T20-34-46.771+03-00[Europe-Kiev]`      |
| `useTimestamp = true`                   | `0.4.0-master-2020-05-16T20-34-46.771+03-00[Europe-Kiev]` | `0.4.0-master-SNAPSHOT-dbef6a-2020-05-16T20-34-46.771+03-00[Europe-Kiev]` | `0.4.0-master-SNAPSHOT-dbef6a-2020-05-16T20-34-46.771+03-00[Europe-Kiev]`      |
| `useTimestamp = false, useSha = false`  | `0.4.0-master`                                            | `0.4.0-master-SNAPSHOT`                                                   | `0.4.0-master-SNAPSHOT-dirty`                                                  |
