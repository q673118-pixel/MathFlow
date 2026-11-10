import XCTest
import MathFlowCore
@testable import MathFlow

final class MathFlowSmokeTests: XCTestCase {
    func testAllAndroidLocalesAreAvailableInNativeApp() {
        XCTAssertEqual(MathFlowLanguage.all.count, 15)
        for language in MathFlowLanguage.all {
            XCTAssertFalse(MathFlowLocalization.text("settings", language: language.id).isEmpty)
            XCTAssertFalse(MathFlowLocalization.text("calculate", language: language.id).isEmpty)
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

    func testAppTargetUsesNativeSwiftUIModule() {
        XCTAssertTrue(String(reflecting: RootView.self).contains("RootView"))
    }
}
