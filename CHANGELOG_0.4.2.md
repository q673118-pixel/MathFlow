# MathFlow 0.4.2 — calculator and expression-engine upgrade

## Changes
- Added implicit multiplication: `2(3+4)`, `2pi`, and `3sin(30)`.
- Perfect rational square roots remain exact (`sqrt(81/16) = 9/4`).
- Absolute value preserves exact rational arithmetic (`abs(-7/3) = 7/3`).
- Fixed power-step explanations to show the original base, exponent, and result; negative integer powers now work for approximate constants such as pi.
- Expanded the calculator keypad with scientific function shortcuts and Euler's constant.
- Made the calculator screen vertically scrollable and exposed the complete calculation trace.
- Corrected Android app version metadata to 0.4.2 / versionCode 2.
- Added regression tests for the new expression features.

## Verification
Source and tests were updated. An Android/Gradle build and JUnit execution have not been performed in this environment.
