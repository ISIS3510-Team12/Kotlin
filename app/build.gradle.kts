plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.team12kotlin.juggle"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.team12kotlin.juggle"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000\"")
        buildConfigField("boolean", "USE_FIREBASE_EMULATOR", "true")
        buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"10.0.2.2\"")
        buildConfigField("int", "FIREBASE_AUTH_EMULATOR_PORT", "9099")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"932315820701-t6sefi9e45vec6r72j7j1bgfjds0c6ai.apps.googleusercontent.com\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.navigation:navigation-compose:2.9.4")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.12.0")
    implementation("com.composables:icons-material-symbols-outlined-cmp:2.2.1")
    implementation("com.composables:icons-material-symbols-rounded-cmp:2.2.1")
    implementation("com.composables:icons-material-symbols-sharp-cmp:2.2.1")
    implementation("com.composables:icons-material-symbols-outlined-filled-cmp:2.2.1")
    implementation("com.composables:icons-material-symbols-rounded-filled-cmp:2.2.1")
    implementation("com.composables:icons-material-symbols-sharp-filled-cmp:2.2.1")
    implementation(libs.androidx.material3)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.identity.googleid)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}