# MathFlow 1.0.0 — final integration pass

## Final integration work
- Connected calculator, equation solver, tools, history and settings in the five-tab application shell.
- Added the missing Compose `togetherWith` import used by the page transition.
- Removed a duplicate JUnit import in the expression-engine tests.
- Equation solver now accepts the superscript-square notation shown in its own UI (`x²`) and Unicode mathematical minus/multiply/divide symbols.
- Added regression tests for superscript equation notation, percent increase, exact decimal fraction input and zero-length geometry dimensions.
- Set release metadata to versionName 1.0.0 / versionCode 5.
- Disabled cleartext network traffic and configured the activity to resize when the keyboard appears.

## Included functionality
- Exact rational arithmetic and scientific expression parser.
- One-variable linear and quadratic equation solver.
- Fraction, percentage and geometry tools.
- Persistent calculation history, custom font import, theme and text-size settings.

## Verification status
- ZIP archive integrity and project structure are checked.
- Android manifest XML is parsed successfully.
- Source-level integration checks are performed.
- **A full Android Gradle build and JUnit execution were not possible in this environment:** no Gradle installation, Gradle wrapper, Kotlin compiler or Android SDK is available. Therefore this is a 1.0.0 source release candidate, not a verified installable APK.
