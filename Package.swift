// swift-tools-version:5.9
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://api.github.com/repos/Airthings/KmpLog/releases/assets/548498467.zip"
let remoteKotlinChecksum = "f35b27af7ed166bbc1159ed8f55b20f320a5fc4adce11a9b7650123c307f6ded"
let packageName = "KmpLog"
// END KMMBRIDGE BLOCK

let package = Package(
    name: packageName,
    platforms: [
        .iOS(.v14),
.macOS(.v11)
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