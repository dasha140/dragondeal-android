import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
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
                props.getProperty("API_BASE_URL", "http://dragondeal.online:8081/api/")
            } else {
                "http://dragondeal.online:8081/api/"
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
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")
    implementation(libs.koin.android)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}