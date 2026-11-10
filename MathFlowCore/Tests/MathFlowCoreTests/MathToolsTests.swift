import XCTest
@testable import MathFlowCore

final class MathToolsTests: XCTestCase {
    func testExactFractionOperations() throws {
        let result = try FractionTools.calculate("3/4", "2/5", operation: .add)
        XCTAssertEqual(result.exact, "23/20")
        XCTAssertEqual(result.mixed, "1 3/20")
    }

    func testDecimalFractionInput() throws {
        let result = try FractionTools.calculate("0.5", "1/4", operation: .add)
        XCTAssertEqual(result.exact, "3/4")
    }

    func testFractionDivisionByZero() {
        XCTAssertThrowsError(try FractionTools.calculate("1/2", "0", operation: .divide))
    }

    func testPercentTools() throws {
        XCTAssertEqual(try PercentTools.percentOf("15", "240"), "36")
        XCTAssertEqual(try PercentTools.whatPercent("25", "200"), "12.5")
        XCTAssertEqual(try PercentTools.change("100", percent: "15", increase: false), "85")
        XCTAssertEqual(try PercentTools.change("200", percent: "12.5", increase: true), "225")
    }

    func testGeometry() throws {
        let rectangle = try GeometryTools.rectangle("5", "3")
        XCTAssertEqual(rectangle.area, "15")
        XCTAssertEqual(rectangle.perimeter, "16")
        let triangle = try GeometryTools.triangle("3", "4", "5")
        XCTAssertEqual(triangle.area, "6")
        XCTAssertThrowsError(try GeometryTools.triangle("1", "2", "8"))
    }

    func testUnitConversions() throws {
        XCTAssertEqual(try UnitConverter.convert("1.25", category: "length", from: "km", to: "m"), "1250")
        XCTAssertEqual(try UnitConverter.convert("1", category: "mass", from: "lb", to: "kg"), "0.45359237")
        XCTAssertEqual(try UnitConverter.convert("32", category: "temperature", from: "f", to: "c"), "0")
        XCTAssertEqual(try UnitConverter.convert("1", category: "data", from: "kib", to: "b"), "1024")
        XCTAssertThrowsError(try UnitConverter.convert("-1", category: "temperature", from: "k", to: "c"))
    }
    func testExactLinearAndQuadraticEquationSolving() throws {
        XCTAssertTrue(try AlgebraSolver.solve("2x + 3 = 11").answer.contains("4"))
        let quadratic = try AlgebraSolver.solve("x^2 - 5x + 6 = 0")
        XCTAssertEqual(quadratic.kind, "quadratic")
        XCTAssertTrue(quadratic.answer.contains("3"))
        XCTAssertTrue(quadratic.answer.contains("2"))
    }

    func testRationalEquationDomainExclusions() throws {
        let result = try AlgebraSolver.solve("(x^2 - 1)/(x - 1) = 0")
        XCTAssertTrue(result.answer.contains("-1"))
        XCTAssertFalse(result.answer.contains("x₂"))
        XCTAssertTrue(result.steps.contains(where: { $0.contains("ОДЗ") }))
    }

    func testPolynomialAndRepeatedRoots() throws {
        let quartic = try AlgebraSolver.solve("x^4 - 5x^2 + 4 = 0")
        XCTAssertTrue(quartic.answer.contains("-2"))
        XCTAssertTrue(quartic.answer.contains("2"))
        let repeated = try AlgebraSolver.solve("x^2 - 4x + 4 = 0")
        XCTAssertTrue(repeated.answer.contains("2"))
    }

    func testUnitCatalogMatchesAndroidCategories() throws {
        XCTAssertEqual(UnitConverter.categories.count, 10)
        XCTAssertEqual(try UnitConverter.convert("1", category: "pressure", from: "mpa", to: "pa"), "1000000")
        XCTAssertEqual(try UnitConverter.convert("1", category: "speed", from: "kmh", to: "ms"), "0.2777777777777777777777777777777778")
        XCTAssertThrowsError(try UnitConverter.convert("1", category: "time", from: "week", to: "day"))
    }

}
