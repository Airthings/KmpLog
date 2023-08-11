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

group = "com.airthings.lib"
version = "0.1.0"

buildscript {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${properties["version.plugin.androidGradle"]}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${properties["version.kotlin"]}")
    }
}

// Versions of plugins are now configured once in pluginManagement block in settings.gradle.kts.
plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.github.ben-manes.versions")
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-cli:${properties["version.plugin.detekt"]}")
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${properties["version.plugin.detekt"]}")
    }

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

        toolVersion = "${properties["version.plugin.detekt"]}"
        config.setFrom(configFile)
        parallel = true
    }

    ktlint {
        // Latest: https://rdr.to/EI9Yvj1YywO
        version.set("${properties["version.ktlint"]}")
        verbose.set(true)
        outputToConsole.set(true)
        filter {
            exclude("**/shared/build/**")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
