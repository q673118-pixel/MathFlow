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
        XCTAssertThrowsError(try GeometryTools.rectangle("0", "5"))
        let triangle = try GeometryTools.triangle("3", "4", "5")
        XCTAssertEqual(triangle.area, "6")
        XCTAssertThrowsError(try GeometryTools.triangle("1", "2", "8"))
        let circle = try GeometryTools.circle("1")
        XCTAssertEqual(circle.area, "3.141592653589793")
        XCTAssertEqual(circle.circumference, "6.283185307179586")
    }

    func testPercentageUsesAndroidSixteenDigitPrecision() throws {
        XCTAssertEqual(try PercentTools.whatPercent("1", "3"), "33.33333333333333")
    }

    func testUnitConversions() throws {
        XCTAssertEqual(try UnitConverter.convert("1.25", category: "length", from: "km", to: "m"), "1250")
        XCTAssertEqual(try UnitConverter.convert("1", category: "mass", from: "lb", to: "kg"), "0.45359237")
        XCTAssertEqual(try UnitConverter.convert("32", category: "temperature", from: "f", to: "c"), "0")
        XCTAssertEqual(try UnitConverter.convert("100", category: "temperature", from: "c", to: "f"), "212")
        XCTAssertEqual(try UnitConverter.convert("0", category: "temperature", from: "k", to: "c"), "-273.15")
        XCTAssertEqual(try UnitConverter.convert("1", category: "data", from: "gb", to: "mb"), "1000")
        XCTAssertEqual(try UnitConverter.convert("1", category: "data", from: "kb", to: "b"), "1000")
        XCTAssertEqual(try UnitConverter.convert("1", category: "data", from: "kib", to: "b"), "1024")
        XCTAssertEqual(try UnitConverter.convert("2,5", category: "length", from: "m", to: "cm"), "250")
        XCTAssertEqual(try UnitConverter.convert("1", category: "speed", from: "mph", to: "kmh"), "1.609344")
        XCTAssertThrowsError(try UnitConverter.convert("-1", category: "temperature", from: "k", to: "c"))
        XCTAssertThrowsError(try UnitConverter.convert("1", category: "length", from: "kg", to: "m"))
        XCTAssertThrowsError(try UnitConverter.convert("abc", category: "length", from: "m", to: "km"))
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

    func testLinearEquationWithParenthesesAndQuadraticDoubleRoot() throws {
        XCTAssertTrue(try AlgebraSolver.solve("3(x - 2) = 9").answer.contains("5"))
        let result = try AlgebraSolver.solve("x^2 - 4x + 4 = 0")
        XCTAssertEqual(result.kind, "quadratic_double")
        XCTAssertTrue(result.answer.contains("2"))
    }

    func testIdentityAndContradiction() throws {
        XCTAssertEqual(try AlgebraSolver.solve("2x + 2 = 2(x + 1)").kind, "identity")
        XCTAssertEqual(try AlgebraSolver.solve("x + 1 = x + 2").kind, "no_solution")
    }

    func testCubicQuarticAndQuinticEquations() throws {
        XCTAssertTrue(try AlgebraSolver.solve("x^3 = 8").answer.contains("2"))
        let quartic = try AlgebraSolver.solve("x^4 - 5x^2 + 4 = 0")
        for root in ["-2", "-1", "1", "2"] { XCTAssertTrue(quartic.answer.contains(root)) }
        XCTAssertTrue(try AlgebraSolver.solve("x^5 = 1").answer.contains("1"))
    }

    func testRationalEquationsPreserveDomainRestrictions() throws {
        let linear = try AlgebraSolver.solve("1/(x - 1) = 2")
        XCTAssertTrue(linear.answer.contains("1.5"))
        XCTAssertTrue(linear.steps.contains(where: { $0.contains("ОДЗ") }))
        let identity = try AlgebraSolver.solve("(x - 1)/(x - 1) = 1")
        XCTAssertEqual(identity.kind, "identity")
        XCTAssertTrue(identity.steps.contains(where: { $0.contains("ОДЗ") }))
        let quotient = try AlgebraSolver.solve("(1/x)/(1/(x - 1)) = 2")
        XCTAssertEqual(quotient.kind, "linear")
        XCTAssertTrue(quotient.answer.contains("-1"))
        XCTAssertTrue(quotient.steps.contains(where: { $0.contains("ОДЗ") }))
        let zeroPower = try AlgebraSolver.solve("(1/x)^0 = 1")
        XCTAssertEqual(zeroPower.kind, "identity")
        XCTAssertTrue(zeroPower.answer.contains("0"))
    }

    func testNegativePowersAndNestedRationalExpressions() throws {
        XCTAssertTrue(try AlgebraSolver.solve("x^-1 = 0.5").answer.contains("2"))
        XCTAssertTrue(try AlgebraSolver.solve("(x + 1)/(x - 2) = 3").answer.contains("3.5"))
        let constantDenominator = try AlgebraSolver.solve("x/2 = 3")
        XCTAssertEqual(constantDenominator.kind, "linear")
        XCTAssertTrue(constantDenominator.answer.contains("6"))
        let poweredFraction = try AlgebraSolver.solve("(1/x)^2 = 1/4")
        XCTAssertEqual(poweredFraction.kind, "quadratic")
        XCTAssertTrue(poweredFraction.answer.contains("-2"))
        XCTAssertTrue(poweredFraction.answer.contains("2"))
    }

    func testSuperscriptEquationInput() throws {
        let result = try AlgebraSolver.solve("x³ = 8")
        XCTAssertTrue(result.answer.contains("2"))
    }

    func testGeneralSolverSupportsAbsoluteValueAndFunctions() throws {
        let absolute = try AlgebraSolver.solve("|x| = 2")
        XCTAssertTrue(absolute.answer.contains("-2"))
        XCTAssertTrue(absolute.answer.contains("2"))
        XCTAssertTrue(try AlgebraSolver.solve("sqrt(x) = 2").answer.contains("4"))
        XCTAssertTrue(try AlgebraSolver.solve("ln(x) = 0").answer.contains("1"))
        XCTAssertTrue(try AlgebraSolver.solve("sin(x) = 0", searchRange: -1...1).answer.contains("0"))
    }

    func testGeneralSolverSupportsFractionalPowers() throws {
        let result = try AlgebraSolver.solve("x^0.5 = 3")
        XCTAssertTrue(result.answer.contains("9"))
    }

    func testDivisionByIdenticallyZeroExpressionThrows() {
        XCTAssertThrowsError(try AlgebraSolver.solve("1/(x - x) = 2")) { error in
            XCTAssertEqual(error as? MathFlowMathError, .divisionByZero)
        }
    }

    func testUnicodeMinusAndSuperscriptQuadratic() throws {
        let result = try AlgebraSolver.solve("x² − 5x + 6 = 0")
        XCTAssertEqual(result.kind, "quadratic")
        XCTAssertTrue(result.answer.contains("x₁ = 3"))
        XCTAssertTrue(result.answer.contains("x₂ = 2"))
    }

    func testRepeatedQuarticRootsAreRetained() throws {
        let result = try AlgebraSolver.solve("x^4 - 2x^2 + 1 = 0")
        XCTAssertEqual(result.kind, "polynomial_numeric")
        XCTAssertTrue(result.answer.contains("-1"))
        XCTAssertTrue(result.answer.contains("1"))
        XCTAssertEqual(try AlgebraSolver.solve("x^4 + 1 = 0").kind, "no_real_roots")
        XCTAssertEqual(try AlgebraSolver.solve("x^2 + 1 = 0").kind, "no_real_roots")
    }

    func testGeneralSolverSupportsMinimumFunction() throws {
        XCTAssertTrue(try AlgebraSolver.solve("min(x, 1) = 0.5").answer.contains("0.5"))
    }

    func testGeneralSolverSupportsMaximumFunction() throws {
        XCTAssertTrue(try AlgebraSolver.solve("max(x, 1) = 2").answer.contains("2"))
    }

    func testGeneralSolverSupportsExponentialFunction() throws {
        XCTAssertTrue(try AlgebraSolver.solve("exp(x) = e").answer.contains("1"))
    }

    func testGeneralSolverSupportsInverseTrigonometricFunctions() throws {
        XCTAssertTrue(try AlgebraSolver.solve("asin(x) = 0").answer.contains("0"))
    }

    func testGeneralSolverUsesRadiansForTrigonometricFunctions() throws {
        XCTAssertTrue(try AlgebraSolver.solve("sin(x) = 0.5", searchRange: -1...1).answer.contains("0.5235987756"))
    }

    func testGeneralSolverReportsIdentityAndFiniteRangeNoRoots() throws {
        XCTAssertEqual(try AlgebraSolver.solve("sin(x) = sin(x)").kind, "general_identity")
        XCTAssertEqual(try AlgebraSolver.solve("sin(x) = 2").kind, "general_no_roots_in_range")
    }

    func testEveryAndroidConverterCategoryAndUnitIDIsPresent() {
        let expected: [String: [String]] = [
            "length": ["mm", "cm", "m", "km", "in", "ft", "yd", "mi"],
            "mass": ["mg", "g", "kg", "t", "oz", "lb"],
            "area": ["mm2", "cm2", "m2", "ha", "km2", "acre"],
            "volume": ["ml", "l", "m3", "tsp", "cup", "gal"],
            "temperature": ["c", "f", "k"],
            "speed": ["ms", "kmh", "mph", "kn"],
            "time": ["ms", "s", "min", "h", "day"],
            "pressure": ["pa", "kpa", "mpa", "bar", "atm", "mmhg"],
            "energy": ["j", "kj", "cal", "kcal", "wh", "kwh"],
            "data": ["b", "kb", "mb", "gb", "tb", "kib", "mib", "gib"]
        ]
        XCTAssertEqual(Set(UnitConverter.categories.map(\\.id)), Set(expected.keys))
        for category in UnitConverter.categories {
            XCTAssertEqual(category.units.map(\\.id), expected[category.id], "Unit catalog mismatch for \\(category.id)")
        }
    }

    func testRepresentativeConversionsAcrossAllCategories() throws {
        let cases: [(String, String, String, String, String)] = [
            ("length", "1.25", "km", "m", "1250"),
            ("mass", "1", "kg", "g", "1000"),
            ("area", "1", "ha", "m2", "10000"),
            ("volume", "1", "l", "m3", "0.001"),
            ("temperature", "32", "f", "c", "0"),
            ("speed", "36", "kmh", "ms", "10"),
            ("time", "1", "h", "min", "60"),
            ("pressure", "1", "bar", "pa", "100000"),
            ("energy", "1", "kwh", "j", "3600000"),
            ("data", "1", "gb", "mb", "1000")
        ]
        for (category, value, from, to, expected) in cases {
            XCTAssertEqual(try UnitConverter.convert(value, category: category, from: from, to: to), expected, "Conversion mismatch: \\(category) \\(from) -> \\(to)")
        }
    }

}
