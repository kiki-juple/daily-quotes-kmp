import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// :composeApp has to be evaluated first for its task to be resolvable here; without this the
// lookup below runs before that project exists and fails at configuration time.
evaluationDependsOn(":composeApp")

val composeResourcesForApp = project(":composeApp").tasks.named<Sync>("packageComposeResourcesForApp")

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
    // not export them to consumers. Register the producing task itself as the asset source so
    // Gradle derives the dependency for every consumer. Pointing at the raw directory instead used
    // to leave lint's own tasks reading it with no declared dependency, which failed the build.
    sourceSets["main"].assets.srcDir(composeResourcesForApp)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.koin.android)

    debugImplementation(libs.compose.uiTooling)
}
