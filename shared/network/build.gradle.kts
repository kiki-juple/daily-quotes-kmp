import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinSerialization)
}

// The single place the FavQs key is read. :androidApp used to repeat this block just to validate it
// for release builds; that check lives here now, next to the value it guards.
//
// Wrapped in providers so Gradle tracks local.properties/the env var as inputs instead of reading
// them eagerly at configuration time, which is what breaks the configuration cache.
val favqsApiKey: Provider<String> = providers.fileContents(
    rootProject.layout.projectDirectory.file("local.properties"),
).asText.map { text ->
    Properties().apply { text.reader().use { load(it) } }.getProperty("favqs.api.key").orEmpty()
}.orElse(providers.environmentVariable("FAVQS_API_KEY")).orElse("")

val requestedReleaseBuild = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("Release", ignoreCase = true) &&
            (taskName.contains("androidApp") || ":" !in taskName)
}

val generateApiKey by tasks.registering {
    val outputDirProvider = layout.buildDirectory.dir("generated/source/apikey/commonMain")
    val apiKey = favqsApiKey
    val failOnMissingKey = requestedReleaseBuild
    outputs.dir(outputDirProvider)
    inputs.property("apiKey", apiKey)
    doLast {
        val key = apiKey.get()
        check(!failOnMissingKey || key.isNotBlank()) {
            "Missing FavQs API key. Add favqs.api.key to local.properties or set FAVQS_API_KEY " +
                    "before building a release."
        }
        val pkgDir = outputDirProvider.get().asFile.resolve("com/disheveled/dailyquotes/data/api")
        pkgDir.mkdirs()
        pkgDir.resolve("GeneratedApiKey.kt").writeText(
            """
            // Generated; do not edit. Source: local.properties (favqs.api.key) or env FAVQS_API_KEY.
            package com.disheveled.dailyquotes.data.api

            internal const val FAVQS_API_KEY: String = "$key"
            """.trimIndent() + "\n"
        )
    }
}

kotlin {
    android {
        namespace = "com.disheveled.network"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {
        }
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            kotlin.srcDir(generateApiKey)
        }

        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.json)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)

            api(libs.multiplatform.settings)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.multiplatform.settings.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.security.crypto)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
