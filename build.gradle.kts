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
    testImplementation(Deps.junit)
    testImplementation(Deps.uspek)
}
