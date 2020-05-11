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

    fun status(): GitStatus {
        val commitId = git.repository.resolve("HEAD")
        val allTags = git.tagList().call()

        val commitIds = RevWalk(git.repository).use {
            allCommitIds(commitId, it).toSet()
        }

        val branchTags = allTags
                .filter { it.objectId in commitIds }
                .map { it.name.substringAfter("refs/tags/") }

        val commitTags = allTags.filter { it.objectId == commitId }.map { it.name.substringAfter("refs/tags/") }
        val isDirty = !git.status().call().isClean

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

    private fun allCommitIds(childCommitId: ObjectId, revWalk: RevWalk): List<ObjectId> = revWalk
            .parseCommit(childCommitId)
            .parents
            .flatMap { allCommitIds(it.id, revWalk) } + revWalk.parseCommit(childCommitId).id

    fun tag(tagName: String) {
        git.tag().setName(tagName).setAnnotated(false).call().also {
            println("Tag created: ${it.name}")
        }
    }
}
