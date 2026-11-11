import XCTest

final class MathFlowUITests: XCTestCase {
    func testSettingsNavigationThemeGalleryAndArabicLayout() {
        let app = XCUIApplication()
        app.launchArguments = [
            "-mathflow.welcomeSeen", "YES",
            "-mathflow.language", "ar"
        ]
        app.launch()

        let settingsTab = app.buttons["mathflow.tab.settings"]
        XCTAssertTrue(settingsTab.waitForExistence(timeout: 10), "Settings tab must be available")
        settingsTab.tap()

        let themesRow = app.buttons["settings.menu.themes"]
        XCTAssertTrue(themesRow.waitForExistence(timeout: 5), "Settings categories must load")
        attachScreenshot(of: app, named: "settings-root-arabic")

        themesRow.tap()
        let oceanTheme = app.buttons["settings.theme.ocean"]
        XCTAssertTrue(oceanTheme.waitForExistence(timeout: 5), "Theme gallery must display all theme cards")
        attachScreenshot(of: app, named: "theme-gallery-arabic")

        let back = app.buttons["settings.back"]
        XCTAssertTrue(back.waitForExistence(timeout: 5), "Settings subpage must expose a back action")
        back.tap()

        let languageRow = app.buttons["settings.menu.app_language"]
        XCTAssertTrue(languageRow.waitForExistence(timeout: 5), "Language settings must be available")
        languageRow.tap()
        XCTAssertTrue(app.staticTexts["العربية"].waitForExistence(timeout: 5), "Arabic language option must be visible")
        attachScreenshot(of: app, named: "language-list-arabic")
    }

    private func attachScreenshot(of app: XCUIApplication, named name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
