import Foundation

public struct EquationSolution: Sendable {
    public let kind: String
    public let answer: String
    public let steps: [String]
}

public enum AlgebraSolver {
    public static func solve(_ equation: String, searchRange: ClosedRange<Double> = -1000...1000) throws -> EquationSolution {
        let parts = equation.components(separatedBy: "=")
        guard parts.count == 2, !parts[0].trimmingCharacters(in: .whitespaces).isEmpty,
              !parts[1].trimmingCharacters(in: .whitespaces).isEmpty else {
            throw MathFlowMathError.malformedExpression
        }
        let left = parts[0], right = parts[1]
        let engine = ExpressionEngine()
        func residual(_ x: Double) -> Double? {
            guard let a = try? engine.calculate(left, variables: ["x": x], includeSteps: false),
                  let b = try? engine.calculate(right, variables: ["x": x], includeSteps: false),
                  let av = Double(a.decimalValue), let bv = Double(b.decimalValue) else { return nil }
            let value = av - bv
            return value.isFinite ? value : nil
        }
        let count = 20000
        let lower = searchRange.lowerBound, upper = searchRange.upperBound
        guard lower.isFinite, upper.isFinite, lower < upper else { throw MathFlowMathError.invalidNumber }
        let step = (upper - lower) / Double(count)
        var roots: [Double] = []
        var samples: [(Double, Double?)] = []
        samples.reserveCapacity(count + 1)
        for index in 0...count {
            let x = lower + Double(index) * step
            samples.append((x, residual(x)))
        }
        func addRoot(_ value: Double) {
            guard value.isFinite, residual(value).map({ abs($0) < 1e-6 }) == true,
                  !roots.contains(where: { abs($0 - value) < 1e-5 * max(1, abs(value)) }) else { return }
            roots.append(value)
        }
        for index in 0..<count {
            guard let a = samples[index].1, let b = samples[index + 1].1 else { continue }
            if abs(a) < 1e-10 { addRoot(samples[index].0) }
            if (a < 0 && b > 0) || (a > 0 && b < 0) {
                var lo = samples[index].0, hi = samples[index + 1].0
                var flo = a
                for _ in 0..<80 {
                    let mid = (lo + hi) / 2
                    guard let fm = residual(mid) else { break }
                    if abs(fm) < 1e-12 { lo = mid; hi = mid; break }
                    if (flo < 0 && fm > 0) || (flo > 0 && fm < 0) { hi = mid }
                    else { lo = mid; flo = fm }
                }
                addRoot((lo + hi) / 2)
            }
        }
        // Repeated/even-multiplicity roots do not change sign. Refine local minima of |f(x)|.
        for index in 1..<count {
            guard let previous = samples[index - 1].1, let current = samples[index].1,
                  let next = samples[index + 1].1,
                  abs(current) <= abs(previous), abs(current) <= abs(next),
                  abs(current) < 0.05 else { continue }
            var lo = samples[index - 1].0, hi = samples[index + 1].0
            let ratio = 0.6180339887498949
            var x1 = hi - ratio * (hi - lo), x2 = lo + ratio * (hi - lo)
            for _ in 0..<70 {
                let f1 = abs(residual(x1) ?? .infinity), f2 = abs(residual(x2) ?? .infinity)
                if f1 < f2 { hi = x2; x2 = x1; x1 = hi - ratio * (hi - lo) }
                else { lo = x1; x1 = x2; x2 = lo + ratio * (hi - lo) }
            }
            addRoot((lo + hi) / 2)
        }
        roots.sort()
        let formatted = roots.map(format)
        let steps = [
            "Переносим всё в левую часть: \(left) − (\(right)) = 0",
            "Ищем действительные корни численно на интервале [\(format(lower)); \(format(upper))].",
            "Проверяем найденные значения подстановкой в исходное уравнение."
        ]
        if roots.isEmpty {
            return EquationSolution(kind: "no_real_roots", answer: "На выбранном интервале действительных корней не найдено", steps: steps)
        }
        if roots.count == 1 {
            return EquationSolution(kind: "general_numeric", answer: "x ≈ \(formatted[0])", steps: steps + ["x ≈ \(formatted[0])"])
        }
        let labels = roots.enumerated().map { "x\(subscript($0.offset + 1)) ≈ \(format($0.element))" }
        return EquationSolution(kind: "general_numeric", answer: labels.joined(separator: ", "), steps: steps + labels)
    }

    private static func format(_ value: Double) -> String {
        if abs(value) < 1e-10 { return "0" }
        return String(format: "%.12g", value)
    }
    private static func subscript(_ value: Int) -> String {
        let digits = ["₀","₁","₂","₃","₄","₅","₆","₇","₈","₉"]
        return String(value).map { Int(String($0)).map { digits[$0] } ?? String($0) }.joined()
    }
}
