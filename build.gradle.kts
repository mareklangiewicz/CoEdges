buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath(Deps.kotlinGradlePlugin)
    }
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
