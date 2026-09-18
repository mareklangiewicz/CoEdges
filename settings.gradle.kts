@file:Suppress("UnstableApiUsage")

import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLib

rootProject.name = "CoEdges"


// WARNING: Careful with auto publishing fails/stack traces (also on github after each push or sth)
val isCI = System.getenv("GITHUB_ACTIONS") == "true"
val allowBuildScanPublish = isCI
// val allowBuildScanPublish = !isCI
// val allowBuildScanPublish = false

// region [[My Settings Stuff <~~]]
// ~~>".*/Deps\.kt"~~>"../DepsKt"<~~
// endregion [[My Settings Stuff <~~]]
// region [[My Settings Stuff]]

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  val depsDir = File(rootDir, "../DepsKt").normalize()
  val depsInclude =
    // depsDir.exists()
    false
  if (depsInclude) {
    logger.warn("Including local build $depsDir")
    includeBuild(depsDir)
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.4.63" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "4.5.1" // https://docs.gradle.com/develocity/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { allowBuildScanPublish && it.buildResult.failures.isNotEmpty() }
  }
}

// endregion [[My Settings Stuff]]

val enableJs = true
val enableNative = true

gradle.extLib = lib(
  info = myLibInfo(
    name = "CoEdges",
    description = "Kotlin Coroutines Edges.",
    githubUrl = "https://github.com/langara/CoEdges",
    version = Ver(0, 0, 7),
    // https://central.sonatype.com/artifact/pl.mareklangiewicz/coedges
    // https://github.com/mareklangiewicz/CoEdges/releases
  ),
  flags = LibFlags(
    withJs = enableJs,
    withLinuxX64 = enableNative,
    // withCentralPublish is GONE from LibFlags as of DepsKt 0.4.63. Publish intent is a per-MODULE
    // value now, so :coedges opts in with LibPublish(toCentral = true) in its own build script.
    // See DepsKt/docs/design/publish-intent-per-module.md.
  ),
  withCompose = false, // was: compose = null
  // andro is absent by default
)

include(":coedges")
