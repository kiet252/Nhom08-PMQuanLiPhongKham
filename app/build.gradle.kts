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

val geminiApiKey = localProperties
    .getProperty("GEMINI_API_KEY")
    ?.normalizeSecretValue()
    ?: ""

android {
    namespace = "com.example.nhom08_quanlyphongkham"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.nhom08_quanlyphongkham"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${supabaseAnonKey.escapeForBuildConfig()}\"")

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${geminiApiKey.escapeForBuildConfig()}\""
        )

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
    packaging {
        resources {
            // Loại bỏ các file INDEX.LIST gây xung đột
            excludes += "/META-INF/INDEX.LIST"

            // Loại bỏ thêm các file meta-data khác thường xuyên gây lỗi khi dùng thư viện Google
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/io.netty.versions.properties"
            excludes += "/META-INF/INDEX.LIST"
        }
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

    // ML Kit Face Detection
    implementation("com.google.mlkit:face-detection:16.1.5")

    // TensorFlow Lite (for embedding model inference)
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    // Use the support API artifact only to avoid manifest namespace duplication between AARs
    implementation("org.tensorflow:tensorflow-lite-support-api:0.4.4")

    implementation("io.coil-kt:coil:2.4.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    //Nhận diện OCR
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    implementation("com.google.mlkit:text-recognition:16.0.1")

    //Gemini
    implementation("com.google.genai:google-genai:1.16.0")

    //Xuất PDF
    implementation("com.itextpdf:itext7-core:7.2.5")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
