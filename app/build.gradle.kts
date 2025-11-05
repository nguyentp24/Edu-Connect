plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.educonnect"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.educonnect"
        minSdk = 33
        targetSdk = 36
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

    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
// Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.retrofit2:converter-gson:2.4.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
}