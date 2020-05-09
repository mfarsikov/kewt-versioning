package com.github.mfarsikov.kewt.versioning

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.Test
import kotlin.test.assertTrue

class KewtVersioningPluginFunctionalTest {
    @Test fun `can run task`() {
        val projectDir = File("build/functionalTest")
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle").writeText("""
            plugins {
                id('com.github.mfarsikov.kewt.versioning.greeting')
            }
        """)

        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("greeting")
        runner.withProjectDir(projectDir)
        val result = runner.build();

        assertTrue(result.output.contains("rst"))
    }
}
