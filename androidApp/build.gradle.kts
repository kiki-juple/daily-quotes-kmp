import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val favqsApiKeyForRelease = run {
    val localProps = rootProject.file("local.properties").takeIf { it.exists() }?.let { file ->
        Properties().apply { file.inputStream().use { load(it) } }
    }
    localProps?.getProperty("favqs.api.key")
        ?: System.getenv("FAVQS_API_KEY")
        ?: ""
}

val requestedReleaseBuild = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("Release", ignoreCase = true) &&
            (taskName.contains("androidApp") || ":" !in taskName)
}
if (requestedReleaseBuild) {
    check(favqsApiKeyForRelease.isNotBlank()) {
        "Missing FavQs API key. Add favqs.api.key to local.properties or set FAVQS_API_KEY before building a release."
    }
}

android {
    namespace = "com.disheveled.dailyquotes"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.disheveled.dailyquotes"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // :composeApp ships its Compose resources as Android assets, but the KMP library plugin does
    // not export them to consumers. Pull them in from the directory produced by
    // :composeApp:packageComposeResourcesForApp (wired below) so they land in the APK's assets.
    sourceSets["main"].assets.srcDir(rootDir.resolve("composeApp/build/composeResourcesForApp"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

// Ensure the Compose resource assets are packaged by :composeApp before this app merges assets.
tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Assets") }.configureEach {
    dependsOn(":composeApp:packageComposeResourcesForApp")
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.errorprone.annotations)
    implementation(libs.koin.android)

    debugImplementation(libs.compose.uiTooling)
}
