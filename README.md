# MathFlow Android — 1.0.0

MathFlow is a Kotlin / Jetpack Compose / Material 3 Android mathematics app.

## Features
- Scientific calculator: exact fractions, arithmetic, parentheses, integer powers, percentages, roots, trigonometric and logarithmic functions, constants π and e.
- Linear and quadratic equation solver with a worked solution.
- Fraction calculator: exact addition, subtraction, multiplication and division; reduced fraction, mixed and decimal representations.
- Percentage tools: percentage of a value, part-to-whole percentage, increase and decrease.
- Geometry: rectangle, circle and triangle (Heron's formula).
- Local history of the latest 100 successful calculator evaluations, including result, timestamp and steps.
- TTF/OTF font import, persistent light/dark theme and text-size adjustment.

## Numerical behavior
- Rational arithmetic is exact using `BigInteger`.
- Percent calculations use `BigDecimal` with 16 significant digits.
- Trigonometry and transcendental functions are approximations; trigonometry defaults to degrees.
- Circle and triangle geometry use floating-point approximations.
- The equation solver supports one variable and degree at most two.

## Build requirements
- Android Studio with JDK 17+
- Android Gradle Plugin 8.7.3 and compatible Gradle 8.9+
- Android SDK Platform 35

Open the project root in Android Studio, sync Gradle, run the `test` task, then run the app on a device.

Application ID: `com.mathflow.app`
Minimum Android: API 26
Version: `1.0.0` (versionCode 5)

## Important release status
This repository is a **source release**, not an APK. In the environment where this archive was prepared, Gradle, the Gradle wrapper, the Kotlin compiler and Android SDK were unavailable. The Android app has therefore not been built or run here; compile and device verification remain required in Android Studio.

## Build an APK from an Android phone (GitHub Actions)

This archive includes a GitHub Actions workflow. It builds the debug APK on GitHub's
cloud runner; no Android Studio or local Gradle installation is needed.

1. In a mobile browser, sign in to GitHub and create a **private** repository named
   `MathFlow` (or another name).
2. Open the repository's **Add file → Upload files** page.
3. Extract this ZIP first. Upload the *contents* of the `MathFlow_Android` folder,
   preserving the `.github/workflows/android.yml` path. The workflow must end up at
   `.github/workflows/android.yml` in the repository root, not nested under
   `MathFlow_Android`.
4. Commit the uploaded files to the `main` branch.
5. Open the repository's **Actions** tab. Select **MathFlow Android build**.
   If it has not started automatically, choose **Run workflow → Run workflow**.
6. Open the completed run. Under **Artifacts**, download `MathFlow-debug-apk`.
7. Extract the downloaded artifact ZIP; it contains `app-debug.apk`. Open that APK
   on the Android phone and install it. Android may ask you to allow installs from
   your browser or file manager.

The workflow runs `testDebugUnitTest` before `assembleDebug`. If tests or compilation
fail, the run will be marked failed and its log will show the error.

Note: this produces a debug APK for testing, not a signed Play Store release.
