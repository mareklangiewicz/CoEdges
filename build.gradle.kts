
// region [[Basic Root Build Imports and Plugs]]

import pl.mareklangiewicz.defaults.*
import pl.mareklangiewicz.deps.*
import pl.mareklangiewicz.utils.*

plugins {
    plug(plugs.KotlinMulti) apply false
    plug(plugs.KotlinJvm) apply false
}

// endregion [[Basic Root Build Imports and Plugs]]

val enableJs = true
val enableNative = true

defaultBuildTemplateForRootProject(
    myLibDetails(
        name = "CoEdges",
        description = "Kotlin Coroutines Edges.",
        githubUrl = "https://github.com/langara/CoEdges",
        version = Ver(0, 0, 7),
        // https://central.sonatype.com/artifact/pl.mareklangiewicz/coedges
        // https://github.com/mareklangiewicz/CoEdges/releases
        settings = LibSettings(
            withJs = enableJs,
            withLinuxX64 = enableNative,
            compose = null,
            withCentralPublish = true,
        ),
    ),
)

// region [[Root Build Template]]

fun Project.defaultBuildTemplateForRootProject(details: LibDetails? = null) {
  details?.let {
    rootExtLibDetails = it
    defaultGroupAndVerAndDescription(it)
  }
}

// endregion [[Root Build Template]]
