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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mathflow.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 14
        versionName = "1.5.4"
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
            if (useDistributionSigning) {
                signingConfig = signingConfigs.getByName("distribution")
            }
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
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
