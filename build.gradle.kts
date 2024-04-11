/*
 * Copyright 2023 Airthings ASA. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

rootProject.group = "com.airthings.lib"
rootProject.version = "0.2.6"

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${properties["version.plugin.androidGradle"]}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${properties["version.kotlin"]}")
        classpath("com.github.ben-manes:gradle-versions-plugin:${properties["version.plugin.outdated"]}")
    }
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://plugins.gradle.org/m2/")
}

// Versions of plugins are now configured once in pluginManagement block in settings.gradle.kts.
plugins {
    kotlin("multiplatform")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.ben-manes.versions")
    id("com.android.library")
    id("maven-publish")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-cli:${properties["version.plugin.detekt"]}")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${properties["version.plugin.detekt"]}")
}

apply(plugin = "io.gitlab.arturbosch.detekt")
apply(plugin = "org.jlleitschuh.gradle.ktlint")

detekt {
    val rootDir = project.rootDir.canonicalFile
    var configDir = project.projectDir.canonicalFile
    var configFile: File
    do {
        configFile = file("$configDir/detekt.yml")
        if (configFile.exists()) {
            break
        }
        configDir = configDir.parentFile.canonicalFile
    } while (configDir.startsWith(rootDir))

    config.setFrom(files(configFile))
    parallel = true
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/build/**")
        exclude("**/gen/**")
    }
}

kotlin {
    val jvmVersion = "${properties["jvm.version"]}"

    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        // Fixes this: https://rdr.to/DkdMx1MXyeB
        it.binaries.all {
            linkerOpts += "-ld64"
        }
        it.binaries.framework {
            baseName = rootProject.name
        }
    }

    // Export KDoc comments to generated Objective-C header (https://rdr.to/8KKpSbCUBdY).
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].kotlinOptions.freeCompilerArgs += "-Xexport-kdoc"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = jvmVersion
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(
                    "co.touchlab:stately-concurrency:" +
                        "${properties["version.stately.concurrency"]}",
                )
                implementation(
                    "org.jetbrains.kotlinx:kotlinx-coroutines-core:" +
                        "${properties["version.kotlin.coroutines"]}",
                )
                implementation(
                    "org.jetbrains.kotlinx:kotlinx-datetime:" +
                        "${properties["version.kotlin.datetime"]}",
                )
            }
        }
        val jvmMain by getting
        val androidMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

android {
    namespace = "${rootProject.group}.logging"

    compileSdk = "${properties["build.android.compileSdk"]}".toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = "${properties["build.android.minimumSdk"]}".toInt()
        targetSdk = "${properties["build.android.targetSdk"]}".toInt()
    }

    compileOptions {
        val jvmVersion = JavaVersion.toVersion("${properties["jvm.version"]}".toInt())

        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "AirthingsGitHubPackages"
            url = uri("https://maven.pkg.github.com/airthings/kmplog")

            credentials {
                // The following are automatically generated by GitHub.
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
