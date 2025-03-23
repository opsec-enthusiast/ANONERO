plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.anonero"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.anonero"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
        packaging {
            jniLibs.useLegacyPackaging = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".stagenet.debug"
            versionNameSuffix = "-stagenet"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        create("stageNet") {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".stagenet"
            versionNameSuffix = "-stagenet"
            signingConfig = signingConfigs.getByName("debug")
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
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }

    }

    flavorDimensions += "anon_mode"

    productFlavors {
        create("anon") {
            applicationIdSuffix = ".anon"
            resValue("string", "app_name", "ANON")
            dimension = "anon_mode"
            buildConfigField("String", "FLAVOR", "\"anon\"")
        }
        create("nero") {
            applicationIdSuffix = ".nero"
            resValue("string", "app_name", "NERO")
            dimension = "anon_mode"
            buildConfigField("String", "FLAVOR", "\"nero\"")
        }

    }
    buildToolsVersion = "34.0.0"
    ndkVersion = "25.1.8937393"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.animation)

    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.datastore.preferences)


    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.datastore.preferences.core.jvm)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.camera.view)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.core.coroutines)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.navigation)
    implementation(libs.koin.androidx.compose)

    implementation(libs.org.jetbrains.kotlin.android)

    implementation(libs.org.boofcv.boofcv.core)
    implementation(libs.com.google.zxing.core)
    implementation(libs.com.google.accompanist.permissions)

    implementation(libs.com.jakewharton.timber)
    implementation(libs.org.bouncycastle.bcprov)

    implementation(libs.io.matthewnelson.kmp.tor.runtime)
    implementation(libs.io.matthewnelson.kmp.resource.exec.tor)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}