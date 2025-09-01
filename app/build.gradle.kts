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
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
        buildConfig = true
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
    implementation ("com.squareup.okhttp3:logging-interceptor:3.12.2")
    implementation("org.samba.jcifs:jcifs:1.3.14-kohsuke-1")
    implementation ("org.jetbrains.kotlin:kotlin-reflect:2.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("io.getstream:photoview:1.0.3")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("commons-io:commons-io:2.19.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


}