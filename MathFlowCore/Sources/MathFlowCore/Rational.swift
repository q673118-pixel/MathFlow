import BigInt
import Foundation

public enum MathFlowMathError: Error, Equatable {
    case invalidNumber
    case divisionByZero
    case malformedExpression
    case unsupportedExpression(String)
}

public enum RationalRoundingMode: Sendable { case halfUp, halfEven }

public struct Rational: Equatable, Comparable, Sendable {
    public let numerator: BigInt
    public let denominator: BigInt

    public static let zero = Rational(uncheckedNumerator: 0, denominator: 1)
    public static let one = Rational(uncheckedNumerator: 1, denominator: 1)

    public init(_ numerator: BigInt, _ denominator: BigInt = 1) throws {
        guard denominator != 0 else { throw MathFlowMathError.divisionByZero }
        var n = numerator
        var d = denominator
        if d < 0 { n = -n; d = -d }
        let absoluteNumerator = n < 0 ? -n : n
        let divisor = Self.gcd(absoluteNumerator, d)
        self.numerator = n / divisor
        self.denominator = d / divisor
    }

    private init(uncheckedNumerator: BigInt, denominator: BigInt) {
        self.numerator = uncheckedNumerator
        self.denominator = denominator
    }

    private static func gcd(_ lhs: BigInt, _ rhs: BigInt) -> BigInt {
        var a = lhs
        var b = rhs
        while b != 0 {
            let remainder = a % b
            a = b
            b = remainder
        }
        return a == 0 ? 1 : a
    }

    public static func parseDecimal(_ source: String) throws -> Rational {
        let normalized = source.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: ",", with: ".")
        guard !normalized.isEmpty else { throw MathFlowMathError.invalidNumber }
        let exponentParts = normalized.lowercased().split(separator: "e", omittingEmptySubsequences: false)
        guard exponentParts.count <= 2 else { throw MathFlowMathError.invalidNumber }
        let mantissa = String(exponentParts[0])
        let exponent: Int
        if exponentParts.count == 2 {
            guard let parsed = Int(exponentParts[1]), (-1000...1000).contains(parsed) else {
                throw MathFlowMathError.invalidNumber
            }
            exponent = parsed
        } else {
            exponent = 0
        }
        let pieces = mantissa.split(separator: ".", omittingEmptySubsequences: false)
        guard pieces.count <= 2 else { throw MathFlowMathError.invalidNumber }
        let rawWhole = String(pieces[0])
        let fraction = pieces.count == 2 ? String(pieces[1]) : ""
        guard !(rawWhole.isEmpty && fraction.isEmpty) else { throw MathFlowMathError.invalidNumber }
        let whole = rawWhole.isEmpty ? "0" : rawWhole
        guard whole.allSatisfy({ $0.isNumber || $0 == "-" || $0 == "+" }),
              fraction.allSatisfy({ $0.isNumber }) else { throw MathFlowMathError.invalidNumber }
        guard let integer = BigInt(whole + fraction) else { throw MathFlowMathError.invalidNumber }
        let decimalScale = fraction.count - exponent
        var scale: BigInt = 1
        if decimalScale >= 0 {
            for _ in 0..<decimalScale { scale *= 10 }
            return try Rational(integer, scale)
        }
        var multiplier: BigInt = 1
        for _ in 0..<(-decimalScale) { multiplier *= 10 }
        return try Rational(integer * multiplier, 1)
    }

    public static func < (lhs: Rational, rhs: Rational) -> Bool {
        lhs.numerator * rhs.denominator < rhs.numerator * lhs.denominator
    }

    public static func + (lhs: Rational, rhs: Rational) -> Rational {
        try! Rational(lhs.numerator * rhs.denominator + rhs.numerator * lhs.denominator,
                      lhs.denominator * rhs.denominator)
    }

    public static func - (lhs: Rational, rhs: Rational) -> Rational {
        try! Rational(lhs.numerator * rhs.denominator - rhs.numerator * lhs.denominator,
                      lhs.denominator * rhs.denominator)
    }

    public static func * (lhs: Rational, rhs: Rational) -> Rational {
        try! Rational(lhs.numerator * rhs.numerator, lhs.denominator * rhs.denominator)
    }

    public func divided(by other: Rational) throws -> Rational {
        guard other.numerator != 0 else { throw MathFlowMathError.divisionByZero }
        return try Rational(numerator * other.denominator, denominator * other.numerator)
    }

    public func power(_ exponent: Int) throws -> Rational {
        guard (-1000...1000).contains(exponent) else {
            throw MathFlowMathError.unsupportedExpression("Показатель степени слишком большой")
        }
        if exponent == 0 { return .one }
        if exponent < 0 {
            guard numerator != 0 else { throw MathFlowMathError.divisionByZero }
            return try Rational(denominatorPower(-exponent), numeratorPower(-exponent))
        }
        return try Rational(numeratorPower(exponent), denominatorPower(exponent))
    }

    private func numeratorPower(_ exponent: Int) -> BigInt {
        var result: BigInt = 1
        for _ in 0..<exponent { result *= numerator }
        return result
    }

    private func denominatorPower(_ exponent: Int) -> BigInt {
        var result: BigInt = 1
        for _ in 0..<exponent { result *= denominator }
        return result
    }


    public var doubleValue: Double {
        guard numerator != 0 else { return 0 }
        let negative = numerator < 0
        let numeratorDigits = String(negative ? -numerator : numerator)
        let denominatorDigits = String(denominator)
        let precision = 17
        let numeratorHeadText = String(numeratorDigits.prefix(precision))
        let denominatorHeadText = String(denominatorDigits.prefix(precision))
        guard let numeratorHead = Double(numeratorHeadText),
              let denominatorHead = Double(denominatorHeadText),
              denominatorHead != 0 else { return .nan }
        let exponent = (numeratorDigits.count - numeratorHeadText.count) -
            (denominatorDigits.count - denominatorHeadText.count)
        let magnitude = (numeratorHead / denominatorHead) * pow(10.0, Double(exponent))
        return negative ? -magnitude : magnitude
    }

    public var isInteger: Bool { denominator == 1 }
    public var fractionString: String {
        denominator == 1 ? String(numerator) : "\(numerator)/\(denominator)"
    }

    public var mixedString: String {
        guard denominator != 1 else { return String(numerator) }
        let whole = numerator / denominator
        let remainder = numerator % denominator
        let absoluteRemainder = remainder < 0 ? -remainder : remainder
        guard whole != 0 else { return fractionString }
        return "\(whole) \(absoluteRemainder)/\(denominator)"
    }

    public func decimalString(scale: Int = 10, rounding: RationalRoundingMode = .halfUp) -> String {
        let precision = max(1, min(scale, 100))
        if numerator == 0 { return "0" }
        let negative = numerator < 0
        let absolute = negative ? -numerator : numerator

        // Match BigDecimal MathContext rounding: scale is significant digits.
        let numeratorDigits = String(absolute).count
        let denominatorDigits = String(denominator).count
        var decimalExponent = numeratorDigits - denominatorDigits
        if decimalExponent >= 0 {
            var power: BigInt = 1
            for _ in 0..<decimalExponent { power *= 10 }
            if absolute < denominator * power { decimalExponent -= 1 }
        } else {
            var power: BigInt = 1
            for _ in 0..<(-decimalExponent) { power *= 10 }
            if absolute * power < denominator { decimalExponent -= 1 }
        }

        let decimalPlaces = precision - 1 - decimalExponent
        let roundedInteger: BigInt
        let outputPlaces: Int
        if decimalPlaces >= 0 {
            var factor: BigInt = 1
            for _ in 0..<decimalPlaces { factor *= 10 }
            let scaledNumerator = absolute * factor
            var quotient = scaledNumerator / denominator
            let remainder = scaledNumerator % denominator
            if remainder * 2 > denominator || (remainder * 2 == denominator && (rounding == .halfUp || quotient % 2 != 0)) { quotient += 1 }
            roundedInteger = quotient
            outputPlaces = decimalPlaces
        } else {
            let integerPlaces = -decimalPlaces
            var factor: BigInt = 1
            for _ in 0..<integerPlaces { factor *= 10 }
            let divisor = denominator * factor
            var quotient = absolute / divisor
            let remainder = absolute % divisor
            if remainder * 2 > divisor || (remainder * 2 == divisor && (rounding == .halfUp || quotient % 2 != 0)) { quotient += 1 }
            roundedInteger = quotient * factor
            outputPlaces = 0
        }

        let sign = negative && roundedInteger != 0 ? "-" : ""
        guard outputPlaces > 0 else { return sign + String(roundedInteger) }
        var digits = String(roundedInteger)
        if digits.count <= outputPlaces {
            digits = String(repeating: "0", count: outputPlaces + 1 - digits.count) + digits
        }
        let split = digits.index(digits.endIndex, offsetBy: -outputPlaces)
        let whole = String(digits[..<split])
        var fraction = String(digits[split...])
        while fraction.last == "0" { fraction.removeLast() }
        return fraction.isEmpty ? sign + whole : sign + whole + "." + fraction
    }
}
