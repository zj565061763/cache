plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.sd.demo.cache"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        targetSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = 21
        applicationId = "com.sd.demo.cache"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":lib"))

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.cash.turbine)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines)
}
