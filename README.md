# MathFlow Android — 1.6.0

MathFlow is a Kotlin / Jetpack Compose / Material 3 Android mathematics app.

## Features
- Scientific calculator: exact fractions, arithmetic, parentheses, integer powers, percentages, roots, trigonometric and logarithmic functions, constants π and e.
- Equation solver for linear and quadratic equations, rational equations, and numerical polynomial roots up to degree four, with step-by-step explanations.
- Linear systems with two or three variables using exact rational Gaussian elimination; detects unique, inconsistent, and underdetermined systems.
- Polynomial expression simplifier (one variable, degree up to four): expands parentheses, combines like terms, and preserves exact rational coefficients.
- Fraction calculator: exact addition, subtraction, multiplication and division; reduced fraction, mixed and decimal representations.
- Percentage tools: percentage of a value, part-to-whole percentage, increase and decrease.
- Geometry: rectangle, circle and triangle (Heron's formula).
- Offline unit converter: length, mass, area, volume, temperature, speed, time, pressure, energy and data units.
- Local history of the latest 100 successful calculator evaluations, including result, timestamp and steps.
- TTF/OTF font import, persistent light/dark theme and text-size adjustment.

## Numerical behavior
- Rational arithmetic is exact using `BigInteger`.
- Percent calculations use `BigDecimal` with 16 significant digits.
- Trigonometry and transcendental functions are approximations; trigonometry defaults to degrees.
- Circle and triangle geometry use floating-point approximations.
- The equation solver supports one variable; exact polynomial processing is supported through degree four, with numerical root finding for higher-degree or general-function cases. Linear systems support two or three variables. The expression simplifier supports one-variable polynomials through degree four and division by non-zero constants.

## Build requirements
- Android Studio with JDK 17+
- Android Gradle Plugin 8.7.3 and compatible Gradle 8.9+
- Android SDK Platform 35

Open the project root in Android Studio, sync Gradle, run the `test` task, then run the app on a device.

Application ID: `com.mathflow.app`
Minimum Android: API 26
Version: `1.6.0` (versionCode is assigned from the GitHub Actions run number for CI builds)

## Important release status
GitHub Actions compiles the Android app, runs the JVM unit tests, and publishes a debug APK artifact. A permanent release-signing keystore must be configured as repository Actions secrets before builds can be installed as in-place updates across workflow runs. Device-level UI and installation checks remain required on a physical Android phone.

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
