plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.kangrio.shellserver"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        consumerProguardFiles("proguard-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        aidl = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation("dev.mobile:dadb:1.2.10")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.1")
}