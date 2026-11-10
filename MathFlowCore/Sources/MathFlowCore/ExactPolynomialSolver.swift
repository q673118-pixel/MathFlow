import Foundation
import BigInt

private struct ExactPolynomial {
    var c: [Rational]
    init(_ values: [Rational]) {
        var values = values
        while values.count > 1 && values.last == .zero { values.removeLast() }
        c = values.isEmpty ? [.zero] : values
    }
    static let zero = ExactPolynomial([.zero])
    static let one = ExactPolynomial([.one])
    static let x = ExactPolynomial([.zero, .one])
    var degree: Int { c.count - 1 }
    var isZero: Bool { c.allSatisfy { $0 == .zero } }
    subscript(_ i: Int) -> Rational { i < c.count ? c[i] : .zero }
    func adding(_ other: ExactPolynomial) -> ExactPolynomial {
        ExactPolynomial((0...max(degree, other.degree)).map { self[$0] + other[$0] })
    }
    func subtracting(_ other: ExactPolynomial) -> ExactPolynomial {
        ExactPolynomial((0...max(degree, other.degree)).map { self[$0] - other[$0] })
    }
    func multiplied(by other: ExactPolynomial) throws -> ExactPolynomial {
        if isZero || other.isZero { return .zero }
        guard degree + other.degree <= 4 else { throw MathFlowMathError.unsupportedExpression("После преобразования степень уравнения выше четвёртой") }
        var values = Array(repeating: Rational.zero, count: degree + other.degree + 1)
        for i in 0...degree { for j in 0...other.degree { values[i+j] = values[i+j] + self[i] * other[j] } }
        return ExactPolynomial(values)
    }
    func power(_ exponent: Int) throws -> ExactPolynomial {
        guard (0...4).contains(exponent) else { throw MathFlowMathError.unsupportedExpression("Поддерживаются степени от 0 до 4") }
        var value = Self.one
        for _ in 0..<exponent { value = try value.multiplied(by: self) }
        return value
    }
    func value(_ x: Double) -> Double {
        c.reversed().reduce(0.0) { $0 * x + $1.doubleValue }
    }
}

private struct ExactRationalExpression {
    let numerator: ExactPolynomial
    let denominatorFactors: [ExactPolynomial]
    let domainFactors: [ExactPolynomial]
    var denominatorProduct: ExactPolynomial {
        denominatorFactors.reduce(.one) { partial, factor in (try? partial.multiplied(by: factor)) ?? .zero }
    }
    static func polynomial(_ value: ExactPolynomial) -> ExactRationalExpression {
        ExactRationalExpression(numerator:value, denominatorFactors:[], domainFactors:[])
    }
    func adding(_ other: ExactRationalExpression) throws -> ExactRationalExpression {
        let n = try numerator.multiplied(by: other.denominatorProduct).adding(other.numerator.multiplied(by: denominatorProduct))
        return ExactRationalExpression(numerator:n, denominatorFactors:denominatorFactors+other.denominatorFactors, domainFactors:domainFactors+other.domainFactors)
    }
    func subtracting(_ other: ExactRationalExpression) throws -> ExactRationalExpression {
        let n = try numerator.multiplied(by: other.denominatorProduct).subtracting(other.numerator.multiplied(by: denominatorProduct))
        return ExactRationalExpression(numerator:n, denominatorFactors:denominatorFactors+other.denominatorFactors, domainFactors:domainFactors+other.domainFactors)
    }
    func multiplied(by other: ExactRationalExpression) throws -> ExactRationalExpression {
        ExactRationalExpression(numerator:try numerator.multiplied(by:other.numerator), denominatorFactors:denominatorFactors+other.denominatorFactors, domainFactors:domainFactors+other.domainFactors)
    }
    func divided(by other: ExactRationalExpression) throws -> ExactRationalExpression {
        guard !other.numerator.isZero else { throw MathFlowMathError.divisionByZero }
        let denominator = try numerator.multiplied(by: other.denominatorProduct)
        let domains = domainFactors + other.domainFactors + (other.numerator.degree > 0 ? [other.numerator] : [])
        return ExactRationalExpression(numerator:denominator, denominatorFactors:denominatorFactors+[other.numerator], domainFactors:domains)
    }
    func power(_ exponent: Int) throws -> ExactRationalExpression {
        guard (-4...4).contains(exponent) else { throw MathFlowMathError.unsupportedExpression("Поддерживаются степени от −4 до 4") }
        if exponent == 0 { return ExactRationalExpression(numerator:.one, denominatorFactors:[], domainFactors:domainFactors) }
        if exponent > 0 {
            var factors:[ExactPolynomial] = []
            for factor in denominatorFactors { for _ in 0..<exponent { factors.append(factor) } }
            return ExactRationalExpression(numerator:try numerator.power(exponent), denominatorFactors:factors, domainFactors:domainFactors)
        }
        guard !numerator.isZero else { throw MathFlowMathError.divisionByZero }
        let degree = -exponent
        let newDomain = domainFactors + (numerator.degree > 0 ? [numerator] : [])
        return ExactRationalExpression(numerator:try denominatorProduct.power(degree), denominatorFactors:Array(repeating:numerator,count:degree), domainFactors:newDomain)
    }
}

private struct ExactEquationParser {
    private enum Kind: Equatable { case number, variable, plus, minus, multiply, divide, power, left, right, end }
    private struct Token { let kind: Kind; let text: String }
    private let tokens:[Token]
    private var index = 0
    init(_ source: String) throws {
        let chars=Array(source); var result:[Token]=[]; var i=0
        while i<chars.count {
            let ch=chars[i]
            if ch.isWhitespace { i+=1; continue }
            if (ch.isNumber && !["²", "³", "⁴"].contains(ch)) || ch == "." || ch == "," {
                let start=i; var separators=0
                while i<chars.count && ((chars[i].isNumber && !["²", "³", "⁴"].contains(chars[i])) || chars[i] == "." || chars[i] == ",") {
                    if chars[i] == "." || chars[i] == "," { separators+=1 }
                    if separators>1 { throw MathFlowMathError.invalidNumber }
                    i+=1
                }
                result.append(Token(kind:.number,text:String(chars[start..<i]).replacingOccurrences(of:",",with:"."))); continue
            }
            if ch == "x" || ch == "X" { result.append(Token(kind:.variable,text:"x")); i+=1; continue }
            let kind:Kind
            switch ch {
            case "+": kind = .plus
            case "-", "−": kind = .minus
            case "*", "×": kind = .multiply
            case "/", "÷": kind = .divide
            case "^", "²", "³", "⁴": kind = .power
            case "(": kind = .left
            case ")": kind = .right
            default: throw MathFlowMathError.unsupportedExpression("Символ не поддерживается символьным решателем: \(ch)")
            }
            result.append(Token(kind:kind,text:String(ch))); i+=1
        }
        result.append(Token(kind:.end,text:"")); tokens=result
    }
    private var peek:Token { tokens[index] }
    private mutating func take()->Token { defer { index+=1 }; return tokens[index] }
    private mutating func expression() throws -> ExactRationalExpression {
        var value=try term()
        while peek.kind == .plus || peek.kind == .minus {
            let op=take().kind; let rhs=try term()
            value = op == .plus ? try value.adding(rhs) : try value.subtracting(rhs)
        }
        return value
    }
    private mutating func term() throws -> ExactRationalExpression {
        var value=try unary()
        while peek.kind == .multiply || peek.kind == .divide || peek.kind == .left || peek.kind == .variable || peek.kind == .number {
            let op:Kind
            if peek.kind == .multiply || peek.kind == .divide { op=take().kind } else { op = .multiply }
            let rhs=try unary()
            value = op == .divide ? try value.divided(by:rhs) : try value.multiplied(by:rhs)
        }
        return value
    }
    private mutating func unary() throws -> ExactRationalExpression {
        if peek.kind == .plus { _=take(); return try unary() }
        if peek.kind == .minus {
            _=take(); let value=try unary()
            return ExactRationalExpression(numerator:ExactPolynomial(value.numerator.c.map { Rational.zero - $0 }),denominatorFactors:value.denominatorFactors,domainFactors:value.domainFactors)
        }
        return try power()
    }
    private mutating func power() throws -> ExactRationalExpression {
        let base=try primary()
        guard peek.kind == .power else { return base }
        let powerToken=take()
        if powerToken.text == "²" { return try base.power(2) }
        if powerToken.text == "³" { return try base.power(3) }
        if powerToken.text == "⁴" { return try base.power(4) }
        var sign=1
        if peek.kind == .minus { _=take(); sign = -1 } else if peek.kind == .plus { _=take() }
        guard peek.kind == .number, let n=Int(take().text), n<=4 else { throw MathFlowMathError.unsupportedExpression("Поддерживаются целые показатели от −4 до 4") }
        return try base.power(sign*n)
    }
    private mutating func primary() throws -> ExactRationalExpression {
        let token=take()
        switch token.kind {
        case .number: return .polynomial(ExactPolynomial([try Rational.parseDecimal(token.text)]))
        case .variable: return .polynomial(.x)
        case .left:
            let value=try expression()
            guard peek.kind == .right else { throw MathFlowMathError.malformedExpression }
            _=take(); return value
        default: throw MathFlowMathError.malformedExpression
        }
    }
    mutating func parse() throws -> ExactRationalExpression {
        let value=try expression()
        guard peek.kind == .end else { throw MathFlowMathError.malformedExpression }
        return value
    }
}

enum ExactPolynomialEquationSolver {
    static func solve(_ equation:String) throws -> EquationSolution? {
        let parts=equation.components(separatedBy:"=")
        guard parts.count==2, !parts[0].trimmingCharacters(in:.whitespaces).isEmpty,
              !parts[1].trimmingCharacters(in:.whitespaces).isEmpty else { throw MathFlowMathError.malformedExpression }
        var leftParser=try ExactEquationParser(parts[0]), rightParser=try ExactEquationParser(parts[1])
        let left=try leftParser.parse(), right=try rightParser.parse()
        let reduced=try left.numerator.multiplied(by:right.denominatorProduct).subtracting(right.numerator.multiplied(by:left.denominatorProduct))
        let excluded=(left.domainFactors+right.domainFactors).flatMap { realRoots($0) }.sorted()
        func allowed(_ x:Double)->Bool { excluded.allSatisfy { abs($0-x)>1e-7*max(1,abs(x)) } }
        let domainLine=excluded.isEmpty ? [] : ["ОДЗ: исключаем x = "+excluded.map(format).joined(separator:", ")]
        if reduced.isZero {
            let answer=excluded.isEmpty ? "x — любое число" : "x — любое число, кроме "+excluded.map(format).joined(separator:", ")
            return EquationSolution(kind:"identity",answer:answer,steps:["Получили верное равенство 0 = 0"]+domainLine)
        }
        let degree=reduced.degree
        if degree==0 { return EquationSolution(kind:"no_solution",answer:"Решений нет",steps:["Получили противоречие: \(reduced[0].fractionString) = 0"]+domainLine) }
        if degree==1 {
            let root=try Rational(-reduced[0].numerator*reduced[1].denominator,reduced[0].denominator*reduced[1].numerator)
            let x=double(root)
            guard allowed(x) else { return EquationSolution(kind:"no_solution",answer:"Решений нет",steps:["Корень исключён условиями ОДЗ"]+domainLine) }
            return EquationSolution(kind:"linear",answer:"x = \(root.mixedString) (\(root.decimalString()))",steps:["Переносим всё в левую часть: \(reduced[1].fractionString)x = \(fraction(Rational.zero - reduced[0]))","Делим обе части на коэффициент при x.","x = \(root.fractionString)"]+domainLine)
        }
        if degree==2 {
            let a=reduced[2], b=reduced[1], c=reduced[0]
            let d=b*b-(try Rational(4))*a*c
            if d < .zero { return EquationSolution(kind:"no_real_roots",answer:"Действительных корней нет",steps:["Дискриминант D = \(d.fractionString) < 0. Действительных корней нет."]+domainLine) }
            if let sd=exactSquareRoot(d) {
                let denom=(try Rational(2))*a
                let x1=try (Rational.zero-b+sd).divided(by:denom), x2=try (Rational.zero-b-sd).divided(by:denom)
                let roots=(x1 == x2 ? [x1] : [x1,x2]).filter { allowed(double($0)) }.sorted()
                guard !roots.isEmpty else { return EquationSolution(kind:"no_solution",answer:"Решений нет",steps:["Все корни исключены ОДЗ"]+domainLine) }
                if roots.count==1 { return EquationSolution(kind:"quadratic_double",answer:"x = \(roots[0].mixedString)",steps:["D = \(d.fractionString)","√D = \(sd.fractionString)","x = \(roots[0].fractionString)"]+domainLine) }
                return EquationSolution(kind:"quadratic",answer:"x₁ = \(roots[1].mixedString), x₂ = \(roots[0].mixedString)",steps:["D = b² − 4ac = \(d.fractionString)","√D = \(sd.fractionString)","x₁ = \(roots[1].fractionString), x₂ = \(roots[0].fractionString)"]+domainLine)
            }
            let ad=double(a), bd=double(b), dd=double(d)
            let raw=[(-bd+sqrt(dd))/(2*ad),(-bd-sqrt(dd))/(2*ad)].filter { $0.isFinite && allowed($0) }.sorted()
            guard !raw.isEmpty else { return EquationSolution(kind:"no_solution",answer:"Решений нет",steps:["Все корни исключены ОДЗ"]+domainLine) }
            return EquationSolution(kind:"quadratic_approx",answer:raw.enumerated().map { "x\($0.offset+1) ≈ \(format($0.element))" }.joined(separator:", "),steps:["D = \(d.fractionString)","Корни вычислены приближённо."]+domainLine)
        }
        let roots=realRoots(reduced).filter(allowed)
        guard !roots.isEmpty else { return EquationSolution(kind:"no_real_roots",answer:"Действительных корней нет",steps:["Ищем действительные корни многочлена степени \(degree)."]+domainLine) }
        let labels=roots.enumerated().map { "x\($0.offset+1) ≈ \(format($0.element))" }
        return EquationSolution(kind:"polynomial_numeric",answer:labels.joined(separator:", "),steps:["Это многочлен степени \(degree). Корни найдены численно."]+domainLine+labels)
    }
    private static func double(_ r:Rational)->Double { r.doubleValue }
    private static func fraction(_ r:Rational)->String { r.fractionString }
    private static func format(_ x:Double)->String {
        if abs(x) < 1.0e-10 { return "0" }
        return String(format: "%.10f", locale: Locale(identifier: "en_US_POSIX"), x)
            .replacingOccurrences(of: #"0+$"#, with: "", options: .regularExpression)
            .replacingOccurrences(of: #"[.]$"#, with: "", options: .regularExpression)
    }
    private static func exactSquareRoot(_ value:Rational)->Rational? {
        guard value >= .zero else { return nil }
        let n=sqrtBig(value.numerator), d=sqrtBig(value.denominator)
        guard n*n==value.numerator, d*d==value.denominator else { return nil }
        return try? Rational(n,d)
    }
    private static func sqrtBig(_ value:BigInt)->BigInt {
        if value<2 { return value }
        var lo:BigInt=1, hi=value
        while hi-lo>1 { let mid=(lo+hi)/2; if mid*mid<=value { lo=mid } else { hi=mid } }
        return lo
    }
    private static func realRoots(_ poly:ExactPolynomial)->[Double] {
        let coefficients = (0...poly.degree).map {
            poly[$0].doubleValue
        }
        return realRoots(coefficients)
    }

    private static func realRoots(_ input:[Double])->[Double] {
        guard !input.isEmpty, input.allSatisfy(\.isFinite) else { return [] }
        let coefficientScale = input.map(abs).max() ?? 0
        guard coefficientScale > 0, coefficientScale.isFinite else { return [] }
        let normalized = input.map { $0 / coefficientScale }
        var degree = normalized.count - 1
        while degree > 0 && normalized[degree] == 0 { degree -= 1 }
        if degree == 0 { return [] }
        if degree == 1 {
            let root = -normalized[0] / normalized[1]
            return root.isFinite ? [root] : []
        }

        let derivative = (0..<degree).map { normalized[$0 + 1] * Double($0 + 1) }
        let criticalPoints = realRoots(derivative).filter(\.isFinite).sorted()
        let leading = abs(normalized[degree])
        guard leading > 0, leading.isFinite else { return [] }
        var ratio = 0.0
        for index in 0..<degree { ratio = max(ratio, abs(normalized[index]) / leading) }
        let bound = min(1.0 + ratio, 1.0e12)
        let points = ([-bound] + criticalPoints.filter { $0 > -bound && $0 < bound } + [bound]).sorted()

        func valueAt(_ x:Double)->Double {
            var value = normalized[degree]
            if degree > 0 {
                for index in stride(from: degree - 1, through: 0, by: -1) {
                    value = value * x + normalized[index]
                }
            }
            return value
        }
        func toleranceAt(_ x:Double)->Double {
            let magnitude = max(1.0, abs(x))
            var scale = 0.0
            var power = 1.0
            for index in 0...degree {
                scale += abs(normalized[index]) * power
                power *= magnitude
            }
            return 1.0e-10 * max(1.0, scale)
        }

        var roots:[Double] = []
        for point in criticalPoints where point >= -bound && point <= bound {
            if abs(valueAt(point)) <= toleranceAt(point) { roots.append(point) }
        }
        if points.count > 1 {
            for index in 0..<(points.count - 1) {
                var low = points[index]
                var high = points[index + 1]
                var lowValue = valueAt(low)
                let highValue = valueAt(high)
                guard lowValue.isFinite, highValue.isFinite, lowValue != 0, highValue != 0,
                      (lowValue < 0) != (highValue < 0) else { continue }
                for _ in 0..<120 {
                    let middle = low + (high - low) / 2
                    let middleValue = valueAt(middle)
                    if middleValue == 0 {
                        low = middle
                        high = middle
                    } else if (middleValue < 0) == (lowValue < 0) {
                        low = middle
                        lowValue = middleValue
                    } else {
                        high = middle
                    }
                }
                roots.append(low + (high - low) / 2)
            }
        }
        return roots.filter(\.isFinite).sorted().reduce(into: [Double]()) { result, root in
            if result.isEmpty || abs(result[result.count - 1] - root) > 1.0e-7 * max(1.0, abs(root)) {
                result.append(root)
            }
        }
    }
}
