plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

val unsplashAccessKey: String = project.findProperty("unsplashAccessKey") as? String
    ?: error("unsplashAccessKey is missing in gradle.properties")

val unsplashSecretKey: String = project.findProperty("unsplashSecretKey") as? String
    ?: error("unsplashSecretKey is missing in gradle.properties")

android {
    namespace = "kz.vrstep.countrytinder"
    compileSdk = 35

    defaultConfig {
        applicationId = "kz.vrstep.countrytinder"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "UNSPLASH_ACCESS_KEY", "\"$unsplashAccessKey\"")
        buildConfigField("String", "UNSPLASH_SECRET_KEY", "\"$unsplashSecretKey\"")

        // Retrieve API key from gradle.properties and expose it via BuildConfig
        val unsplashAccessKey: String = project.findProperty("unsplashAccessKey") as? String
            ?: error("unsplashAccessKey is missing in gradle.properties")
        // The Unsplash Secret Key is typically not needed for client-side image search.
        // val unsplashSecretKey: String = project.findProperty("UNSPLASH_SECRET_KEY") as? String
        //     ?: error("UNSPLASH_SECRET_KEY is missing in gradle.properties")

        buildConfigField("String", "unsplashAccessKey", "\"$unsplashAccessKey\"")
        // buildConfigField("String", "UNSPLASH_SECRET_KEY", "\"$unsplashSecretKey\"") // If needed later
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Room dependencies
    val room_version = "2.7.1"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Koin dependencies
    val koin_version = "4.0.4"
    implementation("io.insert-koin:koin-android:$koin_version")
    implementation("io.insert-koin:koin-androidx-compose:$koin_version")

    // Retrofit dependencies
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil dependencies
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0") // Only available on Android/JVM.
    implementation("io.coil-kt.coil3:coil-network-ktor2:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-ktor3:3.2.0")

    // Compose navigation
    val nav_version = "2.9.0"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Jetpack Compose
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // Lifecycle Viewmodel
    val lifecycle_version = "2.9.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version")



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}