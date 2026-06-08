import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

fun String.escapeForBuildConfig(): String {
    return replace("\\", "\\\\").replace("\"", "\\\"")
}

fun String.normalizeSecretValue(): String {
    val trimmed = trim()
    return if (
        trimmed.length >= 2 &&
        ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
         (trimmed.startsWith("'") && trimmed.endsWith("'")))
    ) {
        trimmed.substring(1, trimmed.length - 1).trim()
    } else {
        trimmed
    }
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val supabaseAnonKey = listOfNotNull(
    System.getenv("SUPABASE_ANON_KEY"),
    localProperties.getProperty("SUPABASE_ANON_KEY"),
    localProperties.getProperty("abAIkey")
).firstOrNull { it.isNotBlank() }?.normalizeSecretValue()
    ?: error("Missing SUPABASE_ANON_KEY. Add it to local.properties or set the SUPABASE_ANON_KEY environment variable.")

android {
    namespace = "com.example.nhom08_quanlyphongkham"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.nhom08_quanlyphongkham"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${supabaseAnonKey.escapeForBuildConfig()}\"")

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

        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.5.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.5.0")
    implementation("io.github.jan-tennert.supabase:supabase-kt:3.5.0")

    // CameraX dependencies
    val camerax_version = "1.3.4"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("io.coil-kt:coil:2.4.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
