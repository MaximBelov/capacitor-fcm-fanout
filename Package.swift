// swift-tools-version: 5.9
import PackageDescription

// The plugin routes nothing on iOS (see the README): APNs hands its token to the app delegate, which every push SDK
// reads, and there is no single service to share. The iOS side exists so that Capacitor registers the plugin on both
// platforms and `getStatus` answers there too.
let package = Package(
    name: "CapacitorFcmFanout",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "CapacitorFcmFanout",
            targets: ["FcmFanoutPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "FcmFanoutPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/FcmFanoutPlugin"),
        .testTarget(
            name: "FcmFanoutPluginTests",
            dependencies: ["FcmFanoutPlugin"],
            path: "ios/Tests/FcmFanoutPluginTests")
    ]
)
