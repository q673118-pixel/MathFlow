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
    public init(angleMode: AngleMode = .degrees) { self.angleMode = angleMode }

    public func calculate(_ source: String, variables: [String: Double] = [:], includeSteps: Bool = true) throws -> CalculationResult {
        guard !source.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            throw MathFlowMathError.malformedExpression
        }
        let parser = try Parser(source: source, angleMode: angleMode, variables: variables, includeSteps: includeSteps)
        let value = try parser.parse()
        switch value {
        case .exact(let rational):
            return CalculationResult(exactValue: rational, displayValue: rational.mixedString,
                                     decimalValue: rational.decimalString(), steps: parser.steps)
        case .approximate(let number):
            let text = Self.format(number)
            return CalculationResult(exactValue: nil, displayValue: text, decimalValue: text, steps: parser.steps)
        }
    }

    private static func format(_ value: Double) -> String {
        guard value.isFinite else { return "Ошибка" }
        if value == 0 { return "0" }
        var text = String(format: "%.15g", value)
        if text.contains("e") || text.contains("E") { return text }
        if text.contains(".") {
            while text.last == "0" { text.removeLast() }
            if text.last == "." { text.removeLast() }
        }
        return text
    }
}

private enum MathValue {
    case exact(Rational)
    case approximate(Double)

    var doubleValue: Double {
        switch self {
        case .exact(let value):
            return (Double(String(value.numerator)) ?? 0) / (Double(String(value.denominator)) ?? 1)
        case .approximate(let value): return value
        }
    }

    var description: String {
        switch self {
        case .exact(let value): return value.fractionString
        case .approximate(let value): return String(format: "%.15g", value)
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
    fileprivate var steps: [String] = []

    init(source: String, angleMode: AngleMode, variables: [String: Double], includeSteps: Bool) throws {
        self.angleMode = angleMode
        self.variables = variables
        self.includeSteps = includeSteps
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
            if c.isNumber || c == "." || c == "," {
                let start = i
                var separators = 0
                while i < chars.count && (chars[i].isNumber || chars[i] == "." || chars[i] == ",") {
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
            case .approximate(let value): return .approximate(-value)
            }
        }
        return try parsePower()
    }

    private func parsePower() throws -> MathValue {
        let base = try parsePostfix()
        guard peek.kind == .power else { return base }
        _ = take()
        let exponentValue = try parseUnary()
        guard case .exact(let exponent) = exponentValue, exponent.isInteger,
              let n = Int(String(exponent.numerator)), (-1000...1000).contains(n) else {
            throw MathFlowMathError.unsupportedExpression("Пока поддерживаются только целые показатели степени")
        }
        let result: MathValue
        switch base {
        case .exact(let value): result = .exact(try value.power(n))
        case .approximate(let value):
            let answer = pow(value, Double(n))
            guard answer.isFinite else { throw MathFlowMathError.invalidNumber }
            result = .approximate(answer)
        }
        if includeSteps { steps.append("\(base.description)^\(n) = \(result.description)") }
        return result
    }

    private func parsePostfix() throws -> MathValue {
        var value = try parsePrimary()
        while peek.kind == .percent {
            _ = take()
            value = try binary(value, .exact(Rational(100)), .divide)
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
        if let variable = variables[name] { return .approximate(variable) }
        if name == "pi" { return .approximate(Double.pi) }
        if name == "e" { return .approximate(M_E) }
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
        if includeSteps { steps.append("\(name)(\(argument.description)) ≈ \(String(format: "%.15g", answer))") }
        return .approximate(answer)
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
        let a = lhs.doubleValue, b = rhs.doubleValue
        let value: Double
        switch op {
        case .plus: value = a + b
        case .minus: value = a - b
        case .multiply: value = a * b
        case .divide:
            guard b != 0 else { throw MathFlowMathError.divisionByZero }
            value = a / b
        default: throw MathFlowMathError.malformedExpression
        }
        guard value.isFinite else { throw MathFlowMathError.invalidNumber }
        return .approximate(value)
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
