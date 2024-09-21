import com.github.mfarsikov.kewt.versioning.plugin.Incrementer

plugins {
  `java-gradle-plugin`
  id("org.jetbrains.kotlin.jvm") version "2.0.20"
  id("com.gradle.plugin-publish") version "1.3.0"
  id("maven-publish")
  id("com.github.mfarsikov.kewt-versioning") version "1.5.0"
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
  mavenCentral()
}

dependencies {
  implementation("org.eclipse.jgit:org.eclipse.jgit:7.0.0.202409031743-r")
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

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

gradlePlugin {
  website = "https://github.com/mfarsikov/kewt-versioning"
  vcsUrl = "https://github.com/mfarsikov/kewt-versioning"
  plugins {
    matching { it.name == "kewtVersioning" }.configureEach {
      id = "com.github.mfarsikov.kewt-versioning"
      displayName = "Kewt versioning"
      description = "Gradle plugin for versioning using Git tags"
      tags = listOf("git", "versioning")
      implementationClass = "com.github.mfarsikov.kewt.versioning.plugin.KewtVersioningPlugin"
    }
  }
}
project.ext["gradle.publish.key"] = System.getenv("GRADLE_PUBLISH_KEY")
project.ext["gradle.publish.secret"] = System.getenv("GRADLE_PUBLISH_SECRET")
