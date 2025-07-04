// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.0.0" apply false
    id("com.android.library") version "8.0.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false   
    id("com.google.gms.google-services") version "4.4.2" apply false     
    id("com.google.dagger.hilt.android") version "2.44.2" apply false // Add Hilt plugin
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}