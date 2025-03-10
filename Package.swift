// swift-tools-version:5.9
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://maven.pkg.github.com/Airthings/KmpLog/com/airthings/lib/kmplog-kmmbridge/0.2.13/kmplog-kmmbridge-0.2.13.zip"
let remoteKotlinChecksum = "f21c94b55d8149c63fc124240aaf1cd6daa014bb5b7d7b7ea5d25a7b345ea64d"
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