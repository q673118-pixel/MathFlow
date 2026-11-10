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
    guard let value = Decimal(string: normalized, locale: Locale(identifier: "en_US_POSIX")), !value.isNaN else {
        throw MathFlowToolError.invalidNumber(source)
    }
    return value
}

private func toolFormat(_ value: Decimal) -> String {
    var rounded = value
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
        return toolFormat(a * p / 100)
    }
    public static func whatPercent(_ part: String, _ whole: String) throws -> String {
        let p = try toolDecimal(part), w = try toolDecimal(whole)
        guard w != 0 else { throw MathFlowToolError.divisionByZero }
        return toolFormat(p * 100 / w)
    }
    public static func change(_ amount: String, percent: String, increase: Bool) throws -> String {
        let value = try toolDecimal(amount), rate = try toolDecimal(percent) / 100
        return toolFormat(value * (increase ? 1 + rate : 1 - rate))
    }
}

public enum GeometryTools {
    public static func rectangle(_ aText: String, _ bText: String) throws -> (area: String, perimeter: String) {
        let a = try toolDecimal(aText), b = try toolDecimal(bText)
        guard a > 0, b > 0 else { throw MathFlowToolError.invalidGeometry("Стороны должны быть больше нуля") }
        return (toolFormat(a * b), toolFormat(2 * (a + b)))
    }
    public static func circle(_ radiusText: String) throws -> (area: String, circumference: String) {
        let r = try toolDecimal(radiusText)
        guard r > 0 else { throw MathFlowToolError.invalidGeometry("Радиус должен быть больше нуля") }
        let d = NSDecimalNumber(decimal: r).doubleValue
        return (toolFormat(Decimal(Double.pi * d * d)), toolFormat(Decimal(2 * Double.pi * d)))
    }
    public static func triangle(_ aText: String, _ bText: String, _ cText: String) throws -> (perimeter: String, semiperimeter: String, area: String) {
        let a = NSDecimalNumber(decimal: try toolDecimal(aText)).doubleValue
        let b = NSDecimalNumber(decimal: try toolDecimal(bText)).doubleValue
        let c = NSDecimalNumber(decimal: try toolDecimal(cText)).doubleValue
        guard a > 0, b > 0, c > 0, a + b > c, a + c > b, b + c > a else {
            throw MathFlowToolError.invalidGeometry("Такие стороны не образуют треугольник")
        }
        let p = a + b + c, s = p / 2
        return (toolFormat(Decimal(p)), toolFormat(Decimal(s)), toolFormat(Decimal(sqrt(s * (s-a) * (s-b) * (s-c)))))
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
        UnitCategory(id:"length",title:"Длина",units:[("mm","мм"),("cm","см"),("m","м"),("km","км"),("in","дюйм"),("ft","фут"),("yd","ярд"),("mi","миля")].map(UnitDefinition.init)),
        UnitCategory(id:"mass",title:"Масса",units:[("mg","мг"),("g","г"),("kg","кг"),("t","т"),("oz","унция"),("lb","фунт")].map(UnitDefinition.init)),
        UnitCategory(id:"area",title:"Площадь",units:[("mm2","мм²"),("cm2","см²"),("m2","м²"),("ha","га"),("km2","км²"),("ft2","фут²"),("acre","акр")].map(UnitDefinition.init)),
        UnitCategory(id:"volume",title:"Объём",units:[("ml","мл"),("l","л"),("m3","м³"),("tsp","ч. л."),("tbsp","ст. л."),("cup","чашка"),("gal","галлон")].map(UnitDefinition.init)),
        UnitCategory(id:"temperature",title:"Температура",units:[("c","°C"),("f","°F"),("k","K")].map(UnitDefinition.init)),
        UnitCategory(id:"speed",title:"Скорость",units:[("ms","м/с"),("kmh","км/ч"),("mph","миль/ч"),("knot","узел")].map(UnitDefinition.init)),
        UnitCategory(id:"time",title:"Время",units:[("ms","мс"),("s","с"),("min","мин"),("h","ч"),("day","день"),("week","неделя")].map(UnitDefinition.init)),
        UnitCategory(id:"pressure",title:"Давление",units:[("pa","Па"),("kpa","кПа"),("bar","бар"),("atm","атм"),("mmhg","мм рт. ст.")].map(UnitDefinition.init)),
        UnitCategory(id:"energy",title:"Энергия",units:[("j","Дж"),("kj","кДж"),("cal","кал"),("kcal","ккал"),("wh","Вт·ч"),("kwh","кВт·ч")].map(UnitDefinition.init)),
        UnitCategory(id:"data",title:"Данные",units:[("b","Б"),("kb","КБ"),("mb","МБ"),("gb","ГБ"),("tb","ТБ"),("kib","КиБ"),("mib","МиБ"),("gib","ГиБ"),("tib","ТиБ")].map(UnitDefinition.init))
    ]

    private static let factors: [String: [String: Double]] = [
        "length":["mm":0.001,"cm":0.01,"m":1,"km":1000,"in":0.0254,"ft":0.3048,"yd":0.9144,"mi":1609.344],
        "mass":["mg":0.000001,"g":0.001,"kg":1,"t":1000,"oz":0.028349523125,"lb":0.45359237],
        "area":["mm2":0.000001,"cm2":0.0001,"m2":1,"ha":10000,"km2":1000000,"ft2":0.09290304,"acre":4046.8564224],
        "volume":["ml":0.001,"l":1,"m3":1000,"tsp":0.00492892159375,"tbsp":0.01478676478125,"cup":0.2365882365,"gal":3.785411784],
        "speed":["ms":1,"kmh":1/3.6,"mph":0.44704,"knot":0.514444444444],
        "time":["ms":0.001,"s":1,"min":60,"h":3600,"day":86400,"week":604800],
        "pressure":["pa":1,"kpa":1000,"bar":100000,"atm":101325,"mmhg":133.322387415],
        "energy":["j":1,"kj":1000,"cal":4.184,"kcal":4184,"wh":3600,"kwh":3600000],
        "data":["b":1,"kb":1000,"mb":1000000,"gb":1000000000,"tb":1000000000000,"kib":1024,"mib":1048576,"gib":1073741824,"tib":1099511627776]
    ]

    public static func convert(_ value: String, category: String, from: String, to: String) throws -> String {
        let input = NSDecimalNumber(decimal: try toolDecimal(value)).doubleValue
        let output: Double
        if category == "temperature" {
            let celsius: Double
            switch from { case "c": celsius = input; case "f": celsius = (input - 32) * 5 / 9; case "k": celsius = input - 273.15; default: throw MathFlowToolError.unknownUnit }
            guard celsius >= -273.15 else { throw MathFlowToolError.belowAbsoluteZero }
            switch to { case "c": output = celsius; case "f": output = celsius * 9 / 5 + 32; case "k": output = celsius + 273.15; default: throw MathFlowToolError.unknownUnit }
        } else {
            guard let categoryFactors = factors[category], let a = categoryFactors[from], let b = categoryFactors[to] else { throw MathFlowToolError.unknownUnit }
            output = input * a / b
        }
        guard output.isFinite else { throw MathFlowToolError.invalidNumber(value) }
        return String(format: "%.15g", output)
    }
}
