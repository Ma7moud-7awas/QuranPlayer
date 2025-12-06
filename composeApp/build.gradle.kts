import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets.configureEach {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                with(compose) {
                    implementation(runtime)
                    implementation(foundation)
                    implementation(material3)
                    implementation(ui)
                    implementation(components.resources)
                    implementation(components.uiToolingPreview)
                    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
                    implementation("org.jetbrains.compose.material:material3:1.9.0-alpha04")
                    implementation("org.jetbrains.androidx.graphics:graphics-shapes:1.0.0-alpha09")
                }
                with(libs) {
                    implementation(androidx.lifecycle.viewmodelCompose)
                    implementation(androidx.lifecycle.runtimeCompose)
                    implementation(ktor.client.core)
                    implementation(ktor.client.content.negotiation)
                    implementation(ktor.serialization.kotlinx.json)
                    implementation(koin.core)
                }
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                with(libs) {
                    implementation(androidx.activity.compose)
                    implementation(androidx.media3.exoplayer)
                    implementation(androidx.media3.session)
                    implementation(androidx.media3.ui)
                    implementation(ktor.client.okhttp)
                    implementation(koin.android)
                    implementation(koin.androidx.compose)
                }
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "com.m7.mediaplayer"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.m7.mediaplayer"
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
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
