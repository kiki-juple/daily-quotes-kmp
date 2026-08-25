plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
}

kotlin {
    android {
        namespace = "com.disheveled.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {
        }
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    // The iOS test executables link sqliter's cinterop, which needs the system SQLite — the same
    // reason :composeApp passes -lsqlite3 to its framework.
    iosArm64 { binaries.all { linkerOpts("-lsqlite3") } }
    iosSimulatorArm64 { binaries.all { linkerOpts("-lsqlite3") } }

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.network)
            api(projects.shared.local)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.multiplatform.settings.test)
        }

        // Repository tests run against a real (in-memory) SQLDelight database, so each test target
        // supplies the driver its platform can actually open.
        getByName("androidHostTest").dependencies {
            implementation(libs.sqldelight.driver.sqlite)
        }

        iosTest.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
    }
}
