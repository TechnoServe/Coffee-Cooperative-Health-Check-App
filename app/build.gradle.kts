

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id ("dagger.hilt.android.plugin")


}

android {
    namespace = "com.technoserve.cooptrac"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.technoserve.cooptrac"
        minSdk = 24
        targetSdk = 34
        versionCode = 20
        versionName = "6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xopt-in=kotlin.contracts.ExperimentalContracts"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
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
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation("com.opencsv:opencsv:5.6")
    implementation ("androidx.activity:activity-ktx:1.7.0")
    implementation ("androidx.activity:activity:1.7.0")
    implementation ("com.google.code.gson:gson:2.8.2")
    implementation ("androidx.compose.runtime:runtime-livedata:1.2.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation ("com.google.dagger:hilt-android:2.44")
    implementation ("androidx.compose.ui:ui:1.4.0")
    implementation ("androidx.core:core-ktx:1.9.0")
    implementation("androidx.compose.foundation:foundation-layout:1.0.5")

    kapt ("com.google.dagger:hilt-compiler:2.44")
    implementation(libs.identity.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Extensions = ViewModel + LiveData
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.room:room-ktx:$roomVersion")

    implementation("androidx.room:room-rxjava2:$roomVersion")

    implementation("androidx.room:room-rxjava3:$roomVersion")

    implementation("androidx.room:room-guava:$roomVersion")

    testImplementation("androidx.room:room-testing:$roomVersion")

    implementation("androidx.room:room-paging:$roomVersion")


}