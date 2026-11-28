plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val distributionStorePath = System.getenv("MATHFLOW_KEYSTORE_PATH")
val distributionStorePassword = System.getenv("MATHFLOW_KEYSTORE_PASSWORD")
val distributionKeyAlias = System.getenv("MATHFLOW_KEY_ALIAS")
val distributionKeyPassword = System.getenv("MATHFLOW_KEY_PASSWORD")
val useDistributionSigning = listOf(
    distributionStorePath,
    distributionStorePassword,
    distributionKeyAlias,
    distributionKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.mathflow.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mathflow.app"
        minSdk = 26
        targetSdk = 35
        versionCode = providers.environmentVariable("GITHUB_RUN_NUMBER").orNull?.toIntOrNull() ?: 14
        versionName = "1.6.0"
    }

    signingConfigs {
        if (useDistributionSigning) {
            create("distribution") {
                storeFile = file(distributionStorePath!!)
                storePassword = distributionStorePassword
                keyAlias = distributionKeyAlias
                keyPassword = distributionKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName(if (useDistributionSigning) "distribution" else "debug")
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName(if (useDistributionSigning) "distribution" else "debug")
        }

        // R8-optimized primary build. It deliberately shares Release's application ID
        // and signing identity so installing Benchmark updates the regular MathFlow app.
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName(if (useDistributionSigning) "distribution" else "debug")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            matchingFallbacks += listOf("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    // Kyant Backdrop provides GPU-based refraction, lens distortion and glass highlights.
    // Backdrop 2.0.0 is vendored under Apache-2.0 in com.kyant.backdrop.
    implementation("io.github.kyant0:shapes:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
