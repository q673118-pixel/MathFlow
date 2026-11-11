import XCTest
import MathFlowCore
@testable import MathFlow

final class MathFlowSmokeTests: XCTestCase {
    func testAllAndroidLocalesAreAvailableInNativeApp() {
        XCTAssertEqual(MathFlowLanguage.all.count, 15)
        XCTAssertGreaterThanOrEqual(MathFlowLocalization.keys.count, 141)
        for language in MathFlowLanguage.all {
            for key in MathFlowLocalization.keys {
                XCTAssertTrue(MathFlowLocalization.hasTranslation(key, language: language.id), "Missing translation for \(key) in \(language.id)")
            }
        }
    }

    func testAllTenAndroidPalettesResolve() {
        let ids = ["vanilla", "black", "white", "purple", "ocean", "emerald", "sunset", "rose", "midnight", "graphite"]
        XCTAssertEqual(ids.count, 10)
        for id in ids {
            _ = MathFlowPalette.named(id)
        }
        XCTAssertTrue(MathFlowPalette.named("black").isDark)
        XCTAssertFalse(MathFlowPalette.named("white").isDark)
        XCTAssertTrue(MathFlowPalette.named("unknown").isDark)
    }

    @MainActor
    func testAppearancePreferencesPersistAcrossSettingsReload() {
        let defaults = UserDefaults.standard
        let values: [(String, Any)] = [
            ("mathflow.theme", "ocean"),
            ("mathflow.language", "ar"),
            ("mathflow.textScale", 1.25),
            ("mathflow.keyStyle", "elastic"),
            ("mathflow.navStyle", "ripple_bloom"),
            ("mathflow.navEnabled", false),
            ("mathflow.icon", "graphite")
        ]
        let previous = values.map { (key: $0.0, value: defaults.object(forKey: $0.0)) }
        defer {
            for item in previous {
                if let value = item.value { defaults.set(value, forKey: item.key) }
                else { defaults.removeObject(forKey: item.key) }
            }
        }
        for (key, value) in values { defaults.set(value, forKey: key) }

        let reloaded = AppSettings()
        XCTAssertEqual(reloaded.themeID, "ocean")
        XCTAssertEqual(reloaded.languageID, "ar")
        XCTAssertEqual(reloaded.textScale, 1.25, accuracy: 0.001)
        XCTAssertEqual(reloaded.keyStyle, "elastic")
        XCTAssertEqual(reloaded.navStyle, "ripple_bloom")
        XCTAssertFalse(reloaded.navEnabled)
        XCTAssertEqual(reloaded.iconID, "graphite")
    }

    func testArabicLocaleIsRegisteredAndTranslated() {
        XCTAssertTrue(MathFlowLanguage.all.contains(where: { $0.id == "ar" }))
        XCTAssertNotEqual(MathFlowLocalization.text("settings", language: "ar"), "settings")
    }


}
