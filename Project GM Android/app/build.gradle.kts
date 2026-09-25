plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "pab.rpg.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "pab.rpg.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        // "localhost" here is the DEVICE's own localhost, not the host machine's — on the emulator that's
        // moot (10.0.2.2 would also work), but on a real USB-connected device this ONLY works after running
        // "adb reverse tcp:8080 tcp:8080" (forwards the device's localhost:8080 to the host machine's),
        // which is required either way. See Project GM Auth/docs-agent/testing.md.
        buildConfigField("String", "API_BASE_URL", "\"http://localhost:8080/project_gm/\"")
    }

    flavorDimensions += "environment"
    productFlavors {
        // Default variant: unchanged DevIdentityInterceptor-only behavior, no login screen.
        create("local") {
            dimension = "environment"
        }
        // Drives a native username/password login against Project GM Auth's custom "password" grant (see
        // CloudAuthSessionManager) — no browser/Custom Tabs involved, just a direct POST to /oauth2/token.
        // Also needs "adb reverse tcp:9000 tcp:9000" on a real USB-connected device (see API_BASE_URL above).
        create("cloud") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"http://localhost:8080/project_gm/\"")
            buildConfigField("String", "AUTH_BASE_URL", "\"http://localhost:9000/\"")
            buildConfigField("String", "AUTH_CLIENT_ID", "\"project-gm-android\"")
            // Not a real secret (ships inside the APK) — see AuthProperties.androidClientSecret on the backend
            // for why a "public" client still needs one for this custom grant. Must match whichever profile
            // the backend is actually running: AUTH_ANDROID_CLIENT_SECRET in Project GM Auth's .env.local
            // ("local-dev-secret-change-me") vs .env.cloud ("cloud-dry-run-secret-change-me").
            buildConfigField("String", "AUTH_CLIENT_SECRET", "\"cloud-dry-run-secret-change-me\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Only the "cloud" flavor needs encrypted token storage.
    "cloudImplementation"(libs.androidx.security.crypto)
}
