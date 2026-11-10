// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "MathFlowCore",
    products: [.library(name: "MathFlowCore", targets: ["MathFlowCore"])],
    dependencies: [.package(url: "https://github.com/attaswift/BigInt.git", from: "5.3.0")],
    targets: [
        .target(name: "MathFlowCore", dependencies: [.product(name: "BigInt", package: "BigInt")]),
        .testTarget(name: "MathFlowCoreTests", dependencies: ["MathFlowCore"])
    ]
)
