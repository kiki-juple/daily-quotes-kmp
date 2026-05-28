import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinSerialization)
}

val favqsApiKey: String = run {
    val localProps = rootProject.file("local.properties").takeIf { it.exists() }?.let { f ->
        Properties().apply { f.inputStream().use { load(it) } }
    }
    localProps?.getProperty("favqs.api.key")
        ?: System.getenv("FAVQS_API_KEY")
        ?: ""
}

val generateApiKey by tasks.registering {
    val outputDirProvider = layout.buildDirectory.dir("generated/source/apikey/commonMain")
    val apiKey = favqsApiKey
    outputs.dir(outputDirProvider)
    inputs.property("apiKey", apiKey)
    doLast {
        val pkgDir = outputDirProvider.get().asFile.resolve("com/disheveled/dailyquotes/data/api")
        pkgDir.mkdirs()
        pkgDir.resolve("GeneratedApiKey.kt").writeText(
            """
            // Generated; do not edit. Source: local.properties (favqs.api.key) or env FAVQS_API_KEY.
            package com.disheveled.dailyquotes.data.api

            internal const val FAVQS_API_KEY: String = "$apiKey"
            """.trimIndent() + "\n"
        )
    }
}

kotlin {
    androidLibrary {
        namespace = "com.disheveled.network"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 29

        withHostTestBuilder {
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            kotlin.srcDir(generateApiKey)
        }

        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.json)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.koin.core)

            api(libs.multiplatform.settings)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
