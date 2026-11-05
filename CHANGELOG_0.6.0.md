# MathFlow 0.6.0 — fractions, percentages and geometry

## Completed
- Replaced the Tools placeholder with working interactive calculators.
- Fraction calculator: exact rational parsing and +, −, ×, ÷; reduced fraction, mixed-number and decimal output.
- Percent calculator UI exposes three modes: percentage of a number, what percent a part is of a whole, and increase/decrease by a percentage.
- Geometry calculator: rectangle area/perimeter, circle area/circumference, triangle perimeter/semiperimeter/Heron area.
- Input validation for malformed fractions, zero denominators, invalid dimensions and impossible triangle sides.
- Added 9 focused unit tests for tool calculations and validation.
- Corrected duplicate package declaration in MainActivity.kt found during integration.
- Updated app version to 0.6.0 / versionCode 4.

## Numerical behavior
- Fraction operations remain exact using Rational/BigInteger.
- Percentage operations use BigDecimal with 16 significant digits.
- Circle and triangle geometry use Double-based π/square-root calculations and are approximate.

## Verification
The source and unit tests are included. Android Gradle build and JUnit execution have not been run in this environment.
