import com.github.mfarsikov.kewt.versioning.plugin.Incrementer

plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
    id("com.gradle.plugin-publish") version "0.14.0"
    id("maven-publish")
    id("com.github.mfarsikov.kewt-versioning") version "0.10.2"
}

group = "com.github.mfarsikov.kewt-versioning"
kewtVersioning {
    configuration {
        branches {
            clear()
            add {
                regexes = mutableListOf("master".toRegex())
                incrementer = Incrementer.MINOR
                stringify = stringifier(useBranch = false, useSha = false, useTimestamp = false)
            }
            add {
                regexes = mutableListOf("fix/.*".toRegex())
                incrementer = Incrementer.PATCH
                stringify = stringifier(useSha = false, useTimestamp = false)
            }
            add {
                regexes = mutableListOf(".*".toRegex())
                incrementer = Incrementer.MINOR
                stringify = { version ->
                    stringifier(
                        useBranch = version.isSnapshot,
                        useSha = false,
                        useTimestamp = false
                    )(version)
                }
            }
        }
    }
}
version = kewtVersioning.version

repositories {
    jcenter()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.11.0.202103091610-r")
}

gradlePlugin {
    val kewtVersioning by plugins.creating {
        id = "com.github.mfarsikov.kewt-versioning"
        displayName = "Kewt versioning"
        implementationClass = "com.github.mfarsikov.kewt.versioning.plugin.KewtVersioningPlugin"
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val check by tasks.getting(Task::class) {
    dependsOn(functionalTest)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
}

val compiler = javaToolchains.compilerFor {
    languageVersion.set(JavaLanguageVersion.of(8))
    vendor.set(JvmVendorSpec.ADOPTOPENJDK)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jdkHome = compiler.get().metadata.installationPath.asFile.absolutePath
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
        sourceCompatibility = "1.8"
    }
}

pluginBundle {
    website = "https://github.com/mfarsikov/kewt-versioning"
    vcsUrl = "https://github.com/mfarsikov/kewt-versioning"
    description = "Gradle plugin for versioning using Git tags"
    tags = listOf("git", "versioning")
}
project.ext["gradle.publish.key"] = System.getenv("GRADLE_PUBLISH_KEY")
project.ext["gradle.publish.secret"] = System.getenv("GRADLE_PUBLISH_SECRET")
