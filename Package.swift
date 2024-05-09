// swift-tools-version:5.9
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://maven.pkg.github.com/Airthings/KmpLog/com/airthings/lib/kmplog-kmmbridge/0.2.8/kmplog-kmmbridge-0.2.8.zip"
let remoteKotlinChecksum = "68ddc747c277d30dd3950c0dae41bd91ec9ad061b59e631a92740acea4bc786d"
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