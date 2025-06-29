
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
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

    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.5.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    
    
    // --- Compose ViewModel & LiveData ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.3.1")
    
    // --- Compose Material Icons (optional, if you use extended icons) ---
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    
    // --- Firebase BOM & Services ---
    implementation(platform("com.google.firebase:firebase-bom:31.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // --- Hilt (Dependency Injection) ---
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-android-compiler:2.44.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0") {
        exclude(group = "com.google.dagger", module = "hilt-android")
        exclude(group = "com.google.dagger", module = "hilt-core")
    }
    
    // --- Kotlin Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // --- Navigation Compose ---
    implementation("androidx.navigation:navigation-compose:2.5.3")
    
    // --- MultiDex (if needed) ---
    implementation("androidx.multidex:multidex:2.0.1")
    
    implementation("androidx.core:core-splashscreen:1.0.1")
}

