import Foundation

public enum MathFlowToolError: Error, LocalizedError, Equatable {
    case emptyInput
    case invalidNumber(String)
    case divisionByZero
    case invalidOperation
    case invalidGeometry(String)
    case unknownUnit
    case belowAbsoluteZero

    public var errorDescription: String? {
        switch self {
        case .emptyInput: return "Заполните все поля"
        case .invalidNumber(let value): return "Некорректное число: \(value)"
        case .divisionByZero: return "Деление на ноль"
        case .invalidOperation: return "Неизвестная операция"
        case .invalidGeometry(let message): return message
        case .unknownUnit: return "Неизвестная единица измерения"
        case .belowAbsoluteZero: return "Температура ниже абсолютного нуля"
        }
    }
}

private func toolDecimal(_ source: String) throws -> Decimal {
    let normalized = source.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: ",", with: ".")
    guard !normalized.isEmpty else { throw MathFlowToolError.emptyInput }
    let numericPattern = #"^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:[eE][+-]?\d+)?$"#
    guard normalized.range(of: numericPattern, options: .regularExpression) != nil else {
        throw MathFlowToolError.invalidNumber(source)
    }
    guard let value = Decimal(string: normalized, locale: Locale(identifier: "en_US_POSIX")), !value.isNaN else {
        throw MathFlowToolError.invalidNumber(source)
    }
    return value
}

private func toolDecimalFromDouble(_ value: Double) -> Decimal {
    Decimal(string: String(value), locale: Locale(identifier: "en_US_POSIX")) ?? .zero
}

private func toolRound(_ value: Decimal) -> Decimal {
    let magnitude = abs(NSDecimalNumber(decimal: value).doubleValue)
    guard magnitude.isFinite, magnitude > 0 else { return value }
    var input = value
    var rounded = value
    // Android MathContext(16, HALF_UP): 16 significant decimal digits.
    let significantScale = max(-128, min(127, 15 - Int(floor(log10(magnitude)))))
    NSDecimalRound(&rounded, &input, significantScale, .plain)
    return rounded
}

private func toolFormat(_ value: Decimal) -> String {
    var rounded = toolRound(value)
    var source = NSDecimalString(&rounded, Locale(identifier: "en_US_POSIX"))
    if source.contains(".") {
        while source.last == "0" { source.removeLast() }
        if source.last == "." { source.removeLast() }
    }
    return source == "-0" ? "0" : source
}

public enum FractionOperation: String, CaseIterable, Sendable { case add = "+", subtract = "−", multiply = "×", divide = "÷" }

public struct FractionToolResult: Sendable {
    public let exact: String
    public let mixed: String
    public let decimal: String
}

public enum FractionTools {
    public static func parse(_ source: String) throws -> Rational {
        let text = source.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: ",", with: ".")
        guard !text.isEmpty else { throw MathFlowToolError.emptyInput }
        let pieces = text.split(separator: "/", omittingEmptySubsequences: false)
        guard pieces.count <= 2 else { throw MathFlowToolError.invalidNumber(source) }
        if pieces.count == 1 { return try Rational.parseDecimal(text) }
        let numerator = try Rational.parseDecimal(String(pieces[0]).trimmingCharacters(in: .whitespaces))
        let denominator = try Rational.parseDecimal(String(pieces[1]).trimmingCharacters(in: .whitespaces))
        guard denominator.numerator != 0 else { throw MathFlowToolError.divisionByZero }
        return try numerator.divided(by: denominator)
    }

    public static func calculate(_ first: String, _ second: String, operation: FractionOperation) throws -> FractionToolResult {
        let a = try parse(first), b = try parse(second)
        let result: Rational
        switch operation {
        case .add: result = a + b
        case .subtract: result = a - b
        case .multiply: result = a * b
        case .divide: result = try a.divided(by: b)
        }
        return FractionToolResult(exact: result.fractionString, mixed: result.mixedString, decimal: result.decimalString(scale: 12))
    }
}

public enum PercentTools {
    public static func percentOf(_ percent: String, _ amount: String) throws -> String {
        let p = try toolDecimal(percent), a = try toolDecimal(amount)
        return toolFormat(toolRound(toolRound(a * p) / 100))
    }
    public static func whatPercent(_ part: String, _ whole: String) throws -> String {
        let p = try toolDecimal(part), w = try toolDecimal(whole)
        guard w != 0 else { throw MathFlowToolError.divisionByZero }
        return toolFormat(toolRound(toolRound(p * 100) / w))
    }
    public static func change(_ amount: String, percent: String, increase: Bool) throws -> String {
        let value = try toolDecimal(amount)
        let rate = toolRound(try toolDecimal(percent) / 100)
        let factor = toolRound(increase ? 1 + rate : 1 - rate)
        return toolFormat(toolRound(value * factor))
    }
}

public enum GeometryTools {
    public static func rectangle(_ aText: String, _ bText: String) throws -> (area: String, perimeter: String) {
        let a = try toolDecimal(aText), b = try toolDecimal(bText)
        guard a > 0, b > 0 else { throw MathFlowToolError.invalidGeometry("Стороны должны быть больше нуля") }
        return (toolFormat(toolRound(a * b)), toolFormat(toolRound(2 * toolRound(a + b))))
    }
    public static func circle(_ radiusText: String) throws -> (area: String, circumference: String) {
        let r = try toolDecimal(radiusText)
        guard r > 0 else { throw MathFlowToolError.invalidGeometry("Радиус должен быть больше нуля") }
        let radius = NSDecimalNumber(decimal: r).doubleValue
        let area = Double.pi * radius * radius
        let circumference = 2 * Double.pi * radius
        guard area.isFinite, circumference.isFinite else { throw MathFlowToolError.invalidNumber(radiusText) }
        return (toolFormat(toolDecimalFromDouble(area)), toolFormat(toolDecimalFromDouble(circumference)))
    }
    public static func triangle(_ aText: String, _ bText: String, _ cText: String) throws -> (perimeter: String, semiperimeter: String, area: String) {
        let a = try toolDecimal(aText)
        let b = try toolDecimal(bText)
        let c = try toolDecimal(cText)
        guard a > 0, b > 0, c > 0, a + b > c, a + c > b, b + c > a else {
            throw MathFlowToolError.invalidGeometry("Такие стороны не образуют треугольник")
        }
        let ad = NSDecimalNumber(decimal: a).doubleValue
        let bd = NSDecimalNumber(decimal: b).doubleValue
        let cd = NSDecimalNumber(decimal: c).doubleValue
        let perimeter = ad + bd + cd
        let semiperimeter = perimeter / 2
        let area = sqrt(semiperimeter * (semiperimeter - ad) * (semiperimeter - bd) * (semiperimeter - cd))
        guard perimeter.isFinite, semiperimeter.isFinite, area.isFinite else {
            throw MathFlowToolError.invalidGeometry("Не удалось вычислить площадь")
        }
        return (toolFormat(toolDecimalFromDouble(perimeter)), toolFormat(toolDecimalFromDouble(semiperimeter)), toolFormat(toolDecimalFromDouble(area)))
    }
}

public struct UnitDefinition: Identifiable, Sendable {
    public let id: String
    public let label: String
}
public struct UnitCategory: Identifiable, Sendable {
    public let id: String
    public let title: String
    public let units: [UnitDefinition]
}

public enum UnitConverter {
    public static let categories: [UnitCategory] = [
        UnitCategory(id:"length",title:"Длина",units:[("mm","mm"),("cm","cm"),("m","m"),("km","km"),("in","in"),("ft","ft"),("yd","yd"),("mi","mi")].map { UnitDefinition(id:$0.0,label:$0.1) }),
        UnitCategory(id:"mass",title:"Масса",units:[("mg","mg"),("g","g"),("kg","kg"),("t","t"),("oz","oz"),("lb","lb")].map { UnitDefinition(id:$0.0,label:$0.1) }),
        UnitCategory(id:"area",title:"Площадь",units:[("mm2","mm²"),("cm2","cm²"),("m2","m²"),("ha","ha"),("km2","km²"),("acre","ac")].map { UnitDefinition(id:$0.0,label:$0.1) }),
        UnitCategory(id:"volume",title:"Объём",units:[("ml","mL"),("l","L"),("m3","m³"),("tsp","tsp"),("cup","cup"),("gal","gal")].map { UnitDefinition(id:$0.0,label:$0.1) }),
        UnitCategory(id:"temperature",title:"Температура",units:[("c","°C"),("f","°F"),("k","K")].map { UnitDefinition(id:$0.0,label:$0.1) }),
        UnitCategory(id:"speed",title:"Скорость",units:[("ms","m/s"),("kmh","km/h"),("mph","mph"),("kn","kn")].map { UnitDefinition(id:$0.0,label:$0.1) }),
        UnitCategory(id:"time",title:"Время",units:[("ms","ms"),("s","s"),("min","min"),("h","h"),("day","d")].map { UnitDefinition(id:$0.0,label:$0.1) }),
        UnitCategory(id:"pressure",title:"Давление",units:[("pa","Pa"),("kpa","kPa"),("mpa","MPa"),("bar","bar"),("atm","atm"),("mmhg","mmHg")].map { UnitDefinition(id:$0.0,label:$0.1) }),
        UnitCategory(id:"energy",title:"Энергия",units:[("j","J"),("kj","kJ"),("cal","cal"),("kcal","kcal"),("wh","Wh"),("kwh","kWh")].map { UnitDefinition(id:$0.0,label:$0.1) }),
        UnitCategory(id:"data",title:"Данные",units:[("b","B"),("kb","kB"),("mb","MB"),("gb","GB"),("tb","TB"),("kib","KiB"),("mib","MiB"),("gib","GiB")].map { UnitDefinition(id:$0.0,label:$0.1) })
    ]

    private static let factors: [String: [String: String]] = [
        "length":["mm":"0.001","cm":"0.01","m":"1","km":"1000","in":"0.0254","ft":"0.3048","yd":"0.9144","mi":"1609.344"],
        "mass":["mg":"0.000001","g":"0.001","kg":"1","t":"1000","oz":"0.028349523125","lb":"0.45359237"],
        "area":["mm2":"0.000001","cm2":"0.0001","m2":"1","ha":"10000","km2":"1000000","acre":"4046.8564224"],
        "volume":["ml":"0.001","l":"1","m3":"1000","tsp":"0.00492892159375","cup":"0.2365882365","gal":"3.785411784"],
        "speed":["ms":"3.6","kmh":"1","mph":"1.609344","kn":"1.852"],
        "time":["ms":"0.001","s":"1","min":"60","h":"3600","day":"86400"],
        "pressure":["pa":"1","kpa":"1000","mpa":"1000000","bar":"100000","atm":"101325","mmhg":"133.322387415"],
        "energy":["j":"1","kj":"1000","cal":"4.184","kcal":"4184","wh":"3600","kwh":"3600000"],
        "data":["b":"1","kb":"1000","mb":"1000000","gb":"1000000000","tb":"1000000000000","kib":"1024","mib":"1048576","gib":"1073741824"]
    ]

    private static let behavior = NSDecimalNumberHandler(roundingMode: .bankers, scale: 38, raiseOnExactness: false, raiseOnOverflow: true, raiseOnUnderflow: false, raiseOnDivideByZero: true)

    private static func rounded34(_ value: Decimal) -> Decimal {
        let magnitude = abs(NSDecimalNumber(decimal: value).doubleValue)
        guard magnitude.isFinite, magnitude > 0 else { return value }
        let scale = max(-128, min(127, 33 - Int(floor(log10(magnitude)))))
        var input = value
        var rounded = value
        NSDecimalRound(&rounded, &input, scale, .bankers)
        return rounded
    }

    private static func decimal(_ source: String) throws -> Decimal {
        guard let value = Decimal(string: source, locale: Locale(identifier:"en_US_POSIX")) else { throw MathFlowToolError.unknownUnit }
        return value
    }
    private static func multiply(_ a: Decimal, _ b: Decimal) -> Decimal {
        rounded34(NSDecimalNumber(decimal:a).multiplying(by:NSDecimalNumber(decimal:b), withBehavior:behavior).decimalValue)
    }
    private static func divide(_ a: Decimal, _ b: Decimal) throws -> Decimal {
        guard b != 0 else { throw MathFlowToolError.divisionByZero }
        return rounded34(NSDecimalNumber(decimal:a).dividing(by:NSDecimalNumber(decimal:b), withBehavior:behavior).decimalValue)
    }
    private static func add(_ a: Decimal, _ b: Decimal) -> Decimal {
        rounded34(NSDecimalNumber(decimal:a).adding(NSDecimalNumber(decimal:b), withBehavior:behavior).decimalValue)
    }
    private static func subtract(_ a: Decimal, _ b: Decimal) -> Decimal {
        rounded34(NSDecimalNumber(decimal:a).subtracting(NSDecimalNumber(decimal:b), withBehavior:behavior).decimalValue)
    }
    private static func formatted(_ value: Decimal) -> String {
        var value = rounded34(value)
        var text = NSDecimalString(&value, Locale(identifier:"en_US_POSIX"))
        if text.contains(".") {
            while text.last == "0" { text.removeLast() }
            if text.last == "." { text.removeLast() }
        }
        return text == "-0" ? "0" : text
    }

    public static func convert(_ source: String, category: String, from: String, to: String) throws -> String {
        let input = try toolDecimal(source)
        let output: Decimal
        if category == "temperature" {
            let celsius: Decimal
            switch from {
            case "c": celsius = input
            case "f": celsius = try divide(multiply(subtract(input, 32), 5), 9)
            case "k": celsius = subtract(input, Decimal(string:"273.15")!)
            default: throw MathFlowToolError.unknownUnit
            }
            guard celsius >= Decimal(string:"-273.15")! else { throw MathFlowToolError.belowAbsoluteZero }
            switch to {
            case "c": output = celsius
            case "f": output = add(try divide(multiply(celsius, 9), 5), 32)
            case "k": output = add(celsius, Decimal(string:"273.15")!)
            default: throw MathFlowToolError.unknownUnit
            }
        } else {
            guard let categoryFactors = factors[category], let fromText = categoryFactors[from],
                  let toText = categoryFactors[to] else { throw MathFlowToolError.unknownUnit }
            let fromFactor = try decimal(fromText), toFactor = try decimal(toText)
            output = try divide(multiply(input, fromFactor), toFactor)
        }
        return formatted(output)
    }
}
