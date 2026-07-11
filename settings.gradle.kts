
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
  id("pl.mareklangiewicz.deps.settings") version "0.4.22" // https://plugins.gradle.org/search?term=mareklangiewicz
  id("com.gradle.develocity") version "4.5.0" // https://docs.gradle.com/develocity/gradle-plugin/
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    publishing.onlyIf { allowBuildScanPublish && it.buildResult.failures.isNotEmpty() }
  }
}

// endregion [[My Settings Stuff]]

include(":coedges")
