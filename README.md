# Airthings Logging Library for Kotlin Multiplatform

A home-grown logging library compatible with Kotlin Multiplatform.

## Yet Another Logging Library (YALL)?

Here at Airthings, we develop bleeding-edge libraries and applications to power our ever-increasing catalogue
of software, so when we looked for a logging library that ticked a few boxes for us, we didn't find one.

So, we wrote one – and this is it.

In short, the following features are the core of this logging library:

- Compatible with Kotlin Multiplatform, at the moment supporting Android and iOS.
- Library comes with a set of default loggers (implementing the interface `LoggerFacility`) that you can use
  out-of-the-box in your project.
- Unlike other logging libraries, logging properties/arguments associated with log messages are separate from
  the message itself – Look below for an example.
- Easily extensible by providing own logging implementation in your app, and then attaching them to the
  logging manager – Look below for an example.

## Installation

We're using JitPack.io to automatically distribute the library to users:

### In the root's `build.gradle.kts`:

```kotlin
allprojects {
    repositories {
        maven("https://jitpack.io")
        // …
    }
}
```

### In your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.airthings:KmpLib:<version>")
}
```

For the last version of the library: https://github.com/Airthings/KmpLog/tags

## Examples

### Setting up the app with a few logging facilities:

The following normally needs to be done once when the app starts:

#### Android:

Somewhere in your `App.kt` file, which extends `Application`, insert the following:

```kotlin
class MyApp : Application() {
    // …

    override fun onCreate() {
        super.onCreate()

        // …

        initializeLogging()
    }

    private fun initializeLogging() {
        // Log to the console when in DEBUG only.
        if (BuildConfig.DEBUG) {
            LoggerFacility.register(
                "${LoggerName.PRINTER}",
                PrinterLoggerFacility()
            )
        }

        // Log messages to the file system, can be uploaded later to a remote server for processing
        // after getting the user's permission.
        LoggerFacility.register(
            "${LoggerName.FILE}",
            FileLoggerFacility(
                LogLevel.INFO,
                logsFolder()
            )
        )

        // Log messages to Firebase.
        LoggerFacility.register(
            "${LoggerName.FIREBASE}",
            FirebaseLoggerFacility(AndroidFirebaseLoggerFacility())
        )
    }
}
```

#### iOS:

Somewhere in your `AppDelegate.swift` file, which extends `UIApplicationDelegate`, insert the following:

```swift
@UIApplicationMain
class MyAppDelegate: UIApplicationDelegate {
    // …

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // …

        initializeLogging()

        return true
    }

    private func initializeLogging() {
        // Log to the console when in DEBUG only.
        #if DEBUG
        LoggerFacilityCompanion.shared.register(
            name: "\(LoggerName.printer)",
            facility: PrinterLoggerFacility()
        )
        #endif

        // Log messages to the file system, can be uploaded later to a remote server for processing
        // after getting the user's permission.
        LoggerFacilityCompanion.shared.register(
            name: "\(LoggerName.file)",
            facility: FileLoggerFacility(
                minimumLogLevel: LogLevel.info,
                baseFolder: FileManager.logsFolder()
            )
        )

        // Log messages to Firebase.
        LoggerFacilityCompanion.shared.register(
            name: "\(LoggerName.firebase)",
            facility: FirebaseLoggerFacility(
                facility: IosFirebaseLoggerFacility()
            )
        )
    }
}
```

### Logging in the native or shared code:

Now, all you need to do is create instances of the `Logger` class and use it everywhere you want to log:

#### Android:

```kotlin
val logger = Logger("MyActivity")

// …

logger.info(
    LogMessage(
        message = "User signed in.",
        args = listOf(
            LogArg("user-id", userId),
            LogArg("captcha-time", captchaTime)
        )
    )
)
```

#### iOS:

Due to the nature of KMP, the code in Swift is a bit more verbose:

```swift
logger.info(
    message: LogMessage(
        message: "User signed in.",
        args: [
            LogArg(label: "user-id", value: userId),
            LogArg(label: "captcha-time", value: captchaTime)
        ]
    )
)
```
