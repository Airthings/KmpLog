// swift-tools-version:5.9
import PackageDescription

let packageName = "KmpLog"

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
            path: "./build/XCFrameworks/debug/\(packageName).xcframework"
        )
        ,
    ]
)