# Building MathFlow for iOS

The iOS app is native Swift/SwiftUI. It is built separately from the Android application.

## Requirements

- macOS with Xcode 16 or newer
- Xcode command-line tools
- XcodeGen
- Swift toolchain compatible with the package manifest

## Generate the project

From the repository root:

```sh
brew install xcodegen
swift scripts/generate_ios_icons.swift
xcodegen generate --spec ios/project.yml --project ios/MathFlow.xcodeproj
```

## Run the core tests

```sh
swift test --package-path MathFlowCore
```

## Build and test on the iOS Simulator

```sh
xcodebuild test \
  -project ios/MathFlow.xcodeproj \
  -scheme MathFlow \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  -derivedDataPath build/ios \
  -parallel-testing-enabled NO \
  CODE_SIGNING_ALLOWED=NO
```

The GitHub Actions workflow performs icon generation, core tests, simulator build, native iOS smoke tests and alternate-icon metadata verification on a macOS runner.

## Physical iPhone installation

The CI artifact is an **unsigned Simulator build**, not a device-installable IPA. Installing on a physical iPhone or distributing through TestFlight requires an Apple Developer team, a valid signing certificate and a matching provisioning profile. These credentials must be configured securely in the repository's GitHub Actions secrets or in Xcode; do not commit certificates, private keys or provisioning profiles to source control.
