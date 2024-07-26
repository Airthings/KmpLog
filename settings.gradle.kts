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

rootProject.name = "kmplog"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        val externalLibs by creating {
            from("com.arithings.mobile:version-catalog:1.0.5")
        }
    }

    val props = java.util.Properties()
    val localPropsFile = file("$rootDir/local.properties")
    if (localPropsFile.exists()) {
        props.load(java.io.FileInputStream(localPropsFile))
    }

    val githubUsername = System.getenv("PUBLISH_GITHUB_USERNAME") ?: props["GITHUB_USERNAME"]?.toString()
    val githubPassword = System.getenv("PUBLISH_GITHUB_PASSWORD") ?: props["GITHUB_PASSWORD"]?.toString()

    val githubPackages = listOf(
        "https://maven.pkg.github.com/airthings/android-version-catalog",
    )

    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()

        githubPackages.forEach { uri ->
            maven(uri) {
                credentials {
                    username = githubUsername
                    password = githubPassword
                }
            }
        }
    }
}
