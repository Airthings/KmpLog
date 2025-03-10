// swift-tools-version:5.9
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://maven.pkg.github.com/Airthings/KmpLog/com/airthings/lib/kmplog-kmmbridge/0.2.14/kmplog-kmmbridge-0.2.14.zip"
let remoteKotlinChecksum = "1a3ab339da01b2ec95e5c42aa43d255d4bd9cde0d69f2329167866b4059bce40"
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