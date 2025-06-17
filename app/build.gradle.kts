
plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.chaosdev.devbuddy"
    compileSdk = 33
    
    defaultConfig {
        applicationId = "com.chaosdev.devbuddy"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        
        compose = true
    }
    
    configurations.all {
       resolutionStrategy {
            // Log what's happening during resolution for debugging (optional)
            // eachDependency { details ->
            //     val requested = details.requested
            //     if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")) {
            //         println("Resolving Kotlin stdlib: ${requested.group}:${requested.name}:${requested.version}")
            //     }
            // }

            // Force all Kotlin standard library modules to the 1.8.10 version.
            // This is the version brought in by lifecycle-viewmodel-compose:2.6.1.
            // By forcing, we ensure consistency across the entire project.
            force("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.10")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10")
            // It's possible you might need to force other related Kotlin libraries
            // if you encounter further conflicts, e.g., kotlinx.coroutines
            // force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            // force("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        }
    }

    
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
}

dependencies {

    implementation(platform("androidx.compose:compose-bom:2022.10.00"))

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.5.1")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.core:core-ktx:1.8.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    
    // Core KTX
    implementation("androidx.core:core-ktx:1.9.0")
    
    // Activity Compose
    implementation("androidx.activity:activity-compose:1.6.1")
    
    // Compose UI
    implementation("androidx.compose.ui:ui:1.3.1")
    
    // Compose UI Util
    implementation("androidx.compose.ui:ui-util:1.3.1")
    
    // Runtime LiveData support
    implementation("androidx.compose.runtime:runtime-livedata:1.3.1")
    
    // Material (Compose MD2)
    //implementation("androidx.compose.material:material:1.3.1")
    
    // Extended Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
}
