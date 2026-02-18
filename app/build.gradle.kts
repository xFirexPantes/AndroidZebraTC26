
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    id("kotlin-kapt")
}

android {
    namespace = "com.example.scanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.scanner"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // 1. Определяем измерение для версий
    flavorDimensions.add("version")

    productFlavors {
        create("version170") {
            dimension = "version"
            versionName = "1.7.0"
        }
        create("version171") {
            dimension = "version"
            versionName = "1.7.1"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        viewBinding = true
        buildConfig = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation (libs.rxkotlin)
    implementation (libs.timber)
    implementation (libs.treessence)
    implementation(libs.androidx.activity)

    implementation(libs.emdk)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.annotation)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation(libs.jcifs)
    implementation (libs.kotlin.reflect)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.photoview)
    implementation(libs.picasso)
    implementation(libs.commons.io)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.tink.android)
    implementation(libs.kotlinx.serialization.json)


}