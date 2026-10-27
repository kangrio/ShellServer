plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
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
    publishing {
        singleVariant("release")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation("dev.mobile:dadb:1.2.10")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.1")
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.kangrio"
                artifactId = "ShellServer"
                version = "1.0.2"
            }
        }
    }
}