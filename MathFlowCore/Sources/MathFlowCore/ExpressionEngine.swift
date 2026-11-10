import Foundation
import BigInt

public enum AngleMode: Sendable { case degrees, radians }

public struct CalculationResult: Sendable {
    public let exactValue: Rational?
    public let displayValue: String
    public let decimalValue: String
    public let steps: [String]
}

public struct ExpressionEngine {
    private let angleMode: AngleMode
    private let allowsFractionalPowers: Bool
    public init(angleMode: AngleMode = .degrees, allowsFractionalPowers: Bool = false) {
        self.angleMode = angleMode
        self.allowsFractionalPowers = allowsFractionalPowers
    }

    public func calculate(_ source: String, variables: [String: Double] = [:], includeSteps: Bool = true) throws -> CalculationResult {
        guard !source.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            throw MathFlowMathError.malformedExpression
        }
        guard variables.values.allSatisfy(\.isFinite) else {
            throw MathFlowMathError.invalidNumber
        }
        let parser = try Parser(source: source, angleMode: angleMode, variables: variables, includeSteps: includeSteps, allowsFractionalPowers: allowsFractionalPowers)
        let value = try parser.parse()
        switch value {
        case .exact(let rational):
            return CalculationResult(exactValue: rational, displayValue: rational.mixedString,
                                     decimalValue: rational.decimalString(), steps: parser.steps)
        case .approximate(let number):
            let text = MathDecimal.string(number)
            return CalculationResult(exactValue: nil, displayValue: text, decimalValue: text, steps: parser.steps)
        }
    }

    fileprivate static func plainDecimalString(_ value: Double) -> String {
        // Swift's Double description is the shortest round-trip representation.
        // Expand scientific notation because Android's BigDecimal.stripTrailingZeros()
        // returns a plain decimal string for the same Double result.
        let raw = String(value).lowercased()
        guard let exponentMarker = raw.firstIndex(of: "e") else {
            var normalized = raw
            if normalized.contains(".") {
                while normalized.last == "0" { normalized.removeLast() }
                if normalized.last == "." { normalized.removeLast() }
            }
            return normalized
        }
        let mantissa = String(raw[..<exponentMarker])
        let exponentText = String(raw[raw.index(after: exponentMarker)...])
        guard let exponent = Int(exponentText) else { return raw }
        let negative = mantissa.hasPrefix("-")
        let unsigned = negative ? String(mantissa.dropFirst()) : mantissa
        let decimalPosition = (unsigned.firstIndex(of: ".").map { unsigned.distance(from: unsigned.startIndex, to: $0) } ?? unsigned.count) + exponent
        let digits = unsigned.filter(\.isNumber)
        let body: String
        if decimalPosition <= 0 {
            body = "0." + String(repeating: "0", count: -decimalPosition) + digits
        } else if decimalPosition >= digits.count {
            body = digits + String(repeating: "0", count: decimalPosition - digits.count)
        } else {
            let split = digits.index(digits.startIndex, offsetBy: decimalPosition)
            body = String(digits[..<split]) + "." + String(digits[split...])
        }
        var normalized = body
        if normalized.contains(".") {
            while normalized.last == "0" { normalized.removeLast() }
            if normalized.last == "." { normalized.removeLast() }
        }
        return (negative ? "-" : "") + normalized
    }

}

private enum MathDecimal {
    static func fromDouble(_ value: Double) -> Rational {
        guard value.isFinite, let parsed = try? Rational.parseDecimal(String(value)) else { return .zero }
        return rounded(parsed)
    }

    static func fromBinaryDouble(_ value: Double) -> Rational {
        guard value.isFinite, value != 0 else { return .zero }
        let bits = value.bitPattern
        let exponentBits = Int((bits >> 52) & 0x7ff)
        let fractionMask = (UInt64(1) << 52) - 1
        let fraction = bits & fractionMask
        let significand = exponentBits == 0 ? fraction : fraction | (UInt64(1) << 52)
        let binaryExponent = exponentBits == 0 ? -1074 : exponentBits - 1023 - 52
        var numerator = BigInt(String(significand)) ?? 0
        var denominator: BigInt = 1
        if binaryExponent >= 0 {
            for _ in 0..<binaryExponent { numerator *= 2 }
        } else {
            for _ in 0..<(-binaryExponent) { denominator *= 2 }
        }
        if (bits >> 63) == 1 { numerator = -numerator }
        return rounded(try! Rational(numerator, denominator))
    }

    static func fromRational(_ value: Rational) -> Rational {
        rounded(value)
    }

    static func rounded(_ value: Rational) -> Rational {
        try! Rational.parseDecimal(value.decimalString(scale: 24, rounding: .halfEven))
    }

    static func power(_ base: Rational, exponent: Int) throws -> Rational {
        let magnitude = rounded(try base.power(abs(exponent)))
        if exponent >= 0 { return magnitude }
        return try calculate(.one, magnitude, operation: .divide)
    }

    static func calculate(_ lhs: Rational, _ rhs: Rational, operation: TokenKind) throws -> Rational {
        let result: Rational
        switch operation {
        case .plus: result = lhs + rhs
        case .minus: result = lhs - rhs
        case .multiply: result = lhs * rhs
        case .divide: result = try lhs.divided(by: rhs)
        default: throw MathFlowMathError.malformedExpression
        }
        return rounded(result)
    }

    static func string(_ value: Rational) -> String {
        value.decimalString(scale: 24, rounding: .halfEven)
    }

    static func doubleValue(_ value: Rational) -> Double {
        Double(string(value)) ?? (value.numerator < 0 ? -.infinity : .infinity)
    }
}

private enum MathValue {
    case exact(Rational)
    case approximate(Rational)

    var rationalValue: Rational {
        switch self {
        case .exact(let value): return MathDecimal.fromRational(value)
        case .approximate(let value): return value
        }
    }

    var doubleValue: Double {
        MathDecimal.doubleValue(rationalValue)
    }

    var description: String {
        switch self {
        case .exact(let value): return value.fractionString
        case .approximate(let value): return MathDecimal.string(value)
        }
    }
}

private enum TokenKind {
    case number, identifier, plus, minus, multiply, divide, power, percent, left, right, end
}
private struct MathToken {
    let kind: TokenKind
    let text: String
}

private final class Parser {
    private let tokens: [MathToken]
    private var index = 0
    private let angleMode: AngleMode
    private let includeSteps: Bool
    private let variables: [String: Double]
    private let allowsFractionalPowers: Bool
    fileprivate var steps: [String] = []

    init(source: String, angleMode: AngleMode, variables: [String: Double], includeSteps: Bool, allowsFractionalPowers: Bool) throws {
        self.angleMode = angleMode
        self.variables = variables
        self.includeSteps = includeSteps
        self.allowsFractionalPowers = allowsFractionalPowers
        self.tokens = try Self.tokenize(source)
    }

    func parse() throws -> MathValue {
        let result = try parseExpression()
        guard peek.kind == .end else { throw MathFlowMathError.malformedExpression }
        return result
    }

    private static func tokenize(_ source: String) throws -> [MathToken] {
        let chars = Array(source)
        var result: [MathToken] = []
        var i = 0
        while i < chars.count {
            let c = chars[i]
            if c.isWhitespace { i += 1; continue }
            if (c.isNumber && !["²", "³", "⁴"].contains(c)) || c == "." || c == "," {
                let start = i
                var separators = 0
                while i < chars.count && ((chars[i].isNumber && !["²", "³", "⁴"].contains(chars[i])) || chars[i] == "." || chars[i] == ",") {
                    if chars[i] == "." || chars[i] == "," { separators += 1 }
                    if separators > 1 { throw MathFlowMathError.invalidNumber }
                    i += 1
                }
                result.append(MathToken(kind: .number, text: String(chars[start..<i]).replacingOccurrences(of: ",", with: ".")))
                continue
            }
            if c.isLetter || c == "π" {
                let start = i
                while i < chars.count && (chars[i].isLetter || chars[i] == "π") { i += 1 }
                let word = String(chars[start..<i]).lowercased()
                result.append(MathToken(kind: .identifier, text: word == "π" ? "pi" : word))
                continue
            }
            let kind: TokenKind
            switch c {
            case "+": kind = .plus
            case "-", "−": kind = .minus
            case "*", "×": kind = .multiply
            case "/", "÷": kind = .divide
            case "^", "²": kind = .power
            case "%": kind = .percent
            case "(": kind = .left
            case ")": kind = .right
            default: throw MathFlowMathError.unsupportedExpression("Неизвестный символ: \(c)")
            }
            result.append(MathToken(kind: kind, text: String(c)))
            i += 1
        }
        result.append(MathToken(kind: .end, text: ""))
        return result
    }

    private var peek: MathToken { tokens[index] }
    private func take() -> MathToken { defer { index += 1 }; return tokens[index] }
    private func expect(_ kind: TokenKind) throws {
        guard peek.kind == kind else { throw MathFlowMathError.malformedExpression }
        _ = take()
    }

    private func parseExpression() throws -> MathValue {
        var value = try parseTerm()
        while peek.kind == .plus || peek.kind == .minus {
            let op = take()
            let right = try parseTerm()
            let before = value
            value = try binary(value, right, op.kind)
            if includeSteps { steps.append("\(before.description) \(op.text) \(right.description) = \(value.description)") }
        }
        return value
    }

    private func parseTerm() throws -> MathValue {
        var value = try parseUnary()
        while peek.kind == .multiply || peek.kind == .divide || peek.kind == .left || peek.kind == .identifier {
            let op: TokenKind
            let symbol: String
            if peek.kind == .multiply || peek.kind == .divide {
                let token = take()
                op = token.kind
                symbol = token.text
            } else {
                op = .multiply
                symbol = "×"
            }
            let right = try parseUnary()
            let before = value
            value = try binary(value, right, op)
            if includeSteps { steps.append("\(before.description) \(symbol) \(right.description) = \(value.description)") }
        }
        return value
    }

    private func parseUnary() throws -> MathValue {
        if peek.kind == .plus { _ = take(); return try parseUnary() }
        if peek.kind == .minus {
            _ = take()
            switch try parseUnary() {
            case .exact(let value): return .exact(try Rational(-value.numerator, value.denominator))
            case .approximate(let value): return .approximate(try Rational(-value.numerator, value.denominator))
            }
        }
        return try parsePower()
    }

    private func parsePower() throws -> MathValue {
        let base = try parsePostfix()
        guard peek.kind == .power else { return base }
        _ = take()
        let exponentValue = try parseUnary()
        if case .exact(let exponent) = exponentValue, exponent.isInteger,
           let n = Int(String(exponent.numerator)), (-1000...1000).contains(n) {
            let result: MathValue
            switch base {
            case .exact(let value): result = .exact(try value.power(n))
            case .approximate(let value):
                result = .approximate(try MathDecimal.power(value, exponent: n))
            }
            if includeSteps { steps.append("\(base.description)^\(n) = \(result.description)") }
            return result
        }
        guard allowsFractionalPowers else {
            throw MathFlowMathError.unsupportedExpression("Пока поддерживаются только целые показатели степени")
        }
        let answer = pow(base.doubleValue, exponentValue.doubleValue)
        guard answer.isFinite else { throw MathFlowMathError.invalidNumber }
        let result = MathValue.approximate(MathDecimal.fromDouble(answer))
        if includeSteps { steps.append("\(base.description)^\(exponentValue.description) ≈ \(result.description)") }
        return result
    }

    private func parsePostfix() throws -> MathValue {
        var value = try parsePrimary()
        while peek.kind == .percent {
            _ = take()
            value = try binary(value, .exact(try Rational(100)), .divide)
            if includeSteps { steps.append("Процент: \(value.description)") }
        }
        return value
    }

    private func parsePrimary() throws -> MathValue {
        let token = take()
        switch token.kind {
        case .number: return .exact(try Rational.parseDecimal(token.text))
        case .left:
            let value = try parseExpression()
            try expect(.right)
            return value
        case .identifier: return try parseIdentifier(token.text)
        default: throw MathFlowMathError.malformedExpression
        }
    }

    private func parseIdentifier(_ name: String) throws -> MathValue {
        if let variable = variables[name] { return .approximate(MathDecimal.fromDouble(variable)) }
        if name == "pi" { return .approximate(MathDecimal.fromBinaryDouble(Double.pi)) }
        if name == "e" { return .approximate(MathDecimal.fromBinaryDouble(M_E)) }
        try expect(.left)
        let argument = try parseExpression()
        try expect(.right)
        let x = argument.doubleValue
        if name == "abs", case .exact(let exact) = argument {
            let result = try Rational(exact.numerator < 0 ? -exact.numerator : exact.numerator, exact.denominator)
            if includeSteps { steps.append("Модуль: |\(argument.description)| = \(result.fractionString)") }
            return .exact(result)
        }
        if name == "sqrt", case .exact(let exact) = argument,
           exact.numerator >= 0 {
            let n = integerSquareRoot(exact.numerator)
            let d = integerSquareRoot(exact.denominator)
            if n * n == exact.numerator && d * d == exact.denominator {
                let result = try Rational(n, d)
                if includeSteps { steps.append("√\(argument.description) = \(result.fractionString)") }
                return .exact(result)
            }
        }
        let radians = angleMode == .degrees ? x * Double.pi / 180 : x
        let answer: Double
        switch name {
        case "sqrt":
            guard x >= 0 else { throw MathFlowMathError.unsupportedExpression("Корень из отрицательного числа") }
            answer = sqrt(x)
        case "sin": answer = sin(radians)
        case "cos": answer = cos(radians)
        case "tan":
            guard abs(cos(radians)) > 1e-12 else { throw MathFlowMathError.unsupportedExpression("Тангенс не определён для этого угла") }
            answer = tan(radians)
        case "ln":
            guard x > 0 else { throw MathFlowMathError.unsupportedExpression("Аргумент ln должен быть положительным") }
            answer = log(x)
        case "log":
            guard x > 0 else { throw MathFlowMathError.unsupportedExpression("Аргумент log должен быть положительным") }
            answer = log10(x)
        case "abs": answer = abs(x)
        default: throw MathFlowMathError.unsupportedExpression("Неизвестная функция: \(name)")
        }
        guard answer.isFinite else { throw MathFlowMathError.invalidNumber }
        let approximate = MathDecimal.fromDouble(answer)
        if includeSteps { steps.append("\(name)(\(argument.description)) ≈ \(MathDecimal.string(approximate))") }
        return .approximate(approximate)
    }

    private func binary(_ lhs: MathValue, _ rhs: MathValue, _ op: TokenKind) throws -> MathValue {
        if case .exact(let a) = lhs, case .exact(let b) = rhs {
            switch op {
            case .plus: return .exact(a + b)
            case .minus: return .exact(a - b)
            case .multiply: return .exact(a * b)
            case .divide: return .exact(try a.divided(by: b))
            default: throw MathFlowMathError.malformedExpression
            }
        }
        return .approximate(try MathDecimal.calculate(lhs.rationalValue, rhs.rationalValue, operation: op))
    }

    private func integerSquareRoot(_ value: BigInt) -> BigInt {
        if value < 2 { return value }
        var low: BigInt = 1
        var high = value
        while high - low > 1 {
            let middle = (low + high) / 2
            if middle * middle <= value { low = middle } else { high = middle }
        }
        return low
    }
}
