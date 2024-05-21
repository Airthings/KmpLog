// swift-tools-version:5.9
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://maven.pkg.github.com/Airthings/KmpLog/com/airthings/lib/kmplog-kmmbridge/0.2.10/kmplog-kmmbridge-0.2.10.zip"
let remoteKotlinChecksum = "146e6b0f1941c3969e9cac21e5a3623a768024c9b0591b9ef618df214b5ff353"
let packageName = "KmpLog"
// END KMMBRIDGE BLOCK

let package = Package(
    name: packageName,
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: packageName,
            targets: [packageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: packageName,
            url: remoteKotlinUrl,
            checksum: remoteKotlinChecksum
        )
        ,
    ]
)