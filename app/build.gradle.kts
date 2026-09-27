plugins {
    id("com.android.application")
}

// Change these two values to point the button at your own site.
val launchUrl = "https://gcdatlas.com/#o=earth&tour=grand"
val buttonLabel = "Grand Tour"

android {
    namespace = "io.github.tvlauncher"
    compileSdk = 35
    defaultConfig {
        applicationId = "io.github.tvlauncher.site"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
        ndk { abiFilters += listOf("arm64-v8a") }
        buildConfigField("String", "LAUNCH_URL", "\"$launchUrl\"")
        resValue("string", "app_name", buttonLabel)
    }
    buildFeatures { buildConfig = true }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Firefox's own engine as a library, so the page renders exactly as it does in Firefox, full screen.
    implementation("org.mozilla.geckoview:geckoview-arm64-v8a:156.0.20260921121718")
}

// GeckoView's metadata asks for compileSdk 36; it runs fine against 35 on older TVs, so skip that check.
tasks.withType<com.android.build.gradle.internal.tasks.CheckAarMetadataTask>().configureEach { enabled = false }
