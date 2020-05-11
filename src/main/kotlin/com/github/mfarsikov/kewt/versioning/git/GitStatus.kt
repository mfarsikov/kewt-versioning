package com.github.mfarsikov.kewt.versioning.git

data class GitStatus(
        val branch: String,
        val branchTags: List<String>,
        val isDirty: Boolean,
        val commitTags: List<String>,
        val sha: String
)