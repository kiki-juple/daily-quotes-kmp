import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "com.disheveled.dailyquotes.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {}
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.disheveled.dailyquotes")
            linkerOpts("-lsqlite3")
            export(projects.shared.data)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            api(projects.shared.data)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.uiBackHandler)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.navigation3.runtime)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// The `com.android.kotlin.multiplatform.library` plugin (required for KMP modules on AGP 9)
// does not export Android assets to consuming application modules, so the Compose resources
// bundled in this module never reach :androidApp's APK and the app crashes on first drawable
// load with a MissingResourceException. Re-package the prepared Compose resources into a stable
// directory that :androidApp adds to its asset source set (see androidApp/build.gradle.kts).
//
// The "dailyquotes.composeapp.generated.resources" segment is the runtime lookup path used by
// the generated `Res` accessors; it must stay in sync with the generated Res package.
tasks.register<Sync>("packageComposeResourcesForApp") {
    val preparedRoot =
        layout.buildDirectory.dir("generated/compose/resourceGenerator/preparedResources")
    // Copy the children of each source set's `composeResources` dir (drawable/, font/, …)
    // directly under the qualifier so the runtime lookup path is correct. Non-existent source
    // dirs (e.g. an empty androidMain) are silently skipped by Sync.
    listOf("CommonMain", "AndroidMain").forEach { sourceSet ->
        dependsOn("prepareComposeResourcesTaskFor$sourceSet")
        from(preparedRoot.map { it.dir("${sourceSet.replaceFirstChar(Char::lowercase)}/composeResources") })
    }
    into(layout.buildDirectory.dir("composeResourcesForApp/composeResources/dailyquotes.composeapp.generated.resources"))
}
