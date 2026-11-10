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

## Current iOS implementation audit (2026-11-10)

Audited against the Android source above. The native port is substantially implemented, but is not yet at full parity.

### Implemented
- Native Swift and SwiftUI app entry point with five primary destinations; Android source files remain unchanged on the separate `redesign-ui` branch.
- Calculator parser with exact rational arithmetic, implicit multiplication, scientific functions, angle mode, backspace, clear, continuation, optional worked steps and history integration.
- Math core: arbitrary-precision Rational, fraction tools, percentage tools, rectangle/circle/triangle calculations and the ten-category offline unit converter.
- Equation solver: exact linear/quadratic and rational-polynomial solving through degree four, denominator-domain exclusions, plus a numerical fallback. The fallback has been extended to mirror Android's `NumericParser`: absolute-value bars, fractional powers, radians, asin/acos/atan, exp, min and max.
- Graph screen with x-variable evaluation and coordinate inspection.
- History persisted locally and capped at 100 records.
- Ten palettes and 15 language dictionaries; Arabic switches layout direction to RTL.
- Native TTF/OTF import with security-scoped file access, CoreText registration, persistent copies and a saved-font list.
- Ten alternate iOS app icon sets. A macOS build script renders the original Android vector paths and launcher backgrounds into iOS icon assets; the app uses `UIApplication.setAlternateIconName`.
- Eight calculator-key animation styles and eight bottom-navigation styles, with staged settings and an explicit Apply action.
- Native vector MathFlow mark and a two-impact splash animation based on the Android launcher artwork.
- GitHub Actions workflow generates icon assets, runs Swift package tests, builds the unsigned iOS Simulator app, executes the native iOS simulator tests and verifies alternate icon registration.

### Confirmed corrections from source comparison
- Tool selection now stores stable IDs rather than translated labels, preventing the wrong tool screen from opening in non-Russian languages.
- Calculator rows now match the Android key arrangement, including the AC key and scientific rows.
- Bottom navigation is icon-only, matching the Compose implementation rather than showing extra labels.
- Superscript digits are no longer tokenized as ordinary numbers by the Swift equation parser.
- Polynomial root finding now uses derivative critical points, matching Android's approach for repeated roots.
- Percentage and geometry output is rounded to Android's 16-significant-digit HALF_UP context.
- General equation solving now uses the Android finite interval, radian trig behavior and matching numeric root-search strategy.

### Remaining differences / not yet verified
- The latest code changes still require a successful CI run. The last successful simulator build was an earlier commit; it does not verify the subsequent navigation, calculator typography and expanded equation-solver changes.
- There is no Android/iPhone screenshot baseline in the repository, so pixel-level visual comparison has not been performed. Source-level comparison is complete, but actual-device appearance is not certified.
- The iOS Settings screen is a single native List rather than Android's separate animated settings subpages. Theme and icon descriptions/previews are present, but the full-page structure and transition choreography are not equivalent.
- Text scaling now uses the full 80–140% range in exact 5% increments. The custom multiplier is combined with SwiftUI `ScaledMetric`, so the iOS accessibility text-size setting is still respected.
- Imported fonts are copied into the app's Documents/Fonts folder, registered with CoreText, listed for later selection and applied through a shared text-font modifier. The exact appearance of synthetic bold/semibold weights can still vary when the imported file contains only one font face.
- The localization catalog covers all 15 Android languages, but some result descriptions, welcome text and secondary UI labels remain hard-coded. Full visible-string localization is not complete.
- Key and navigation animation styles are available, and both settings sections now have live previews driven by the staged slider/style values. SwiftUI timing/rendering remains an approximation of Compose; exact per-frame parity has not been validated.
- Approximate calculator operations use Swift Double rather than Android's 24-digit BigDecimal context. Exact rational operations remain arbitrary precision; unit conversion uses Decimal at 34-digit precision; percentage/geometry tools use the Android 16-significant-digit rounding policy.
- The current CI artifact is an unsigned Simulator app. It is not an installable signed iPhone IPA. Device signing/TestFlight still require an Apple Developer team, certificate and provisioning profile, and a real iPhone test.
- The iOS test target checks all 15 locales and all ten palettes; CI executes these tests successfully. There are still no screenshot-diff or end-to-end interaction tests for calculator, settings and tool navigation.

### Build verification snapshot
- Last fully successful simulator run: [run 42796139721](https://github.com/q673118-pixel/MathFlow/actions/runs/42796139721), commit `8a2825d33a5462501bdd67cbefa168a6ad699d0a`.
- That run passed 47 MathFlowCore tests and 2 native iOS smoke tests, built the Simulator app and verified all 9 alternate icons in `CFBundleAlternateIcons`.
- Later UI preview, icon-generator color parsing and welcome-dialog changes are newer than that successful run. Do not treat run 42796139721 as verification of the latest HEAD.

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
