import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }

    dependencies { classpath(Deps.kotlinGradlePlugin) }
}

repositories {
    google()
    jcenter()
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
}

plugins { kotlin("jvm") version Vers.kotlin }

dependencies {
    implementation(Deps.kotlinStdlib8)
    implementation(Deps.kotlinxCoroutinesCore)
    implementation(Deps.kotlinxCoroutinesRx2)
    testImplementation(Deps.junit5)
    testImplementation(Deps.uspek)
    testImplementation(Deps.smokk)
    testImplementation(kotlin("script-runtime"))
}

tasks.test { useJUnitPlatform() }

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions { jvmTarget = "1.8" }

val compileTestKotlin: KotlinCompile by tasks

compileTestKotlin.kotlinOptions { jvmTarget = "1.8" }