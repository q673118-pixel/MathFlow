# MathFlow iOS port audit

## Source of truth
- Audited branch: redesign-ui
- Audited commit: a5a3c4d191a830f64560d04edc61a0390ffdffd7
- Android release represented by this branch: 1.5.4
- The repository default branch main is older (1.0.0) and is not the iOS port baseline.
- iOS work is isolated in redesign-ui-ios, created from the audited redesign-ui commit.

## Android application structure
Five primary destinations:
1. Home — scientific calculator.
2. Tools — fractions, percentages, geometry, unit converter, graph.
3. Equations — equation solver with worked steps.
4. History — persisted latest 100 successful calculator evaluations.
5. Settings — theme, app icon, language, animation, font controls and text scale.

The app also has a first-launch welcome dialog and a branded animated intro splash.

## Calculator behavior
- Recursive-descent parser with operator precedence, parentheses, unary signs and implicit multiplication.
- Exact rational arithmetic for ordinary arithmetic, decimal literals and integer powers.
- Approximate transcendental operations: sqrt, sin, cos, tan, ln, log, abs; constants pi and e.
- Degree mode by default; scientific mode exposes DEG/RAD selection.
- Percent postfix, backspace, clear, continuation from previous result, auto-close unmatched opening parentheses.
- Optional worked steps and local history.
- Evaluation is dispatched off the UI thread and can be cancelled.

## Mathematical modules
- Rational.kt: reduced exact rational values backed by arbitrary-precision integers.
- ExpressionEngine.kt: 24-digit HALF_EVEN decimal context for approximate operations.
- AlgebraSolver.kt: current source supports polynomial/rational equations up to degree four, preserves denominator exclusions, and includes numerical methods for broader forms. This is more capable than the stale README claim of degree two; port the implementation and its tests, not the README sentence.
- MathTools.kt: exact fraction arithmetic; percentage calculations at 16 significant digits; rectangle, circle and Heron-triangle calculations.
- UnitConverter.kt: offline conversion for length, mass, area, volume, temperature, speed, time, pressure, energy and data; decimal and binary data units are distinct; temperature validates absolute zero.

## Design system
- Ten named palettes: Vanilla, Black, White, Purple, Ocean, Emerald, Sunset, Rose, Midnight, Graphite.
- Ten app icon variants with matching names.
- Custom typography and user-imported TTF/OTF font support.
- Persistent text scaling.
- Branded splash motion, animated page transitions, result transitions, configurable calculator-key feedback and configurable bottom-navigation motion.
- Five-item bottom navigation with eight selectable animation styles.
- Settings use staged/pending selections and explicit Apply behavior. Preserve this model rather than applying every setting immediately.

## Localization
- Fifteen languages: Russian, English, Kazakh, Spanish, French, German, Portuguese, Italian, Turkish, Arabic, Hindi, Chinese, Japanese, Korean and Uzbek.
- Arabic switches layout direction to RTL.

## Existing tests
- Expression engine: exact decimal/fraction math, precedence, parentheses, powers, percent, divide-by-zero, degree trig, implicit multiplication, exact roots and abs, approximate negative powers.
- Algebra solver: linear/quadratic roots, repeated roots, no-real-root, identity/contradiction, rejection of unsupported degree, Unicode operators.
- Math tools: exact fraction operations, division by zero, percent calculations, rectangle/circle/triangle validations and Heron formula.
- Unit converter: length, mass, temperature, absolute-zero validation, decimal/binary data, comma decimals, speed and invalid units/values.
- Animation settings: defaults, clamping and fallback behavior.

## iOS port constraints and acceptance criteria
- Native Swift + SwiftUI implementation; no Kotlin Multiplatform and no Compose dependency.
- Keep Android code and Android build workflow untouched.
- Recreate every screen, state transition, palette, setting, input behavior, result formatting and animation. Adapt only for iPhone safe areas, touch conventions and available screen dimensions.
- Keep all calculations offline-capable. API integration must not become a runtime dependency for basic calculations.
- Establish parity tests using the Android unit tests as the behavioral specification. Add Swift tests for every supported operation and edge case.
- Preserve exact rational arithmetic with arbitrary-precision integers; do not replace it with Int64 or Double.
- Audit source behavior against tests before claiming feature parity. README descriptions are not authoritative where they conflict with current source/tests.
- Build with an Apple macOS runner. A simulator build is a compile gate, not proof of real-device visual fidelity.
- Test on an actual iPhone before declaring the iOS port complete.
