package com.github.mfarsikov.kewt.versioning.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.slf4j.LoggerFactory
import java.io.File

class GitReader(
        gitPath: File
) {
    private val logger = LoggerFactory.getLogger(GitReader::class.java)
    private val git = Git.open(gitPath)
    private val tagPrefix = "refs/tags/"

    fun status(): GitStatus {
        val commitId = git.repository.resolve("HEAD")
        val allTags = git.tagList().call()

        val commitIds = RevWalk(git.repository).use {
            allCommitIds(commitId, it).toSet()
        }

        val branchTags = allTags
                .filter { it.objectId in commitIds }
                .map { it.name.substringAfter(tagPrefix) }

        val commitTags = allTags.filter { it.objectId == commitId }.map { it.name.substringAfter(tagPrefix) }
        val isDirty = git.status().call().isClean.not()

        return GitStatus(
                branch = git.repository.branch,
                branchTags = branchTags,
                isDirty = isDirty,
                commitTags = commitTags,
                sha = commitId.name
        ).also {
            logger.debug("Git status: $it")
        }
    }

    private fun allCommitIds(childCommitId: ObjectId, revWalk: RevWalk): List<ObjectId> {
        val commit = revWalk.parseCommit(childCommitId)
        return commit.parents
                .flatMap { allCommitIds(it.id, revWalk) } + commit.id
    }

    fun tag(tagName: String) {
        if (git.tagList().call().any { it.name == "$tagPrefix$tagName" }) {
            println("Tag $tagName already exists. Have another gradle submodule just created it? If so, the release task can be turned of for this sub module")
        } else {
            git.tag().setName(tagName).setAnnotated(false).call().also {
                println("Tag created: ${it.name.substringAfter(tagPrefix)}")
            }
        }
    }
}
