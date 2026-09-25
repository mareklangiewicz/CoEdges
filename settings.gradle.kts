@file:Suppress("UnstableApiUsage")

import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.extLib

rootProject.name = "CoEdges"


// WARNING: Careful with auto publishing fails/stack traces (also on github after each push or sth)
val isCI = System.getenv("GITHUB_ACTIONS") == "true"
val allowBuildScanPublish = isCI
// val allowBuildScanPublish = !isCI
// val allowBuildScanPublish = false

// region [[My Settings Stuff]]

// https://docs.gradle.org/current/userguide/upgrading_version_9.html#opt_into_gradle_10_behavior_by_disabling_implicit_lookup_in_parent_projects
enableFeaturePreview("NO_IMPLICIT_LOOKUP_IN_PARENT_PROJECTS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  // Opt-in through the environment, so this region is identical in every project and no flag has
  // to live above it and be kept in sync. Unset means off. To enable for one run:
  //   ENABLE_LOCAL_DEPSKT_IN_DIR=/home/marek/code/kotlin/DepsKt ./gradlew build
  val enableLocalDepsKtInDir = System.getenv("ENABLE_LOCAL_DEPSKT_IN_DIR")?.let { File(it).normalize() }
  // The env var reaches nested builds too, so DepsKt's own copy of this region sees it: skip self.
  // Pass a String: this scope's includeBuild takes only String, and a File silently resolves to the
  // outer Settings.includeBuild, a plain composite that never offers DepsKt's PLUGINS.
  if (enableLocalDepsKtInDir != null && enableLocalDepsKtInDir != rootDir.normalize()) {
    logger.warn("Including local build $enableLocalDepsKtInDir")
    includeBuild(enableLocalDepsKtInDir.path)
  }
}

plugins {
  id("pl.mareklangiewicz.deps.settings") version "0.4.66" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "4.6.0" // https://docs.gradle.com/develocity/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    // Opt-in through the environment; unset means no scan is ever published, which is what keeps
    // private repos safe without anyone remembering to switch them off. A public repo turns it on
    // in its own CI workflow:  ENABLE_BUILD_SCAN_PUBLISHING_ON_FAILURE=true
    // Read into a local at configuration time: `onlyIf` runs at the END of the build, and reading
    // a settings-script top-level `val` from there would capture the script OBJECT, which the
    // configuration cache rejects. A local is captured by value.
    val enabled = System.getenv("ENABLE_BUILD_SCAN_PUBLISHING_ON_FAILURE") == "true"
    publishing.onlyIf { enabled && it.buildResult.failures.isNotEmpty() }
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
