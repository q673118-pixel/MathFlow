import XCTest
@testable import MathFlowCore

final class RationalTests: XCTestCase {
    func testExactDecimalParsing() throws {
        let value = try Rational.parseDecimal("0.125")
        XCTAssertEqual(value.fractionString, "1/8")
        XCTAssertEqual(value.decimalString(), "0.125")
    }

    func testReducedFractionArithmetic() throws {
        let a = try Rational(3, 4)
        let b = try Rational(2, 5)
        XCTAssertEqual((a + b).fractionString, "23/20")
        XCTAssertEqual((a * b).fractionString, "3/10")
        XCTAssertEqual(try a.divided(by: b).fractionString, "15/8")
    }

    func testNegativeMixedNumber() throws {
        XCTAssertEqual(try Rational(-7, 3).mixedString, "-2 1/3")
    }

    func testDivisionByZero() throws {
        XCTAssertThrowsError(try Rational(1, 2).divided(by: .zero))
    }

    func testNegativePower() throws {
        XCTAssertEqual(try Rational(2, 3).power(-2).fractionString, "9/4")
    }

    func testDecimalRoundingCarriesIntoWholePart() throws {
        XCTAssertEqual(try Rational(1999, 1000).decimalString(scale: 2), "2")
    }
}
