import XCTest
@testable import MathFlowCore

final class ExpressionEngineTests: XCTestCase {
    func testExactFractionArithmetic() throws {
        let result = try ExpressionEngine().calculate("0.5 + 1/4")
        XCTAssertEqual(result.displayValue, "3/4")
        XCTAssertEqual(result.decimalValue, "0.75")
    }

    func testOperatorPrecedenceAndParentheses() throws {
        XCTAssertEqual(try ExpressionEngine().calculate("2 + 3 * 4").displayValue, "14")
        XCTAssertEqual(try ExpressionEngine().calculate("(2 + 3) * 4").displayValue, "20")
    }

    func testImplicitMultiplication() throws {
        XCTAssertEqual(try ExpressionEngine().calculate("2(3+4)").displayValue, "14")
    }

    func testIntegerPowersAndPostfixPercent() throws {
        XCTAssertEqual(try ExpressionEngine().calculate("3^4").displayValue, "81")
        XCTAssertEqual(try ExpressionEngine().calculate("25%").displayValue, "1/4")
    }

    func testPerfectSquareRootIsExact() throws {
        let result = try ExpressionEngine().calculate("sqrt(81/16)")
        XCTAssertEqual(result.displayValue, "2 1/4")
        XCTAssertEqual(result.exactValue, try Rational(9, 4))
    }

    func testDegreeTrigonometry() throws {
        let result = try ExpressionEngine().calculate("sin(30)")
        XCTAssertEqual(Double(result.decimalValue) ?? .nan, 0.5, accuracy: 1e-12)
    }

    func testDivisionByZeroThrows() {
        XCTAssertThrowsError(try ExpressionEngine().calculate("1/0"))
    }

    func testImplicitMultiplicationWithConstant() throws {
        let result = try ExpressionEngine().calculate("2pi")
        XCTAssertNil(result.exactValue)
        XCTAssertEqual(Double(result.decimalValue)!, 2 * Double.pi, accuracy: 1e-9)
    }

    func testGraphVariableInsidePolynomial() throws {
        let result = try ExpressionEngine().calculate("x^2 + 2x + 1", variables: ["x": 3], includeSteps: false)
        XCTAssertEqual(result.displayValue, "16")
        XCTAssertTrue(result.steps.isEmpty)
    }

    func testGraphVariableInsideTrigonometricFunction() throws {
        let result = try ExpressionEngine().calculate("sin(x)", variables: ["x": 30])
        XCTAssertEqual(Double(result.decimalValue)!, 0.5, accuracy: 1e-9)
    }

    func testAbsoluteValueAndExactSquareRoot() throws {
        let absolute = try ExpressionEngine().calculate("abs(-7/3)")
        XCTAssertEqual(absolute.displayValue, "2 1/3")
        let root = try ExpressionEngine().calculate("sqrt(81/16)")
        XCTAssertEqual(root.displayValue, "2 1/4")
    }

    func testNegativeIntegerPowerOfPi() throws {
        let result = try ExpressionEngine().calculate("pi^-1")
        XCTAssertEqual(Double(result.decimalValue)!, 1 / Double.pi, accuracy: 1e-9)
    }

    func testCommaDecimalInput() throws {
        XCTAssertEqual(try ExpressionEngine().calculate("1,5 + 2,5").displayValue, "4")
    }
    func testApproximateResultsKeepRoundTripPrecisionAndPlainDecimalFormatting() throws {
        let root = try ExpressionEngine().calculate("sqrt(2)")
        XCTAssertEqual(root.decimalValue, "1.4142135623730951")

        let small = try ExpressionEngine().calculate("sqrt(2)/100000000")
        XCTAssertEqual(Double(small.decimalValue) ?? .nan, sqrt(2.0) / 100000000, accuracy: 1e-22)
        XCTAssertFalse(small.decimalValue.contains("e"))
    }

    func testRadianModeMatchesAndroidEngineOption() throws {
        let result = try ExpressionEngine(angleMode: .radians).calculate("sin(pi/2)")
        XCTAssertEqual(Double(result.decimalValue) ?? .nan, 1.0, accuracy: 1e-12)
    }

    func testFractionalPowersRemainDisabledByDefaultButCanBeEnabledForGraphing() throws {
        XCTAssertThrowsError(try ExpressionEngine().calculate("9^0.5"))
        let result = try ExpressionEngine(allowsFractionalPowers: true).calculate("9^0.5")
        XCTAssertEqual(Double(result.decimalValue) ?? .nan, 3.0, accuracy: 1e-12)
    }

    func testLargeExactRationalIsConvertedWithoutOverflowDuringFunctionEvaluation() throws {
        let result = try ExpressionEngine().calculate("sin(1e400/1e399)")
        XCTAssertEqual(Double(result.decimalValue)!, sin(Double.pi / 18), accuracy: 1e-12)
    }

    func testNonFiniteGraphVariableIsRejected() {
        XCTAssertThrowsError(try ExpressionEngine().calculate("x", variables: ["x": .infinity]))
        XCTAssertThrowsError(try ExpressionEngine().calculate("x", variables: ["x": .nan]))
    }

    func testNegativeLargeRationalIsPreservedDuringFunctionEvaluation() throws {
        let result = try ExpressionEngine().calculate("sin(-1e400/1e399)")
        XCTAssertEqual(Double(result.decimalValue)!, -sin(Double.pi / 18), accuracy: 1e-12)
    }

}
