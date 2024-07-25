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

private val version = (properties["AUTO_VERSION"] ?: properties["LIBRARY_VERSION"])?.toString()
    ?: error("Fatal error: unable to determine the library version.")

private val iosFrameworkName = "KmpLog"

rootProject.group = "${properties["GROUP"]}"
rootProject.version = version

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(externalLibs.plugins.arturbosch.detekt)
    alias(externalLibs.plugins.jlleitschuh.gradle.ktlint)
    alias(externalLibs.plugins.ben.manes)
    alias(externalLibs.plugins.android.library)
    alias(externalLibs.plugins.kotlin.multiplatform)
    alias(externalLibs.plugins.kmmbridge)
    id("maven-publish")
}

dependencies {
    detektPlugins(externalLibs.detekt.formatting)
    detektPlugins(externalLibs.detekt.cli)
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
            baseName = iosFrameworkName
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

    // See for details: https://youtrack.jetbrains.com/issue/KT-61573
    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(externalLibs.touchlab.stately.concurrency)
                implementation(externalLibs.kotlinx.coroutines.core)
                implementation(externalLibs.kotlinx.datetime)
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

kmmbridge {
    frameworkName.set(iosFrameworkName)
    mavenPublishArtifacts()
    spm(
        spmDirectory = "./",
        swiftToolVersion = "5.9",
        targetPlatforms = {
            iOS { v("14") }
        },
    )
}

addGithubPackagesRepository()

/**
 * Apple requires 3rd party frameworks to include a privacy policy file.
 *
 * Based on a discussion on the Kotlin Slack, several people proposed the below solution to copy the privacy policy
 * file to the framework output directory.
 *
 * Source: https://kotlinlang.slack.com/archives/C3PQML5NU/p1711715546770359
 */
rootProject.afterEvaluate {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink>().forEach { linkTask ->
        val frameworkDestinationDir = linkTask.outputFile
        linkTask.doLast {
            rootProject.copy {
                val privacyFile = rootProject.file("PrivacyInfo.xcprivacy")

                from(privacyFile)
                into(frameworkDestinationDir.get())
            }
        }
    }
}
