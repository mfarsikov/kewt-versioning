package com.github.mfarsikov.kewt.versioning.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import java.io.File

class GitReader(
    private val gitPath: File,
    private val remoteName: String?,
    private val user: String?,
    private val password: String?
) {
    private val logger = LoggerFactory.getLogger(GitReader::class.java)
    private val tagPrefix = "refs/tags/"

    fun status(): GitStatus {
        val git = Git.open(gitPath)
        val commitId = git.repository.resolve("HEAD")
        val allTags = git.tagList().call()

        val revWalk = RevWalk(git.repository)
        val commitIds = allCommitIds(commitId, revWalk).toSet()


        val branchTags = allTags
            .filter {
                it.objectId in commitIds || try {
                    revWalk.parseTag(it.objectId).`object`.id in commitIds
                } catch (ex: Exception) {
                    false
                }
            }
            .map { it.name.substringAfter(tagPrefix) }

        revWalk.close()

        val commitTags = allTags.filter { it.objectId == commitId }.map { it.name.substringAfter(tagPrefix) }
        val isDirty = git.status().call().isClean.not()

        return GitStatus(
            branch = git.repository.branch.takeIf { it != commitId.name },
            branchTags = branchTags,
            isDirty = isDirty,
            commitTags = commitTags,
            sha = commitId.name
        ).also {
            logger.debug("Git status: $it")
        }
    }

    private fun allCommitIds(
        childCommitId: ObjectId,
        revWalk: RevWalk,
        visited: MutableSet<ObjectId> = mutableSetOf()
    ): List<ObjectId> {
        visited += childCommitId
        val commit = revWalk.parseCommit(childCommitId)
        return commit.parents
            .filter { it.id !in visited }
            .flatMap { allCommitIds(it.id, revWalk, visited) } + commit.id
    }

    fun tag(tagName: String) {
        val git = Git.open(gitPath)

        if (git.tagList().call().any { it.name == "$tagPrefix$tagName" }) {
            println("Tag $tagName already exists. Have another gradle submodule just created it? If so, the release task can be turned of for this sub module")
        } else {
            git.tag().setName(tagName).setAnnotated(false).call().also {
                println("Tag created: ${it.name.substringAfter(tagPrefix)}")
            }
        }
        if (git.remoteList().call().any { it.name == remoteName }) {
            pushToRemote(tagName)
        }
    }

    private fun pushToRemote(tagName: String) {
        try {
            val git = Git.open(gitPath)
            git.push()
                .setRemote(remoteName)
                .setCredentialsProvider(UsernamePasswordCredentialsProvider(user, password))
                .add(tagName)
                .call()
        } catch (e: TransportException) {
            if (e.message == "not authorized") {
                logger.error("Check kewtVersioning.user and kewtVersioning.password")
            }
            throw e
        }
        logger.info("Pushed $tagName to $remoteName")
    }
}
