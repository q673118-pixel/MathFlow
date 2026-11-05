# MathFlow 0.5.0 — persistence and personalization

## Completed in this stage
- Calculation history is persisted locally and survives app restarts.
- History stores expression, result, timestamp and calculation steps.
- History is limited to the latest 100 entries and can be cleared.
- Added TTF/OTF font import through Android's document picker.
- Selected font is copied into app-private storage and validated before use.
- Added system-font reset.
- Added persistent text-size scaling.
- Dark/light theme preference is now persisted.
- Updated app version metadata to 0.5.0 / versionCode 3.

## Storage and privacy
- Uses app-private SharedPreferences and filesDir; no storage permission is requested.
- No network requests or account sync are used.
- Clearing history removes the stored history JSON.

## Verification
Source updated. Android Gradle build and JUnit tests have not been run in this environment.
