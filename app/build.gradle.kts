plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "in.jyotirmoy.attendx"
    compileSdk = 36

    defaultConfig {
        applicationId = "in.jyotirmoy.attendx"
        minSdk = 26
        targetSdk = 36
        versionCode = 27
        versionName = "2.4.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    splits {
        abi {
            isEnable = true
            reset()
            // 64-bit architectures for Android 15+ compatibility
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            // Universal APK for max compatibility
            isUniversalApk = true
        }
    }

    signingConfigs {
        create("release") {
            if (System.getenv("CI")?.toBoolean() == true) {
                // CI/CD signing
                val keystorePath = System.getenv("KEYSTORE_PATH")
                val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
                val keyAlias = System.getenv("KEY_ALIAS")
                val keyPassword = System.getenv("KEY_PASSWORD") ?: keystorePassword

                if (
                    !keystorePath.isNullOrBlank() &&
                    !keystorePassword.isNullOrBlank() &&
                    !keyAlias.isNullOrBlank() &&
                    !keyPassword.isNullOrBlank()
                ) {
                    storeFile = file(keystorePath)
                    storePassword = keystorePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                }
            } else {
                // Local signing
                val keystoreFile = rootProject.file("attendx-release-key.jks")
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    storePassword = "JyotirmoyDas05"
                    keyAlias = "attendx"
                    keyPassword = "JyotirmoyDas05"
                }
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin{
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.navigation.compose)

    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.compose.animation)

    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.work)

    implementation(libs.serialization.json)
    implementation(libs.serialization.json)
    implementation(libs.gson)
    implementation(libs.coil.compose)
    implementation(libs.datastore.preferences)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.slf4j.android)

    implementation(libs.androidx.security.crypto)

    implementation(libs.lottie.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
}