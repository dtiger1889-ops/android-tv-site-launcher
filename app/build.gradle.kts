plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Change these three values to point the button at your own site and browser.
val launchUrl = "https://gcdatlas.com/#o=earth&tour=grand"
val browserPackage = "org.mozilla.firefox"
val buttonLabel = "Grand Tour"

android {
    namespace = "io.github.tvlauncher"
    compileSdk = 35
    defaultConfig {
        applicationId = "io.github.tvlauncher.site"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "LAUNCH_URL", "\"$launchUrl\"")
        buildConfigField("String", "BROWSER_PACKAGE", "\"$browserPackage\"")
        resValue("string", "app_name", buttonLabel)
    }
    buildFeatures { buildConfig = true }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
