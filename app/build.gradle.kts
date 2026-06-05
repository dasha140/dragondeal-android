import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.elvsf.dragondeal"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.elvsf.dragondeal"
        minSdk = 26
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
            val releaseApiUrl = project.findProperty("API_BASE_URL_RELEASE") as String? ?: ""
            buildConfigField("String", "BASE_URL", "\"${releaseApiUrl}\"")
        }
        debug {
            val localPropsFile = rootProject.file("local.properties")
            val localApiUrl = if (localPropsFile.exists()) {
                val props = Properties()
                localPropsFile.inputStream().use { props.load(it) }
                props.getProperty("API_BASE_URL", "http://192.168.0.111:8081/api/")
            } else {
                "http://192.168.0.111:8081/api/"
            }
            buildConfigField("String", "BASE_URL", "\"${localApiUrl}\"")
        }
    }

    // Fail-fast: release build requires API_BASE_URL_RELEASE
    gradle.taskGraph.whenReady {
        val releaseTasks = allTasks.filter { it.name.lowercase().contains("release") }
        if (releaseTasks.isNotEmpty()) {
            val releaseApiUrl = project.findProperty("API_BASE_URL_RELEASE") as String?
            if (releaseApiUrl == null || releaseApiUrl.isBlank()) {
                throw GradleException(
                    "API_BASE_URL_RELEASE is required for release build. " +
                    "Example: ./gradlew assembleRelease " +
                    "-PAPI_BASE_URL_RELEASE=https://your-app.up.railway.app/api/"
                )
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.firebase:firebase-auth")
    implementation(platform("com.google.firebase:firebase-bom:34.14.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}