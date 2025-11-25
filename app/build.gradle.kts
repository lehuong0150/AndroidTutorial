plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.eco.musicplayer.audioplayer.music"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.eco.musicplayer.audioplayer.music"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        multiDexEnabled = true

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
    sourceSets {
        getByName("main") {
            assets.srcDir("src/main/assets")
            res.srcDir("src/main/res/main")
            res.srcDir("src/main/res/event")
            res.srcDir("src/main/res/service")
            res.srcDir("src/main/res/roomdb")
            res.srcDir("src/main/res/contentprovider")
            res.srcDir("src/main/res/ads")

        }
    }
}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.material:material:1.11.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.castorflex.smoothprogressbar:library:1.1.0")
    implementation("com.airbnb.android:lottie:6.6.0")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation(project(":billing"))
    implementation("com.android.billingclient:billing-ktx:8.0.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    //add remote config
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //view model
    implementation("androidx.activity:activity-ktx:1.9.2")
    //event bus
    implementation("org.greenrobot:eventbus:3.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    // Lifecycle + ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.fragment:fragment-ktx:1.8.4")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    //ads
    implementation("com.google.android.gms:play-services-ads:24.8.0")




}