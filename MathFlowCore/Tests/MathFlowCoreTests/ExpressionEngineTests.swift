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

    func testPostfixPercent() throws {
        XCTAssertEqual(try ExpressionEngine().calculate("25%").displayValue, "1/4")
    }

    func testPerfectSquareRootIsExact() throws {
        let result = try ExpressionEngine().calculate("sqrt(81/16)")
        XCTAssertEqual(result.displayValue, "2 1/4")
        XCTAssertEqual(result.exactValue, try Rational(9, 4))
    }

    func testDegreeTrigonometry() throws {
        let result = try ExpressionEngine().calculate("sin(30)")
        XCTAssertEqual(result.decimalValue, "0.5")
    }

    func testDivisionByZeroThrows() {
        XCTAssertThrowsError(try ExpressionEngine().calculate("1/0"))
    }
}
