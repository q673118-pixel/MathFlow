import XCTest
import MathFlowCore
@testable import MathFlow

final class MathFlowSmokeTests: XCTestCase {
    func testAllAndroidLocalesAreAvailableInNativeApp() {
        XCTAssertEqual(MathFlowLanguage.all.count, 15)
        XCTAssertGreaterThanOrEqual(MathFlowLocalization.keys.count, 141)
        for language in MathFlowLanguage.all {
            for key in MathFlowLocalization.keys {
                let value = MathFlowLocalization.text(key, language: language.id)
                XCTAssertFalse(value.isEmpty, "Empty translation for \\(key) in \\(language.id)")
                XCTAssertNotEqual(value, key, "Missing translation for \\(key) in \\(language.id)")
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

}
