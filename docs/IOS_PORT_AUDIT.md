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
- Graph tool: plots a function of x over a selected interval, handles invalid/discontinuous samples and lets the user inspect coordinates.

## Design system
- Ten named palettes: Vanilla, Black, White, Purple, Ocean, Emerald, Sunset, Rose, Midnight, Graphite.
- Ten app icon variants with matching names. iOS needs native alternate-icon assets and platform-supported icon switching; Android activity-alias XML cannot be reused as-is.
- Custom typography and user-imported TTF/OTF font support. iOS font registration and file import must be implemented natively; Android font paths cannot be reused.
- Persistent text scaling.
- Branded splash motion, animated page transitions, result transitions, configurable calculator-key feedback and configurable bottom-navigation motion.
- Five-item bottom navigation with eight selectable animation styles.
- Key animation settings include style, duration, opacity, intensity, optional color effect and selected press color.
- Bottom-navigation animation settings include enable/disable, style, duration, intensity, icon scale and indicator opacity.
- Settings use staged/pending selections and explicit Apply behavior. Preserve this model rather than applying every setting immediately.
- First-launch welcome dialog and splash completion state must persist correctly.

## Localization
- Fifteen languages: Russian, English, Kazakh, Spanish, French, German, Portuguese, Italian, Turkish, Arabic, Hindi, Chinese, Japanese, Korean and Uzbek.
- Arabic switches layout direction to RTL.
- All visible strings, validation messages, empty states, button labels, settings descriptions and graph labels must be localized, not only tab titles.

## Persistence and platform-specific behavior
- Android persists preferences (theme, icon, text scale, font path, language, animation settings, welcome-seen flag) in SharedPreferences.
- Calculator history stores up to 100 successful entries with expression, result, steps and timestamp.
- iOS must provide equivalent durable storage and migration-safe versioning.
- The Android launcher icon is dynamically selected via activity aliases. iOS uses its alternate app icon API and requires matching asset catalog entries.
- Font import uses Android's document picker and app-private storage; iOS requires the document picker, security-scoped access handling, persistent copy and CoreText registration.
- Preserve copy/paste actions, numeric keyboards, comma/dot parsing, error messages, keyboard dismissal, loading/cancellation behavior and accessibility labels.
- Adapt system bars, safe areas, status/navigation areas, keyboard insets and back navigation to iOS without changing the MathFlow visual language.

## Current iOS implementation audit (branch redesign-ui-ios, commit a1f1542b8d301733c4affef7565422b7473514dd)
Implemented in the current branch:
- SwiftUI app entry point and five-tab shell.
- Initial calculator screen and basic key handling.
- Ten palette definitions (must still be compared byte-for-byte/visually with Android palette source).
- A basic history store capped at 100 records.
- A basic press-scale button style.
- Swift package with an initial Rational type and expression parser.
- Initial unit tests for Rational and ExpressionEngine.
- macOS GitHub Actions workflow successfully runs Swift package tests and builds an iOS Simulator .app.

Not yet ported / currently incomplete:
- Tools screen is a placeholder: fraction, percentage, geometry, unit conversion and graph workflows are absent.
- Equation solver screen is a placeholder and does not call an implementation of AlgebraSolver.
- Settings currently expose themes only; icon selection, language selection, font import/library, text scaling, key-animation controls, bottom-navigation animation controls, staged selections and shared Apply behavior are absent.
- Localization is absent from the SwiftUI screens; the interface is hard-coded in Russian.
- The first-launch welcome dialog and branded animated intro splash are absent.
- The existing Swift history implementation must be checked for timestamp parity, ordering, persistence compatibility, clear behavior and result-step formatting.
- Calculator behavior is incomplete until all Android parser edge cases, key editing behavior, continuation, auto-parenthesis closure, cancellation, exact/approximate formatting and all tests are ported.
- The current Swift package does not yet include the Android algebra solver, fraction/percentage/geometry modules or unit conversion engine.
- No signed device archive, provisioning profile, distribution certificate or TestFlight upload is configured.
- The current successful CI artifact is an unsigned iOS Simulator app only. It cannot be installed directly on a physical iPhone.

## Existing Android tests
- Expression engine: exact decimal/fraction math, precedence, parentheses, powers, percent, divide-by-zero, degree trig, implicit multiplication, exact roots and abs, approximate negative powers.
- Algebra solver: linear/quadratic roots, repeated roots, no-real-root, identity/contradiction, rejection of unsupported degree, Unicode operators.
- Math tools: exact fraction operations, division by zero, percent calculations, rectangle/circle/triangle validations and Heron formula.
- Unit converter: length, mass, temperature, absolute-zero validation, decimal/binary data, comma decimals, speed and invalid units/values.
- Animation settings: defaults, clamping and fallback behavior.
- Additional UI behavior must be documented from MainActivity.kt and AppLanguage.kt and covered with Swift unit/UI tests.

## iOS port constraints and acceptance criteria
- Native Swift + SwiftUI implementation; no Kotlin Multiplatform and no Compose dependency.
- Keep Android code and Android build workflow untouched.
- Recreate every screen, state transition, palette, setting, input behavior, result formatting and animation. Adapt only for iPhone safe areas, touch conventions and available screen dimensions.
- Keep all calculations offline-capable. API integration must not become a runtime dependency for basic calculations.
- Establish parity tests using the Android unit tests as the behavioral specification. Add Swift tests for every supported operation and edge case.
- Preserve exact rational arithmetic with arbitrary-precision integers; do not replace it with Int64 or Double.
- Audit source behavior against tests before claiming feature parity. README descriptions are not authoritative where they conflict with current source/tests.
- Build with an Apple macOS runner. A simulator build is a compile gate, not proof of real-device visual fidelity.
- A signed physical-device build requires an Apple Developer team, valid signing identity and provisioning. Never commit signing certificates, private keys, provisioning profiles or credentials to the repository.
- Test on an actual iPhone before declaring the iOS port complete.
