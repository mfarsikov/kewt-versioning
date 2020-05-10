package com.github.mfarsikov.kewt.versioning

import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.eclipse.jgit.revwalk.RevWalk
import java.io.File

val git = Git.open(File("/users/mfarsikov/IdeaProjects/kewt-versioning-usage"))
val repository = git.getRepository()
val revWalk = RevWalk(repository)
val commitId = repository.resolve("refs/heads/side-branch")
revWalk.markStart(revWalk.parseCommit(commitId))
revWalk.forEach { println(it.fullMessage) }
revWalk.close()


