import SwiftUI
import MathFlowCore
import UniformTypeIdentifiers
import CoreText

@main
struct MathFlowApp: App {
    @StateObject private var settings = AppSettings()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(settings)
                .preferredColorScheme(settings.palette.isDark ? .dark : .light)
        }
    }
}

final class AppSettings: ObservableObject {
    @Published var themeID: String { didSet { UserDefaults.standard.set(themeID, forKey: "mathflow.theme") } }
    @Published var languageID: String { didSet { UserDefaults.standard.set(languageID, forKey: "mathflow.language") } }
    @Published var textScale: Double { didSet { UserDefaults.standard.set(textScale, forKey: "mathflow.textScale") } }
    @Published var keyStyle: String { didSet { UserDefaults.standard.set(keyStyle, forKey: "mathflow.keyStyle") } }
    @Published var keyDuration: Double { didSet { UserDefaults.standard.set(keyDuration, forKey: "mathflow.keyDuration") } }
    @Published var keyOpacity: Double { didSet { UserDefaults.standard.set(keyOpacity, forKey: "mathflow.keyOpacity") } }
    @Published var keyIntensity: Double { didSet { UserDefaults.standard.set(keyIntensity, forKey: "mathflow.keyIntensity") } }
    @Published var keyColorEffect: Bool { didSet { UserDefaults.standard.set(keyColorEffect, forKey: "mathflow.keyColorEffect") } }
    @Published var keyEffectColorID: String { didSet { UserDefaults.standard.set(keyEffectColorID, forKey: "mathflow.keyEffectColor") } }
    @Published var navEnabled: Bool { didSet { UserDefaults.standard.set(navEnabled, forKey: "mathflow.navEnabled") } }
    @Published var navStyle: String { didSet { UserDefaults.standard.set(navStyle, forKey: "mathflow.navStyle") } }
    @Published var navDuration: Double { didSet { UserDefaults.standard.set(navDuration, forKey: "mathflow.navDuration") } }
    @Published var navIntensity: Double { didSet { UserDefaults.standard.set(navIntensity, forKey: "mathflow.navIntensity") } }
    @Published var navIconScale: Double { didSet { UserDefaults.standard.set(navIconScale, forKey: "mathflow.navIconScale") } }
    @Published var navOpacity: Double { didSet { UserDefaults.standard.set(navOpacity, forKey: "mathflow.navOpacity") } }
    @Published var iconID: String { didSet { UserDefaults.standard.set(iconID, forKey: "mathflow.icon") } }
    @Published var fontPath: String? { didSet { UserDefaults.standard.set(fontPath, forKey: "mathflow.fontPath") } }
    @Published var fontPostScriptName: String? { didSet { UserDefaults.standard.set(fontPostScriptName, forKey: "mathflow.fontName") } }

    init() {
        let d = UserDefaults.standard
        themeID = d.string(forKey: "mathflow.theme") ?? "vanilla"
        languageID = d.string(forKey: "mathflow.language") ?? "ru"
        textScale = d.object(forKey: "mathflow.textScale") as? Double ?? 1
        keyStyle = d.string(forKey: "mathflow.keyStyle") ?? "spring"
        keyDuration = d.object(forKey: "mathflow.keyDuration") as? Double ?? 140
        keyOpacity = d.object(forKey: "mathflow.keyOpacity") as? Double ?? 0.72
        keyIntensity = d.object(forKey: "mathflow.keyIntensity") as? Double ?? 0.10
        keyColorEffect = d.object(forKey: "mathflow.keyColorEffect") as? Bool ?? false
        keyEffectColorID = d.string(forKey: "mathflow.keyEffectColor") ?? "accent"
        navEnabled = d.object(forKey: "mathflow.navEnabled") as? Bool ?? true
        navStyle = d.string(forKey: "mathflow.navStyle") ?? "fluid_pill"
        navDuration = d.object(forKey: "mathflow.navDuration") as? Double ?? 280
        navIntensity = d.object(forKey: "mathflow.navIntensity") as? Double ?? 0.65
        navIconScale = d.object(forKey: "mathflow.navIconScale") as? Double ?? 0.08
        navOpacity = d.object(forKey: "mathflow.navOpacity") as? Double ?? 0.85
        iconID = d.string(forKey: "mathflow.icon") ?? "vanilla"
        fontPath = d.string(forKey: "mathflow.fontPath")
        fontPostScriptName = d.string(forKey: "mathflow.fontName")
        if let path = fontPath { _ = CTFontManagerRegisterFontsForURL(URL(fileURLWithPath: path) as CFURL, .process, nil) }
    }
    var palette: MathFlowPalette { MathFlowPalette.named(themeID) }
    var effectColor: Color {
        switch keyEffectColorID { case "purple": return .purple; case "cyan": return .cyan; case "green": return .green; case "pink": return .pink; case "orange": return .orange; default: return palette.accent }
    }
    func text(_ key: String) -> String { MathFlowLocalization.text(key, language: languageID) }
}

struct RootView: View {
    @EnvironmentObject private var settings: AppSettings
    @State private var selection = 0
    @State private var showSplash = true
    @State private var showWelcome = false
    @State private var splashScale: CGFloat = 0.35
    @State private var splashAlpha = 0.0
    @State private var dotOffset: CGFloat = -450
    @State private var dotAlpha = 0.0
    @State private var dotScaleX: CGFloat = 1
    @State private var dotScaleY: CGFloat = 1

    var body: some View {
        VStack(spacing: 0) {
            Group {
                switch selection {
                case 0: CalculatorView()
                case 1: ToolsView()
                case 2: SolverView()
                case 3: HistoryView()
                default: SettingsView()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            BottomNavigation(selection: $selection)
        }
        .background(settings.palette.background.ignoresSafeArea())
        .environment(\.layoutDirection, settings.languageID == "ar" ? .rightToLeft : .leftToRight)
        .environment(\.dynamicTypeSize, settings.textScale < 0.95 ? .small : settings.textScale > 1.3 ? .xxxLarge : settings.textScale > 1.15 ? .xxLarge : settings.textScale > 1.05 ? .xLarge : .large)
        .font(settings.fontPostScriptName.map { .custom($0, size: 17) } ?? .system(size: 17))
        .animation(.easeInOut(duration: 0.2), value: selection)
        .overlay {
            if showSplash {
                ZStack {
                    Color.black.ignoresSafeArea()
                    ZStack {
                        MathFlowMark()
                            .fill(settings.palette.accent)
                            .frame(width: 108, height: 108)
                            .scaleEffect(splashScale)
                            .opacity(splashAlpha)
                        MathFlowDot()
                            .fill(settings.palette.keyEquals)
                            .frame(width: 108, height: 108)
                            .scaleEffect(x: dotScaleX, y: dotScaleY, anchor: UnitPoint(x: 0.5, y: 0.308))
                            .offset(y: dotOffset)
                            .opacity(dotAlpha)
                    }
                }.transition(.opacity)
            }
        }
        .alert("Добро пожаловать в MathFlow", isPresented: $showWelcome) {
            Button("Начать") { UserDefaults.standard.set(true, forKey: "mathflow.welcomeSeen") }
        } message: {
            Text("Научный калькулятор и математические инструменты всегда под рукой.")
        }
        .onAppear(perform: runIntro)
    }

    private func runIntro() {
        guard showSplash else { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            withAnimation(.spring(response: 0.48, dampingFraction: 0.68)) {
                splashScale = 1.08; splashAlpha = 1
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.42) {
                withAnimation(.spring(response: 0.24, dampingFraction: 0.78)) { splashScale = 1 }
                dotAlpha = 1
                withAnimation(.spring(response: 0.52, dampingFraction: 0.48)) { dotOffset = 0 }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.55) {
                    withAnimation(.spring(response: 0.28, dampingFraction: 0.58)) { dotOffset = -13 }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.28) {
                        withAnimation(.spring(response: 0.24, dampingFraction: 0.68)) { dotOffset = 0 }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.28) {
                            withAnimation(.easeOut(duration: 0.35)) { dotAlpha = 0 }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                                withAnimation(.easeOut(duration: 1.5)) { splashAlpha = 0; splashScale = 0.96 }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                                    showSplash = false
                                    if !UserDefaults.standard.bool(forKey: "mathflow.welcomeSeen") { showWelcome = true }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private struct BottomNavigation: View {
    @EnvironmentObject private var settings: AppSettings
    @Binding var selection: Int
    private let items: [(String,String)] = [("function","home"),("square.grid.2x2","tools"),("equal","solver"),("clock.arrow.circlepath","history"),("slider.horizontal.3","settings")]

    var body: some View {
        let p = settings.palette
        HStack(spacing: 3) {
            ForEach(items.indices, id: \.self) { index in
                let selected = selection == index
                Button {
                    withAnimation(animation) { selection = index }
                } label: {
                    VStack(spacing: 5) {
                        Image(systemName: items[index].0)
                            .font(.system(size: selected ? 20 : 18, weight: selected ? .semibold : .regular))
                            .scaleEffect(settings.navEnabled && selected ? 1 + settings.navIconScale : 1)
                        Text(settings.text(items[index].1))
                            .font(.system(size: 10, weight: selected ? .semibold : .medium))
                            .lineLimit(1).minimumScaleFactor(0.75)
                    }
                    .foregroundStyle(selected ? p.accent : p.muted)
                    .frame(maxWidth: .infinity).frame(height: 58)
                    .background {
                        if selected && settings.navEnabled && ["fluid_pill","morph","ripple_bloom","expand"].contains(settings.navStyle) {
                            RoundedRectangle(cornerRadius: settings.navStyle == "morph" ? 15 : 22, style: .continuous)
                                .fill(p.navActive.opacity(settings.navOpacity))
                                .matchedGeometryEffect(id: "mathflow.nav.indicator", in: namespace)
                        }
                    }
                    .opacity(settings.navEnabled && settings.navStyle == "soft_focus" && !selected ? 0.58 : 1)
                    .scaleEffect(settings.navEnabled && selected && settings.navStyle == "expand" ? 1.04 : 1)
                    .overlay(alignment: .bottom) {
                        if selected && settings.navStyle == "light_trail" {
                            Capsule().fill(p.accent).frame(width: 24, height: 3).padding(.bottom, 2)
                        }
                    }
                }
                .buttonStyle(MathFlowPressStyle())
                .accessibilityLabel(settings.text(items[index].1))
            }
        }
        .padding(.horizontal, 8).padding(.top, 5).padding(.bottom, 2)
        .background(p.surface)
        .overlay(alignment: .top) { Rectangle().fill(p.muted.opacity(0.12)).frame(height: 0.5) }
    }

    @Namespace private var namespace
    private var animation: Animation {
        guard settings.navEnabled else { return .linear(duration: 0) }
        let duration = settings.navDuration / 1000
        switch settings.navStyle {
        case "spring_motion": return .spring(response: duration, dampingFraction: 0.72)
        case "elastic_slide": return .spring(response: duration, dampingFraction: 0.48)
        case "ripple_bloom": return .spring(response: duration, dampingFraction: 0.68)
        default: return .easeInOut(duration: duration)
        }
    }
}

struct CalculatorView: View {
    @EnvironmentObject private var settings: AppSettings
    @State private var expression = ""
    @State private var result = "0"
    @State private var error: String?
    @State private var steps: [String] = []
    @State private var showSteps = true
    @State private var scientific = false
    @State private var degrees = true
    @State private var didEvaluate = false

    private let basicRows = [
        ["(", ")", "⌫", "÷"],
        ["7", "8", "9", "×"],
        ["4", "5", "6", "−"],
        ["1", "2", "3", "+"],
        ["%", "0", ".", "="]
    ]
    private let scientificRows = [
        ["sin", "cos", "tan", "ln"],
        ["log", "abs", "e", "AC"],
        ["√", "x²", "xʸ", "π"]
    ]

    var body: some View {
        let p = settings.palette
        VStack(spacing: 10) {
            VStack(alignment: .trailing, spacing: 10) {
                HStack(spacing: 8) {
                    Button("f(x)") { withAnimation(.easeInOut(duration: 0.18)) { scientific.toggle() } }
                        .foregroundStyle(scientific ? p.accent : p.muted)
                    if scientific {
                        Button(degrees ? "DEG" : "RAD") { degrees.toggle() }.foregroundStyle(p.accent)
                    }
                    if !steps.isEmpty {
                        Button("Решение") { withAnimation(.easeInOut(duration: 0.2)) { showSteps.toggle() } }
                            .foregroundStyle(showSteps ? p.accent : p.muted)
                    }
                    Spacer()
                    Button("⌫") { expression = String(expression.dropLast()) }.foregroundStyle(p.muted)
                }
                Text(expression.isEmpty ? " " : expression.replacingOccurrences(of: "sqrt", with: "√").replacingOccurrences(of: "pi", with: "π"))
                    .font(.system(size: 23, weight: .regular, design: .rounded))
                    .foregroundStyle(p.muted)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .trailing)
                Text(result)
                    .font(.system(size: result.count > 16 ? 25 : result.count > 11 ? 32 : 48, weight: .medium, design: .rounded))
                    .foregroundStyle(error == nil ? p.text : p.error)
                    .lineLimit(1)
                    .minimumScaleFactor(0.5)
                    .contentTransition(.numericText())
                if let error { Text(error).font(.caption).foregroundStyle(p.error) }
            }
            .padding(18)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)
            .background(p.surface, in: RoundedRectangle(cornerRadius: 27, style: .continuous))

            if !steps.isEmpty && showSteps {
                ScrollView {
                    VStack(alignment: .leading, spacing: 4) {
                        ForEach(Array(steps.enumerated()), id: \.offset) { index, step in
                            Text("\(index + 1). \(step)").font(.caption).foregroundStyle(p.muted)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                    .padding(12)
                }
                .frame(maxHeight: 94)
                .background(p.surface, in: RoundedRectangle(cornerRadius: 18))
                .transition(.opacity.combined(with: .move(edge: .top)))
            }

            VStack(spacing: 8) {
                ForEach(Array((scientific ? scientificRows + basicRows : basicRows).enumerated()), id: \.offset) { _, row in
                    HStack(spacing: 8) {
                        ForEach(row, id: \.self) { key in
                            Button { press(key) } label: {
                                Text(key)
                                    .font(.system(size: 18, weight: .medium, design: .rounded))
                                    .frame(maxWidth: .infinity, minHeight: 45)
                                    .background(key == "=" ? p.keyEquals : (key == "AC" || ["÷", "×", "−", "+"].contains(key)) ? p.keyAccent : p.key,
                                                in: RoundedRectangle(cornerRadius: 15, style: .continuous))
                                    .foregroundStyle(key == "=" ? p.onKeyEquals : p.text)
                            }
                            .buttonStyle(MathFlowPressStyle())
                        }
                    }
                }
            }
        }
        .padding(.horizontal, 12)
        .padding(.top, 8)
        .background(p.background)
    }

    private func press(_ key: String) {
        let operators = ["+", "−", "×", "÷", "^"]
        if key == "AC" {
            expression = ""; result = "0"; error = nil; steps = []
            return
        }
        if key == "⌫" {
            if didEvaluate { didEvaluate = false }
            expression = String(expression.dropLast())
            error = nil
            return
        }
        if key == "=" {
            do {
                var normalized = expression
                let opens = normalized.filter { $0 == "(" }.count
                let closes = normalized.filter { $0 == ")" }.count
                if opens > closes { normalized += String(repeating: ")", count: opens - closes) }
                let mode: AngleMode = degrees ? .degrees : .radians
                let calculation = try ExpressionEngine(angleMode: mode).calculate(normalized)
                result = calculation.displayValue
                steps = calculation.steps
                error = nil
                didEvaluate = true
                HistoryStore.shared.add(expression: normalized, result: result, steps: steps)
            } catch {
                result = "Ошибка"
                self.error = error.localizedDescription
                steps = []
                didEvaluate = false
            }
            return
        }
        if didEvaluate {
            if operators.contains(key) { expression = result }
            else { expression = "" }
            didEvaluate = false
        }
        switch key {
        case "√": expression += "sqrt("
        case "x²": expression += "^2"
        case "xʸ": expression += "^"
        case "π": expression += "pi"
        case "sin", "cos", "tan", "ln", "log", "abs": expression += key + "("
        default: expression += key
        }
        error = nil
    }
}

struct ToolsView: View {
    @EnvironmentObject private var settings: AppSettings
    @State private var selected = "fractions"
    @State private var first = ""
    @State private var second = ""
    @State private var third = ""
    @State private var operation: FractionOperation = .add
    @State private var percentMode = 0
    @State private var increase = true
    @State private var geometryMode = 0
    @State private var result = ""
    @State private var error: String?
    @State private var converterValue = "1"
    @State private var categoryID = "length"
    @State private var fromID = "km"
    @State private var toID = "m"
    @State private var graphExpression = "x^2"
    @State private var graphMin = "-5"
    @State private var graphMax = "5"
    @State private var graphPoints: [CGPoint] = []

    private let options = ["fractions", "percentages", "geometry", "converter", "graph"]
    private var category: UnitCategory { UnitConverter.categories.first(where: { $0.id == categoryID }) ?? UnitConverter.categories[0] }
    private var fromUnit: UnitDefinition { category.units.first(where: { $0.id == fromID }) ?? category.units[0] }
    private var toUnit: UnitDefinition { category.units.first(where: { $0.id == toID }) ?? category.units[min(1, category.units.count - 1)] }

    var body: some View {
        let p = settings.palette
        VStack(alignment: .leading, spacing: 14) {
            Text(settings.text("tools")).font(.largeTitle.bold()).foregroundStyle(p.text)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(options, id: \.self) { optionID in
                        Button(settings.text(optionID)) {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                selected = optionID; result = ""; error = nil
                            }
                        }
                        .font(.subheadline.weight(.medium))
                        .padding(.horizontal, 13).padding(.vertical, 9)
                        .background(selected == optionID ? p.keyEquals : p.key, in: Capsule())
                        .foregroundStyle(selected == optionID ? p.onKeyEquals : p.text)
                    }
                }
            }
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    switch selected {
                    case "fractions": fractionContent(p)
                    case "percentages": percentContent(p)
                    case "geometry": geometryContent(p)
                    case "converter": converterContent(p)
                    default: graphContent(p)
                    }
                    if !result.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Результат").font(.headline).foregroundStyle(p.text)
                            Text(result).font(.system(.body, design: .rounded).weight(.medium)).foregroundStyle(p.text).textSelection(.enabled)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(p.surface, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
                        .transition(.opacity.combined(with: .move(edge: .top)))
                    }
                    if let error {
                        Text(error).font(.caption).foregroundStyle(p.error).padding(.horizontal, 4)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .scrollIndicators(.hidden)
        }
        .padding(16)
        .background(p.background)
        .onChange(of: categoryID) { newValue in
            if let next = UnitConverter.categories.first(where: { $0.id == newValue }) {
                fromID = next.units.first?.id ?? ""
                toID = next.units.dropFirst().first?.id ?? fromID
                result = ""
            }
        }
    }

    private func field(_ title: String, text: Binding<String>, palette p: MathFlowPalette, decimal: Bool = true) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.caption.weight(.medium)).foregroundStyle(p.muted)
            TextField("0", text: text)
                .keyboardType(decimal ? .decimalPad : .numbersAndPunctuation)
                .font(.system(size: 19, weight: .medium, design: .rounded))
                .padding(14)
                .background(p.key, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
                .foregroundStyle(p.text)
        }
    }

    private func action(_ title: String, palette p: MathFlowPalette, run: @escaping () throws -> String) -> some View {
        Button {
            do { let value = try run(); withAnimation(.easeInOut(duration: 0.2)) { result = value; error = nil } }
            catch { result = ""; self.error = error.localizedDescription }
        } label: {
            Text(title).font(.system(size: 16, weight: .semibold))
                .frame(maxWidth: .infinity).padding(.vertical, 15)
                .background(p.keyEquals, in: RoundedRectangle(cornerRadius: 15, style: .continuous))
                .foregroundStyle(p.onKeyEquals)
        }.buttonStyle(MathFlowPressStyle())
    }

    @ViewBuilder private func fractionContent(_ p: MathFlowPalette) -> some View {
        Text(settings.text("fractions")).font(.title2.bold()).foregroundStyle(p.text)
        field(settings.text("fraction_one"), text: $first, palette: p)
        HStack(spacing: 8) {
            ForEach(FractionOperation.allCases, id: \.self) { item in
                Button(item.rawValue) { operation = item }
                    .font(.title2.weight(.semibold)).frame(maxWidth: .infinity).padding(.vertical, 11)
                    .background(operation == item ? p.keyEquals : p.key, in: RoundedRectangle(cornerRadius: 12))
                    .foregroundStyle(operation == item ? p.onKeyEquals : p.text)
            }
        }
        field(settings.text("fraction_two"), text: $second, palette: p)
        action("Вычислить", palette: p) {
            let value = try FractionTools.calculate(first, second, operation: operation)
            return "Точный ответ: \(value.mixed)\nДесятичный вид: \(value.decimal)\nСокращённая дробь: \(value.exact)"
        }
    }

    @ViewBuilder private func percentContent(_ p: MathFlowPalette) -> some View {
        Text(settings.text("percentages")).font(.title2.bold()).foregroundStyle(p.text)
        Picker("Операция", selection: $percentMode) {
            Text(settings.text("percent_of")).tag(0); Text(settings.text("what_percent")).tag(1); Text(settings.text("change_percent")).tag(2)
        }.pickerStyle(.segmented)
        field(percentMode == 1 ? "Часть" : "Процент", text: $first, palette: p)
        field(percentMode == 1 ? "Целое" : "Число", text: $second, palette: p)
        if percentMode == 2 {
            Picker("Изменение", selection: $increase) {
                Text("Увеличить").tag(true); Text("Уменьшить").tag(false)
            }.pickerStyle(.segmented)
        }
        action("Рассчитать", palette: p) {
            switch percentMode {
            case 0: return "\(first)% от \(second) = \(try PercentTools.percentOf(first, second))"
            case 1: return "\(first) составляет \(try PercentTools.whatPercent(first, second))% от \(second)"
            default: return (increase ? "После увеличения" : "После уменьшения") + ": " + (try PercentTools.change(second, percent: first, increase: increase))
            }
        }
    }

    @ViewBuilder private func geometryContent(_ p: MathFlowPalette) -> some View {
        Text(settings.text("geometry")).font(.title2.bold()).foregroundStyle(p.text)
        Picker("Фигура", selection: $geometryMode) {
            Text(settings.text("rectangle")).tag(0); Text(settings.text("circle")).tag(1); Text(settings.text("triangle")).tag(2)
        }.pickerStyle(.segmented)
        if geometryMode == 0 {
            field("Сторона A", text: $first, palette: p); field("Сторона B", text: $second, palette: p)
        } else if geometryMode == 1 {
            field("Радиус", text: $first, palette: p)
        } else {
            field("Сторона A", text: $first, palette: p); field("Сторона B", text: $second, palette: p); field("Сторона C", text: $third, palette: p)
        }
        action("Вычислить", palette: p) {
            if geometryMode == 0 {
                let r = try GeometryTools.rectangle(first, second)
                return "Площадь: \(r.area)\nПериметр: \(r.perimeter)"
            }
            if geometryMode == 1 {
                let r = try GeometryTools.circle(first)
                return "Площадь: \(r.area)\nДлина окружности: \(r.circumference)"
            }
            let r = try GeometryTools.triangle(first, second, third)
            return "Периметр: \(r.perimeter)\nПолупериметр: \(r.semiperimeter)\nПлощадь (формула Герона): \(r.area)"
        }
    }

    @ViewBuilder private func converterContent(_ p: MathFlowPalette) -> some View {
        Text(settings.text("converter_title")).font(.title2.bold()).foregroundStyle(p.text)
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(UnitConverter.categories) { item in
                    Button(settings.text(item.id)) { categoryID = item.id }
                        .font(.caption.weight(.semibold)).padding(.horizontal, 12).padding(.vertical, 9)
                        .background(categoryID == item.id ? p.keyEquals : p.key, in: Capsule())
                        .foregroundStyle(categoryID == item.id ? p.onKeyEquals : p.text)
                }
            }
        }
        field("Значение", text: $converterValue, palette: p)
        HStack(spacing: 8) {
            unitPicker("Из", selection: $fromID, p: p)
            Button { swap(&fromID, &toID) } label: {
                Image(systemName: "arrow.left.arrow.right").font(.title3).foregroundStyle(p.accent)
                    .frame(width: 42, height: 48).background(p.key, in: Circle())
            }
            unitPicker("В", selection: $toID, p: p)
        }
        action("Конвертировать", palette: p) {
            let value = try UnitConverter.convert(converterValue, category: categoryID, from: fromID, to: toID)
            return "\(converterValue) \(fromUnit.label) = \(value) \(toUnit.label)"
        }
    }

    private func unitPicker(_ title: String, selection: Binding<String>, p: MathFlowPalette) -> some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(title).font(.caption).foregroundStyle(p.muted)
            Menu {
                ForEach(category.units) { unit in
                    Button(unit.label) { selection.wrappedValue = unit.id }
                }
            } label: {
                HStack { Text(category.units.first(where: { $0.id == selection.wrappedValue })?.label ?? "—"); Spacer(); Image(systemName: "chevron.down") }
                    .font(.subheadline.weight(.medium)).foregroundStyle(p.text).padding(13)
                    .background(p.key, in: RoundedRectangle(cornerRadius: 13))
            }
        }.frame(maxWidth: .infinity)
    }

    @ViewBuilder private func graphContent(_ p: MathFlowPalette) -> some View {
        Text("График функции").font(.title2.bold()).foregroundStyle(p.text)
        field("Функция y =", text: $graphExpression, palette: p, decimal: false)
        HStack(spacing: 10) {
            field("X от", text: $graphMin, palette: p)
            field("X до", text: $graphMax, palette: p)
        }
        action("Построить график", palette: p) {
            guard let lower = Double(graphMin.replacingOccurrences(of: ",", with: ".")),
                  let upper = Double(graphMax.replacingOccurrences(of: ",", with: ".")),
                  lower.isFinite, upper.isFinite, lower < upper else {
                throw MathFlowToolError.invalidNumber("Некорректный диапазон X")
            }
            let engine = ExpressionEngine()
            var points: [CGPoint] = []
            for index in 0...240 {
                let x = lower + (upper - lower) * Double(index) / 240
                if let value = try? engine.calculate(graphExpression, variables: ["x": x], includeSteps: false),
                   let y = Double(value.decimalValue), y.isFinite, abs(y) < 1.0e9 {
                    points.append(CGPoint(x: x, y: y))
                } else {
                    points.append(CGPoint(x: x, y: .nan))
                }
            }
            graphPoints = points
            return "Построено точек: \(points.filter { $0.y.isFinite }.count) из 241"
        }
        if !graphPoints.isEmpty {
            graphPlot(p)
                .frame(height: 250)
                .padding(10)
                .background(p.surface, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
        }
        Text("Поддерживаются выражения калькулятора и переменная x.").font(.caption).foregroundStyle(p.muted)
    }

    private func graphPlot(_ p: MathFlowPalette) -> some View {
        GeometryReader { geometry in
            let valid = graphPoints.filter { $0.x.isFinite && $0.y.isFinite }
            let minX = graphPoints.first?.x ?? -5
            let maxX = graphPoints.last?.x ?? 5
            let minY = max(-100.0, valid.map(\.y).min() ?? -1)
            let maxY = min(100.0, valid.map(\.y).max() ?? 1)
            let spanY = max(0.000001, maxY - minY)
            ZStack {
                Path { path in
                    for step in 0...4 {
                        let y = geometry.size.height * CGFloat(step) / 4
                        path.move(to: CGPoint(x: 0, y: y))
                        path.addLine(to: CGPoint(x: geometry.size.width, y: y))
                    }
                    if minX <= 0 && maxX >= 0 {
                        let x = geometry.size.width * CGFloat((0 - minX) / (maxX - minX))
                        path.move(to: CGPoint(x: x, y: 0)); path.addLine(to: CGPoint(x: x, y: geometry.size.height))
                    }
                    if minY <= 0 && maxY >= 0 {
                        let y = geometry.size.height * CGFloat(maxY / spanY)
                        path.move(to: CGPoint(x: 0, y: y)); path.addLine(to: CGPoint(x: geometry.size.width, y: y))
                    }
                }.stroke(p.muted.opacity(0.25), lineWidth: 1)
                Path { path in
                    var active = false
                    for point in graphPoints {
                        guard point.y.isFinite else { active = false; continue }
                        let x = CGFloat((point.x - minX) / max(0.000001, maxX - minX)) * geometry.size.width
                        let clippedY = min(max(point.y, minY), maxY)
                        let y = CGFloat((maxY - clippedY) / spanY) * geometry.size.height
                        if active { path.addLine(to: CGPoint(x: x, y: y)) }
                        else { path.move(to: CGPoint(x: x, y: y)); active = true }
                    }
                }.stroke(p.accent, style: StrokeStyle(lineWidth: 2.5, lineCap: .round, lineJoin: .round))
            }
        }
    }
}

struct SolverView: View {
    @EnvironmentObject private var settings: AppSettings
    @State private var equation = ""
    @State private var answer = ""
    @State private var steps: [String] = []
    @State private var error: String?

    var body: some View {
        let p = settings.palette
        VStack(alignment: .leading, spacing: 16) {
            Text(settings.text("solver")).font(.largeTitle.bold()).foregroundStyle(p.text)
            Text(settings.text("equation_help")).font(.subheadline).foregroundStyle(p.muted)
            TextField(settings.text("example_equation"), text: $equation)
                .textFieldStyle(.plain)
                .font(.system(size: 18, weight: .medium, design: .rounded))
                .padding(15).background(p.key, in: RoundedRectangle(cornerRadius: 15))
                .foregroundStyle(p.text)
                .keyboardType(.numbersAndPunctuation)
            Button {
                do {
                    let solution = try AlgebraSolver.solve(equation)
                    withAnimation(.easeInOut(duration: 0.2)) {
                        answer = solution.answer; steps = solution.steps; error = nil
                    }
                } catch {
                    answer = ""; steps = []; self.error = error.localizedDescription
                }
            } label: {
                Text(settings.text("solve")).font(.system(size: 16, weight: .semibold))
                    .frame(maxWidth: .infinity).padding(.vertical, 15)
                    .background(p.keyEquals, in: RoundedRectangle(cornerRadius: 15))
                    .foregroundStyle(p.onKeyEquals)
            }.buttonStyle(MathFlowPressStyle())
            if !answer.isEmpty {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Ответ").font(.caption.weight(.semibold)).foregroundStyle(p.muted)
                    Text(answer).font(.system(size: 23, weight: .semibold, design: .rounded)).foregroundStyle(p.text)
                }.frame(maxWidth: .infinity, alignment: .leading).padding(16)
                    .background(p.surface, in: RoundedRectangle(cornerRadius: 18))
                VStack(alignment: .leading, spacing: 9) {
                    Text(settings.text("steps")).font(.headline).foregroundStyle(p.text)
                    ForEach(Array(steps.enumerated()), id: \.offset) { index, step in
                        HStack(alignment: .top, spacing: 9) {
                            Text("\(index + 1).").foregroundStyle(p.accent).fontWeight(.semibold)
                            Text(step).foregroundStyle(p.muted)
                        }.font(.subheadline)
                    }
                }.padding(16).frame(maxWidth: .infinity, alignment: .leading)
                    .background(p.surface, in: RoundedRectangle(cornerRadius: 18))
            }
            if let error { Text(error).font(.caption).foregroundStyle(p.error) }
            Spacer(minLength: 0)
        }
        .padding(16)
        .background(p.background)
    }
}

struct HistoryView: View {
    @EnvironmentObject private var settings: AppSettings
    @ObservedObject private var history = HistoryStore.shared
    var body: some View {
        let p = settings.palette
        NavigationStack {
            List {
                if history.entries.isEmpty {
                    Text(settings.text("history_empty")).foregroundStyle(p.muted)
                }
                ForEach(history.entries) { entry in
                    VStack(alignment: .leading, spacing: 5) {
                        Text(entry.expression).foregroundStyle(p.muted)
                        Text(entry.createdAt.formatted(date: .abbreviated, time: .shortened)).font(.caption2).foregroundStyle(p.muted)
                        Text(entry.result).font(.title2.weight(.semibold)).foregroundStyle(p.text)
                        ForEach(Array(entry.steps.enumerated()), id: \.offset) { _, step in
                            Text(step).font(.caption).foregroundStyle(p.muted)
                        }
                    }
                    .listRowBackground(p.surface)
                }
            }
            .scrollContentBackground(.hidden)
            .background(p.background)
            .navigationTitle(settings.text("history"))
            .toolbar { Button(settings.text("clear_history")) { history.clear() } }
        }
    }
}

struct SettingsView: View {
    @EnvironmentObject private var settings: AppSettings
    @State private var theme = "vanilla"
    @State private var language = "ru"
    @State private var scale = 1.0
    @State private var keyStyle = "spring"
    @State private var keyDuration = 140.0
    @State private var keyOpacity = 0.72
    @State private var keyIntensity = 0.10
    @State private var keyColorEffect = false
    @State private var keyEffectColorID = "accent"
    @State private var navEnabled = true
    @State private var navStyle = "fluid_pill"
    @State private var navDuration = 280.0
    @State private var navIntensity = 0.65
    @State private var navIconScale = 0.08
    @State private var navOpacity = 0.85
    @State private var iconID = "vanilla"
    @State private var fontPath: String?
    @State private var fontName: String?
    @State private var showFontPicker = false
    @State private var fontMessage: String?
    @State private var initialized = false

    private let themes = ["vanilla", "black", "white", "purple", "ocean", "emerald", "sunset", "rose", "midnight", "graphite"]
    private let keyStyles = [("spring","Spring"),("press","Press"),("scale","Scale"),("flash","Flash"),("ripple","Ripple"),("bounce","Bounce"),("elastic","Elastic"),("minimal","Minimal")]
    private let navStyles = [("fluid_pill","Fluid Pill"),("spring_motion","Spring Motion"),("light_trail","Light Trail"),("morph","Morph"),("expand","Expand"),("soft_focus","Soft Focus"),("elastic_slide","Elastic Slide"),("ripple_bloom","Ripple Bloom")]

    private var hasChanges: Bool {
        theme != settings.themeID || language != settings.languageID || abs(scale-settings.textScale)>0.001 ||
        keyStyle != settings.keyStyle || keyDuration != settings.keyDuration || keyOpacity != settings.keyOpacity ||
        keyIntensity != settings.keyIntensity || keyColorEffect != settings.keyColorEffect || keyEffectColorID != settings.keyEffectColorID ||
        navEnabled != settings.navEnabled || navStyle != settings.navStyle || navDuration != settings.navDuration ||
        navIntensity != settings.navIntensity || navIconScale != settings.navIconScale || navOpacity != settings.navOpacity ||
        iconID != settings.iconID || fontPath != settings.fontPath
    }

    var body: some View {
        let p = MathFlowPalette.named(theme)
        NavigationStack {
            List {
                Section(settings.text("themes")) {
                    ForEach(themes, id: \.self) { id in
                        Button { theme = id } label: {
                            HStack(spacing: 12) {
                                Circle().fill(MathFlowPalette.named(id).keyEquals).frame(width: 25, height: 25)
                                Text(id.capitalized).foregroundStyle(p.text)
                                Spacer()
                                if theme == id { Image(systemName: "checkmark.circle.fill").foregroundStyle(p.accent) }
                            }.padding(.vertical, 3)
                        }.listRowBackground(p.surface)
                    }
                    Text(settings.text("theme_hint")).font(.caption).foregroundStyle(p.muted).listRowBackground(p.surface)
                }
                Section(settings.text("app_language")) {
                    ForEach(MathFlowLanguage.all) { item in
                        Button { language = item.id } label: {
                            HStack { Text(item.nativeName).foregroundStyle(p.text); Spacer(); if language == item.id { Image(systemName:"checkmark").foregroundStyle(p.accent) } }
                        }.listRowBackground(p.surface)
                    }
                }
                Section(settings.text("text_size")) {
                    HStack { Text("\(Int(scale * 100))%").foregroundStyle(p.text); Spacer(); Slider(value: $scale, in: 0.8...1.4, step: 0.05).tint(p.accent).frame(maxWidth: 190) }
                        .listRowBackground(p.surface)
                }
                Section("Иконки приложения") {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                        ForEach(themes, id: \.self) { id in
                            Button { iconID = id } label: {
                                HStack { Circle().fill(MathFlowPalette.named(id).keyEquals).frame(width: 18,height:18); Text(id.capitalized).font(.caption); Spacer(); if iconID == id { Image(systemName:"checkmark.circle.fill") } }
                                    .foregroundStyle(p.text).padding(10).background(p.key, in: RoundedRectangle(cornerRadius: 12))
                            }
                        }
                    }.listRowBackground(p.surface)
                    Text("Варианты выбраны и сохраняются. Для фактической смены иконки iOS ещё нужны отдельные AppIcon assets и запись CFBundleAlternateIcons.").font(.caption).foregroundStyle(p.muted).listRowBackground(p.surface)
                }
                Section("Шрифт") {
                    Button { showFontPicker = true } label: {
                        Label(fontName ?? "Выбрать TTF / OTF", systemImage: "textformat")
                            .foregroundStyle(p.accent)
                    }.listRowBackground(p.surface)
                    if fontPath != nil {
                        Button("Сбросить пользовательский шрифт", role: .destructive) { fontPath = nil; fontName = nil }
                            .listRowBackground(p.surface)
                    }
                    if let fontMessage { Text(fontMessage).font(.caption).foregroundStyle(p.muted).listRowBackground(p.surface) }
                }
                Section("Анимация клавиш") {
                    Picker("Стиль", selection: $keyStyle) {
                        ForEach(Array(keyStyles.enumerated()), id: \.offset) { entry in Text(entry.element.1).tag(entry.element.0) }
                    }.listRowBackground(p.surface)
                    settingSlider("Длительность", value: $keyDuration, range: 70...400, suffix: " мс", p: p)
                    settingSlider("Непрозрачность", value: $keyOpacity, range: 0.2...1, suffix: "%", p: p, percent: true)
                    settingSlider("Интенсивность", value: $keyIntensity, range: 0.02...0.2, suffix: "%", p: p, percent: true)
                    Toggle(settings.text("button_color_effect"), isOn: $keyColorEffect).tint(p.accent).listRowBackground(p.surface)
                    HStack(spacing: 13) {
                        ForEach(["accent","purple","cyan","green","pink","orange"], id: \.self) { colorID in
                            Circle().fill(effectColor(for: colorID, palette: p)).frame(width: 27, height: 27)
                                .overlay(Circle().stroke(keyEffectColorID == colorID ? p.text : .clear, lineWidth: 2).padding(-3))
                                .onTapGesture { keyEffectColorID = colorID }
                        }
                    }.padding(.vertical, 5).listRowBackground(p.surface)
                }
                Section("Анимация навигации") {
                    Toggle("Включить анимацию", isOn: $navEnabled).tint(p.accent).listRowBackground(p.surface)
                    Picker("Стиль", selection: $navStyle) {
                        ForEach(Array(navStyles.enumerated()), id: \.offset) { entry in Text(entry.element.1).tag(entry.element.0) }
                    }.listRowBackground(p.surface)
                    settingSlider("Длительность", value: $navDuration, range: 100...1000, suffix: " мс", p: p)
                    settingSlider("Интенсивность", value: $navIntensity, range: 0...1, suffix: "%", p: p, percent: true)
                    settingSlider("Масштаб иконки", value: $navIconScale, range: 0...0.2, suffix: "%", p: p, percent: true)
                    settingSlider("Непрозрачность индикатора", value: $navOpacity, range: 0...1, suffix: "%", p: p, percent: true)
                }
                Section("О MathFlow") { Text("Native iOS · SwiftUI").foregroundStyle(p.muted).listRowBackground(p.surface) }
            }
            .scrollContentBackground(.hidden)
            .background(p.background)
            .navigationTitle(settings.text("settings"))
            .safeAreaInset(edge: .bottom) {
                if hasChanges {
                    Button {
                        settings.themeID = theme; settings.languageID = language; settings.textScale = scale
                        settings.keyStyle = keyStyle; settings.keyDuration = min(400,max(70,keyDuration))
                        settings.keyOpacity = min(1,max(0.2,keyOpacity)); settings.keyIntensity = min(0.2,max(0.02,keyIntensity))
                        settings.keyColorEffect = keyColorEffect; settings.keyEffectColorID = keyEffectColorID; settings.navEnabled = navEnabled; settings.navStyle = navStyle
                        settings.navDuration = min(1000,max(100,navDuration)); settings.navIntensity = navIntensity
                        settings.navIconScale = navIconScale; settings.navOpacity = navOpacity; settings.iconID = iconID
                        settings.fontPath = fontPath; settings.fontPostScriptName = fontName
                        withAnimation(.spring(response: 0.38, dampingFraction: 0.84)) { }
                    } label: {
                        Text(settings.text("apply")).font(.system(size: 15, weight: .semibold))
                            .frame(maxWidth: .infinity).padding(.vertical, 14)
                            .background(p.keyEquals, in: Capsule()).foregroundStyle(p.onKeyEquals)
                    }.padding(.horizontal, 18).padding(.vertical, 10).background(.ultraThinMaterial)
                }
            }
        }
        .onAppear {
            guard !initialized else { return }; initialized = true
            theme=settings.themeID; language=settings.languageID; scale=settings.textScale
            keyStyle=settings.keyStyle; keyDuration=settings.keyDuration; keyOpacity=settings.keyOpacity
            keyIntensity=settings.keyIntensity; keyColorEffect=settings.keyColorEffect; keyEffectColorID=settings.keyEffectColorID; navEnabled=settings.navEnabled
            navStyle=settings.navStyle; navDuration=settings.navDuration; navIntensity=settings.navIntensity
            navIconScale=settings.navIconScale; navOpacity=settings.navOpacity; iconID=settings.iconID
            fontPath=settings.fontPath; fontName=settings.fontPostScriptName
        }
        .fileImporter(isPresented: $showFontPicker, allowedContentTypes: [.font], allowsMultipleSelection: false) { outcome in
            do {
                guard let source = try outcome.get().first else { return }
                let access = source.startAccessingSecurityScopedResource()
                defer { if access { source.stopAccessingSecurityScopedResource() } }
                let folder = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent("Fonts", isDirectory: true)
                try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
                let destination = folder.appendingPathComponent(source.lastPathComponent)
                if FileManager.default.fileExists(atPath: destination.path) { try FileManager.default.removeItem(at: destination) }
                try FileManager.default.copyItem(at: source, to: destination)
                guard CTFontManagerRegisterFontsForURL(destination as CFURL, .process, nil) else { throw MathFlowMathError.invalidNumber }
                let descriptors = CTFontManagerCreateFontDescriptorsFromURL(destination as CFURL) as? [CTFontDescriptor]
                let descriptor = descriptors?.first
                let postScript = descriptor.flatMap { CTFontDescriptorCopyAttribute($0, kCTFontNameAttribute) as? String }
                fontPath = destination.path; fontName = postScript ?? source.deletingPathExtension().lastPathComponent
                fontMessage = "Шрифт добавлен: \(fontName ?? source.lastPathComponent). Нажмите «Применить»."
            } catch { fontMessage = error.localizedDescription }
        }
    }

    private func effectColor(for id: String, palette: MathFlowPalette) -> Color {
        switch id { case "purple": return .purple; case "cyan": return .cyan; case "green": return .green; case "pink": return .pink; case "orange": return .orange; default: return palette.accent }
    }

    private func settingSlider(_ title: String, value: Binding<Double>, range: ClosedRange<Double>, suffix: String, p: MathFlowPalette, percent: Bool = false) -> some View {
        VStack(alignment: .leading, spacing: 5) {
            HStack { Text(title).foregroundStyle(p.text); Spacer(); Text(percent ? "\(Int(value.wrappedValue * 100))%" : "\(Int(value.wrappedValue))\(suffix)").foregroundStyle(p.accent).monospacedDigit() }
            Slider(value: value, in: range).tint(p.accent)
        }.listRowBackground(p.surface)
    }
}

struct MathFlowPressStyle: ButtonStyle {
    @EnvironmentObject private var settings: AppSettings

    func makeBody(configuration: Configuration) -> some View {
        let style = settings.keyStyle
        let amount = CGFloat(settings.keyIntensity)
        let pressed = configuration.isPressed
        let scales: (CGFloat, CGFloat) = {
            guard pressed else { return (1, 1) }
            switch style {
            case "scale": return (1 + amount, 1 + amount)
            case "elastic": return (1 + amount, 1 - amount * 1.25)
            case "minimal": return (1 - amount * 0.25, 1 - amount * 0.25)
            case "press": return (1 - amount * 0.45, 1 - amount)
            case "bounce": return (1 - amount * 0.60, 1 - amount * 0.60)
            case "flash": return (1 - amount * 0.12, 1 - amount * 0.12)
            case "ripple": return (1, 1)
            default: return (1 - amount, 1 - amount)
            }
        }()
        let duration = settings.keyDuration / 1000
        let isSpring = ["spring", "bounce", "elastic"].contains(style)
        let effectAlpha = style == "flash" ? settings.keyOpacity : style == "ripple" ? settings.keyOpacity * 0.34 : settings.keyOpacity * 0.22

        return configuration.label
            .scaleEffect(x: scales.0, y: scales.1, anchor: .center)
            .overlay {
                if pressed && settings.keyColorEffect {
                    if style == "ripple" {
                        Circle()
                            .fill(settings.effectColor.opacity(effectAlpha))
                            .scaleEffect(1 + amount * 3.3)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                            .allowsHitTesting(false)
                    } else {
                        RoundedRectangle(cornerRadius: 16, style: .continuous)
                            .fill(settings.effectColor.opacity(effectAlpha))
                            .allowsHitTesting(false)
                    }
                }
            }
            .animation(
                isSpring
                    ? .spring(response: duration, dampingFraction: style == "elastic" ? 0.48 : 0.72)
                    : .easeOut(duration: duration),
                value: configuration.isPressed
            )
    }
}

struct HistoryEntry: Identifiable {
    let id = UUID()
    let expression: String
    let result: String
    let steps: [String]
    let createdAt: Date
}

final class HistoryStore: ObservableObject {
    static let shared = HistoryStore()
    @Published private(set) var entries: [HistoryEntry] = []
    private let key = "mathflow.history.v1"
    private init() { load() }
    func add(expression: String, result: String, steps: [String]) {
        entries.insert(HistoryEntry(expression: expression, result: result, steps: steps, createdAt: Date()), at: 0)
        entries = Array(entries.prefix(100))
        save()
    }
    func clear() { entries = []; save() }
    private func load() {
        guard let data = UserDefaults.standard.data(forKey: key),
              let decoded = try? JSONDecoder().decode([StoredEntry].self, from: data) else { return }
        entries = decoded.map { HistoryEntry(expression: $0.expression, result: $0.result, steps: $0.steps, createdAt: $0.createdAt ?? .distantPast) }
    }
    private func save() {
        let values = entries.map { StoredEntry(expression: $0.expression, result: $0.result, steps: $0.steps, createdAt: $0.createdAt) }
        UserDefaults.standard.set(try? JSONEncoder().encode(values), forKey: key)
    }
    private struct StoredEntry: Codable {
        let expression: String
        let result: String
        let steps: [String]
        let createdAt: Date?
        init(expression: String, result: String, steps: [String], createdAt: Date) {
            self.expression = expression; self.result = result; self.steps = steps; self.createdAt = createdAt
        }
    }
}
