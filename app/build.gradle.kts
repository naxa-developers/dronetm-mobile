import java.util.Properties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.google.services)
}

val localProperties =  Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
}

android {
    namespace = "np.com.naxa.drone_tasking_manager"
    compileSdk = 36

    defaultConfig {
        applicationId = "np.com.naxa.drone_tasking_manager"
        minSdk = 24
        // noinspection EditedTargetSdkVersion
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "SECRET_DATA_KEY", "\"${localProperties["SECRET_DATA_KEY"] ?: ""}\"")
        buildConfigField("String", "BASE_URL", "\"${localProperties["BASE_URL"] ?: ""}\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${localProperties["GOOGLE_CLIENT_ID"] ?: ""}\"")
        buildConfigField("String", "ENCRYPTION_ALGORITHM", "\"${localProperties["ENCRYPTION_ALGORITHM"] ?: ""}\"")
        buildConfigField("String", "ENCRYPTION_ALGORITHM_KEY", "\"${localProperties["ENCRYPTION_ALGORITHM_KEY"] ?: ""}\"")


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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

//    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.documentfile)
    implementation(libs.okhttp)


    /* *****************************************************
     **** Accompanist
     ****************************************************** */
    implementation(libs.accompanist.flowlayout)


    /* *****************************************************
     **** Lifecycle
     ****************************************************** */
    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.common.java8)
    implementation(libs.runtime.livedata)
    implementation(libs.activity.ktx)

    /* *****************************************************
     **** Coroutines
     ****************************************************** */
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    /* *****************************************************
    **** Dependency-Hilt Injection
    ****************************************************** */
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
//    implementation(libs.hilt.work)

    /* *****************************************************
    **** Retrofit2
    ****************************************************** */
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.converter.moshi)

    /* *****************************************************
    **** Storage
    ****************************************************** */
    implementation(libs.storage.mmkv)

    /* *****************************************************
    **** Maplibre Map
    ****************************************************** */
    implementation(libs.android.sdk)


    /* *****************************************************
    **** Google Sign in
    ****************************************************** */
    implementation(libs.google.credential)
    implementation(libs.google.auth)
    implementation(libs.google.id)
    implementation(libs.google.gms.auth)
    implementation(libs.firebase.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    /* *****************************************************
    **** Image
    ****************************************************** */
    implementation(libs.coil.compose)

    /* *****************************************************
    **** Image
    ****************************************************** */
    implementation(libs.play.services.location)

    /* *****************************************************
    **** Testing
    ****************************************************** */

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}