import Foundation

public struct EquationSolution: Sendable {
    public let kind: String
    public let answer: String
    public let steps: [String]
}

public enum AlgebraSolver {
    public static func solve(_ equation: String, searchRange: ClosedRange<Double> = -100...100) throws -> EquationSolution {
        let normalized = normalize(equation)
        do {
            if let exact = try ExactPolynomialEquationSolver.solve(normalized) { return exact }
        } catch MathFlowMathError.divisionByZero {
            throw MathFlowMathError.divisionByZero
        } catch {
            // Function expressions and powers above degree four use the numeric parser.
        }
        return try solveGeneral(normalized, searchRange: searchRange)
    }

    private static func normalize(_ source: String) -> String {
        source
            .replacingOccurrences(of: "−", with: "-")
            .replacingOccurrences(of: "×", with: "*")
            .replacingOccurrences(of: "÷", with: "/")
            .replacingOccurrences(of: "π", with: "pi")
            .replacingOccurrences(of: "√", with: "sqrt")
            .replacingOccurrences(of: "⁴", with: "^4")
            .replacingOccurrences(of: "³", with: "^3")
            .replacingOccurrences(of: "²", with: "^2")
    }

    private static func solveGeneral(_ equation: String, searchRange: ClosedRange<Double>) throws -> EquationSolution {
        let parts = equation.components(separatedBy: "=")
        guard parts.count == 2,
              !parts[0].trimmingCharacters(in: .whitespaces).isEmpty,
              !parts[1].trimmingCharacters(in: .whitespaces).isEmpty else {
            throw MathFlowMathError.malformedExpression
        }
        let left = try GeneralEquationParser(parts[0]).parse()
        let right = try GeneralEquationParser(parts[1]).parse()
        func valueAt(_ x: Double) -> Double {
            let value = left(x) - right(x)
            return value.isFinite ? value : .nan
        }

        let lower = searchRange.lowerBound
        let upper = searchRange.upperBound
        guard lower.isFinite, upper.isFinite, lower < upper else { throw MathFlowMathError.invalidNumber }
        let sampleCount = 20_000
        let step = (upper - lower) / Double(sampleCount)
        let values = (0...sampleCount).map { valueAt(lower + Double($0) * step) }
        let validValues = values.filter(\.isFinite)
        guard !validValues.isEmpty else {
            throw MathFlowMathError.unsupportedExpression("На выбранном интервале нет допустимых действительных значений")
        }
        if validValues.allSatisfy({ abs($0) <= 1.0e-11 }) {
            return EquationSolution(
                kind: "general_identity",
                answer: "Равенство верно на всей области определения",
                steps: [
                    "Использовано численное исследование выражения",
                    "На проверенном интервале [\(format(lower)); \(format(upper))] равенство выполняется для всех допустимых значений"
                ]
            )
        }

        var roots: [Double] = []
        func addRoot(_ candidate: Double) {
            guard candidate.isFinite, abs(valueAt(candidate)) <= 1.0e-7,
                  !roots.contains(where: { abs($0 - candidate) <= 1.0e-4 * max(1.0, abs(candidate)) }) else { return }
            roots.append(candidate)
        }

        for index in 0..<sampleCount {
            let x1 = lower + Double(index) * step
            let x2 = x1 + step
            let y1 = values[index]
            let y2 = values[index + 1]
            guard y1.isFinite, y2.isFinite else { continue }
            if abs(y1) <= 1.0e-10 { addRoot(x1) }
            if (y1 < 0) != (y2 < 0) {
                var low = x1
                var high = x2
                var lowValue = y1
                for _ in 0..<90 {
                    let middle = low + (high - low) / 2
                    let middleValue = valueAt(middle)
                    if !middleValue.isFinite {
                        high = middle
                    } else if abs(middleValue) <= 1.0e-14 {
                        low = middle
                        high = middle
                    } else if (middleValue < 0) == (lowValue < 0) {
                        low = middle
                        lowValue = middleValue
                    } else {
                        high = middle
                    }
                }
                addRoot(low + (high - low) / 2)
            }
        }
        if values[sampleCount].isFinite && abs(values[sampleCount]) <= 1.0e-10 { addRoot(upper) }

        // Locate even-multiplicity roots by minimizing |f(x)| around local minima.
        if sampleCount > 1 {
            for index in 1..<sampleCount {
                let before = values[index - 1]
                let current = values[index]
                let after = values[index + 1]
                guard before.isFinite, current.isFinite, after.isFinite,
                      abs(current) <= 0.05,
                      abs(current) <= abs(before),
                      abs(current) <= abs(after) else { continue }
                var low = lower + Double(index - 1) * step
                var high = lower + Double(index + 1) * step
                for _ in 0..<55 {
                    let leftProbe = low + (high - low) / 3
                    let rightProbe = high - (high - low) / 3
                    let leftValue = valueAt(leftProbe)
                    let rightValue = valueAt(rightProbe)
                    if !leftValue.isFinite || (rightValue.isFinite && abs(leftValue) > abs(rightValue)) {
                        low = leftProbe
                    } else {
                        high = rightProbe
                    }
                }
                addRoot(low + (high - low) / 2)
            }
        }

        let ordered = roots.sorted()
        guard !ordered.isEmpty else {
            return EquationSolution(
                kind: "general_no_roots_in_range",
                answer: "На интервале [\(format(lower)); \(format(upper))] действительные корни не найдены",
                steps: [
                    "Уравнение содержит функции или степени, для которых применён численный метод",
                    "Проверена конечная область поиска; это не доказывает отсутствие корней за её пределами"
                ]
            )
        }
        let shown = Array(ordered.prefix(20))
        let labels = shown.enumerated().map { "x\(subscriptDigits($0.offset + 1)) ≈ \(format($0.element))" }
        var steps = [
            "Использован численный метод с проверкой допустимых значений",
            "Область поиска: [\(format(lower)); \(format(upper))]; тригонометрические функции вычисляются в радианах",
            "Каждый найденный корень проверен подстановкой в исходное уравнение"
        ]
        if ordered.count > shown.count {
            steps.append("Показаны первые 20 корней из \(ordered.count) найденных на этом интервале")
        }
        return EquationSolution(kind: "general_numeric", answer: labels.joined(separator: ", "), steps: steps + labels)
    }

    private static func format(_ value: Double) -> String {
        if abs(value) < 1.0e-10 { return "0" }
        return String(format: "%.10f", locale: Locale(identifier: "en_US_POSIX"), value)
            .replacingOccurrences(of: #"0+$"#, with: "", options: .regularExpression)
            .replacingOccurrences(of: #"[.]$"#, with: "", options: .regularExpression)
    }

    private static func subscriptDigits(_ value: Int) -> String {
        let digits = ["₀","₁","₂","₃","₄","₅","₆","₇","₈","₉"]
        return String(value).map { Int(String($0)).map { digits[$0] } ?? String($0) }.joined()
    }
}

private final class GeneralEquationParser {
    private let tokens: [String]
    private var index = 0
    private var absoluteDepth = 0

    init(_ input: String) throws {
        let normalized = input.filter { !$0.isWhitespace }
        let pattern = #"[0-9]+(?:[.,][0-9]+)?|[A-Za-z]+|[()+\-*/^,|]"#
        let regex = try NSRegularExpression(pattern: pattern)
        let ns = normalized as NSString
        let matches = regex.matches(in: normalized, range: NSRange(location: 0, length: ns.length))
        let parsed = matches.map { ns.substring(with: $0.range) }
        guard !parsed.isEmpty, parsed.joined() == normalized else {
            throw MathFlowMathError.unsupportedExpression("Недопустимый символ в уравнении")
        }
        tokens = parsed
    }

    func parse() throws -> (Double) -> Double {
        let result = try expression()
        guard index == tokens.count else { throw MathFlowMathError.malformedExpression }
        return result
    }

    private func expression() throws -> (Double) -> Double {
        var value = try term()
        while peek == "+" || peek == "-" {
            let operation = take()
            let left = value
            let right = try term()
            value = operation == "+" ? { x in left(x) + right(x) } : { x in left(x) - right(x) }
        }
        return value
    }

    private func term() throws -> (Double) -> Double {
        var value = try unary()
        while peek == "*" || peek == "/" || startsPrimary(peek) {
            let operation = (peek == "*" || peek == "/") ? take() : "*"
            let left = value
            let right = try unary()
            value = operation == "*" ? { x in left(x) * right(x) } : { x in left(x) / right(x) }
        }
        return value
    }

    private func startsPrimary(_ token: String?) -> Bool {
        guard let token else { return false }
        return token == "(" || (token == "|" && absoluteDepth == 0) ||
            token.first?.isNumber == true || token.first?.isLetter == true
    }

    private func unary() throws -> (Double) -> Double {
        if peek == "+" { _ = take(); return try unary() }
        if peek == "-" {
            _ = take()
            let value = try unary()
            return { x in -value(x) }
        }
        return try power()
    }

    private func power() throws -> (Double) -> Double {
        let base = try primary()
        guard peek == "^" else { return base }
        _ = take()
        let exponent = try unary()
        return { x in Foundation.pow(base(x), exponent(x)) }
    }

    private func primary() throws -> (Double) -> Double {
        guard let token = peek else { throw MathFlowMathError.malformedExpression }
        _ = take()
        if token == "(" {
            let value = try expression()
            guard take() == ")" else { throw MathFlowMathError.malformedExpression }
            return value
        }
        if token == "|" {
            absoluteDepth += 1
            let value = try expression()
            guard take() == "|" else { throw MathFlowMathError.malformedExpression }
            absoluteDepth -= 1
            return { x in abs(value(x)) }
        }
        if token.lowercased() == "x" { return { x in x } }
        if token.lowercased() == "pi" { return { _ in Double.pi } }
        if token.lowercased() == "e" { return { _ in M_E } }
        if token.first?.isNumber == true {
            guard let number = Double(token.replacingOccurrences(of: ",", with: ".")) else {
                throw MathFlowMathError.invalidNumber
            }
            return { _ in number }
        }
        if token.first?.isLetter == true { return try function(token.lowercased()) }
        throw MathFlowMathError.malformedExpression
    }

    private func function(_ name: String) throws -> (Double) -> Double {
        guard peek == "(" else { throw MathFlowMathError.malformedExpression }
        _ = take()
        let first = try expression()
        let second: ((Double) -> Double)?
        if peek == "," {
            _ = take()
            second = try expression()
        } else {
            second = nil
        }
        guard take() == ")" else { throw MathFlowMathError.malformedExpression }
        switch name {
        case "abs": return { x in abs(first(x)) }
        case "sqrt": return { x in let value = first(x); return value >= 0 ? sqrt(value) : .nan }
        case "sin": return { x in sin(first(x)) }
        case "cos": return { x in cos(first(x)) }
        case "tan": return { x in tan(first(x)) }
        case "asin": return { x in asin(first(x)) }
        case "acos": return { x in acos(first(x)) }
        case "atan": return { x in atan(first(x)) }
        case "ln": return { x in let value = first(x); return value > 0 ? log(value) : .nan }
        case "log": return { x in let value = first(x); return value > 0 ? log10(value) : .nan }
        case "exp": return { x in exp(first(x)) }
        case "min":
            guard let second else { throw MathFlowMathError.malformedExpression }
            return { x in min(first(x), second(x)) }
        case "max":
            guard let second else { throw MathFlowMathError.malformedExpression }
            return { x in max(first(x), second(x)) }
        default: throw MathFlowMathError.unsupportedExpression("Неизвестная функция: \(name)")
        }
    }

    private var peek: String? { index < tokens.count ? tokens[index] : nil }
    private func take() -> String? {
        guard index < tokens.count else { return nil }
        defer { index += 1 }
        return tokens[index]
    }
}
