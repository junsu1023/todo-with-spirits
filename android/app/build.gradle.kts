import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}
val kakaoNativeAppKey: String = localProperties.getProperty("KAKAO_NATIVE_API_KEY", "")
val googleWebClientId: String = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")

android {
    signingConfigs {
        create("release") {
            val keystorePath = localProperties.getProperty("RELEASE_KEYSTORE_PATH", "")
            if (keystorePath.isNotBlank()) {
                storeFile = file(keystorePath)
                storePassword = localProperties.getProperty("RELEASE_KEYSTORE_PASSWORD", "")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS", "")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD", "")
            }
        }
    }

    namespace = "com.example.todowithspirits"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.todowithspirits"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")
        // AndroidManifest.xml의 카카오 로그인 리다이렉트 scheme(kakao{NATIVE_APP_KEY})에서 사용.
        manifestPlaceholders["kakaoNativeAppKey"] = kakaoNativeAppKey

        // Google Cloud Console에 등록한 "웹 애플리케이션" 타입 OAuth 클라이언트 ID (Android 타입 아님).
        // Credential Manager가 발급하는 idToken의 aud 클레임이 되고, 서버가 그 값으로 우리 백엔드용 토큰인지 검증한다.
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.runtime)
    ksp(libs.hilt.compiler)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Kakao
    implementation(libs.kakao.sdk.user)

    // Google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.id)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}