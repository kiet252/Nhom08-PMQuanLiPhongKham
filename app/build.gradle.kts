plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nhom08_quanlyphongkham"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.nhom08_quanlyphongkham"
        minSdk = 24
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

    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
// Thư viện Supabase chính (nếu chưa có)
    // 1. Module xử lý Database (Select/Update)
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.5.0")
// 2. Module xử lý Realtime (Bắt buộc phải có cái này mới hết lỗi jan_tennert)
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.5.0")
// 3. Module core để các module trên chạy được
    implementation("io.github.jan-tennert.supabase:supabase-kt:3.5.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.0.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}