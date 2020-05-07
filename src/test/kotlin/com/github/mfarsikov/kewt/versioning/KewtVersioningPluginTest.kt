package com.github.mfarsikov.kewt.versioning

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class KewtVersioningPluginTest {
    @Test
    fun `plugin registers task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.github.mfarsikov.kewt.versioning.greeting")

        assertNotNull(project.tasks.findByName("greeting"))
    }
}
