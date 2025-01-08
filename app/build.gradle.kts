/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

import org.lineageos.generatebp.GenerateBpPlugin
import org.lineageos.generatebp.GenerateBpPluginExtension
import org.lineageos.generatebp.models.Module

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

apply {
    plugin<GenerateBpPlugin>()
}

buildscript {
    repositories {
        maven("https://raw.githubusercontent.com/lineage-next/gradle-generatebp/v1.19/.m2")
    }

    dependencies {
        classpath("org.lineageos:gradle-generatebp:+")
    }
}

android {
    namespace = "org.lineageos.twelve"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.lineageos.twelve"
        minSdk = 31
        targetSdk = 35
        versionCode = 100
        versionName = "1.0.0.1"

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        release {
            // Enables code shrinking, obfuscation, and optimization.
            isMinifyEnabled = true

            // Enables resource shrinking.
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            // Append .dev to package name so we won't conflict with AOSP build.
            applicationIdSuffix = ".dev"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.midi)
    implementation(libs.androidx.media3.exoplayer.rtsp)
    implementation(libs.androidx.media3.exoplayer.smoothstreaming)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.viewpager2)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.kotlinx.coroutines.guava) {
        // Exclude compile time dependencies
        exclude("com.google.j2objc", "j2objc-annotations")
        exclude("org.checkerframework", "checker-qual")
    }
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material)
    implementation(libs.nier.visualizer) {
        exclude(group = "com.android.support")
    }
    implementation(libs.okhttp)
}

configure<GenerateBpPluginExtension> {
    targetSdk.set(android.defaultConfig.targetSdk!!)
    minSdk.set(android.defaultConfig.minSdk!!)
    availableInAOSP.set { module: Module ->
        when {
            module.group.startsWith("androidx") -> {
                // We provide our own androidx.media3
                !module.group.startsWith("androidx.media3")
            }
            module.group.startsWith("org.jetbrains") -> true
            module.group == "com.google.android.material" -> true
            module.group == "com.google.auto.value" -> true
            module.group == "com.google.code.findbugs" -> true
            module.group == "com.google.errorprone" -> true
            module.group == "com.google.guava" -> true
            module.group == "junit" -> true
            else -> false
        }
    }
}
