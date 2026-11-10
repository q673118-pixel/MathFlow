import SwiftUI
import MathFlowCore

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
    @Published var themeID: String {
        didSet { UserDefaults.standard.set(themeID, forKey: "mathflow.theme") }
    }

    init() { themeID = UserDefaults.standard.string(forKey: "mathflow.theme") ?? "vanilla" }
    var palette: MathFlowPalette { MathFlowPalette.named(themeID) }
}

struct RootView: View {
    @EnvironmentObject private var settings: AppSettings
    @State private var selection = 0

    var body: some View {
        ZStack {
            settings.palette.background.ignoresSafeArea()
            TabView(selection: $selection) {
                CalculatorView().tabItem { Label("Главная", systemImage: "function") }.tag(0)
                ToolsView().tabItem { Label("Инструменты", systemImage: "square.grid.2x2") }.tag(1)
                SolverView().tabItem { Label("Уравнения", systemImage: "equal") }.tag(2)
                HistoryView().tabItem { Label("История", systemImage: "clock.arrow.circlepath") }.tag(3)
                SettingsView().tabItem { Label("Настройки", systemImage: "slider.horizontal.3") }.tag(4)
            }
            .tint(settings.palette.accent)
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
                                    .background(key == "=" ? p.keyEquals : key == "AC" ? p.keyAccent : p.key,
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
        switch key {
        case "AC": expression = ""; result = "0"; error = nil; steps = []
        case "⌫": expression = String(expression.dropLast())
        case "=":
            do {
                let mode: AngleMode = degrees ? .degrees : .radians
                let calculation = try ExpressionEngine(angleMode: mode).calculate(expression)
                result = calculation.displayValue
                steps = calculation.steps
                error = nil
                HistoryStore.shared.add(expression: expression, result: result, steps: steps)
            } catch {
                result = "Ошибка"
                self.error = error.localizedDescription
                steps = []
            }
        case "√": expression += "sqrt("
        case "x²": expression += "^2"
        case "xʸ": expression += "^"
        case "π": expression += "pi"
        case "sin", "cos", "tan", "ln", "log", "abs": expression += key + "("
        default: expression += key
        }
    }
}

struct ToolsView: View {
    @EnvironmentObject private var settings: AppSettings
    @State private var selected = "Дроби"
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

    private let options = ["Дроби", "Проценты", "Геометрия", "Конвертер", "График"]
    private var category: UnitCategory { UnitConverter.categories.first(where: { $0.id == categoryID }) ?? UnitConverter.categories[0] }
    private var fromUnit: UnitDefinition { category.units.first(where: { $0.id == fromID }) ?? category.units[0] }
    private var toUnit: UnitDefinition { category.units.first(where: { $0.id == toID }) ?? category.units[min(1, category.units.count - 1)] }

    var body: some View {
        let p = settings.palette
        VStack(alignment: .leading, spacing: 14) {
            Text("Инструменты").font(.largeTitle.bold()).foregroundStyle(p.text)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(options, id: \.self) { option in
                        Button(option) {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                selected = option; result = ""; error = nil
                            }
                        }
                        .font(.subheadline.weight(.medium))
                        .padding(.horizontal, 13).padding(.vertical, 9)
                        .background(selected == option ? p.keyEquals : p.key, in: Capsule())
                        .foregroundStyle(selected == option ? p.onKeyEquals : p.text)
                    }
                }
            }
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    switch selected {
                    case "Дроби": fractionContent(p)
                    case "Проценты": percentContent(p)
                    case "Геометрия": geometryContent(p)
                    case "Конвертер": converterContent(p)
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
            do { withAnimation(.easeInOut(duration: 0.2)) { result = try run(); error = nil } }
            catch { result = ""; self.error = error.localizedDescription }
        } label: {
            Text(title).font(.system(size: 16, weight: .semibold))
                .frame(maxWidth: .infinity).padding(.vertical, 15)
                .background(p.keyEquals, in: RoundedRectangle(cornerRadius: 15, style: .continuous))
                .foregroundStyle(p.onKeyEquals)
        }.buttonStyle(MathFlowPressStyle())
    }

    @ViewBuilder private func fractionContent(_ p: MathFlowPalette) -> some View {
        Text("Калькулятор дробей").font(.title2.bold()).foregroundStyle(p.text)
        field("Первая дробь", text: $first, palette: p)
        HStack(spacing: 8) {
            ForEach(FractionOperation.allCases, id: \.self) { item in
                Button(item.rawValue) { operation = item }
                    .font(.title2.weight(.semibold)).frame(maxWidth: .infinity).padding(.vertical, 11)
                    .background(operation == item ? p.keyEquals : p.key, in: RoundedRectangle(cornerRadius: 12))
                    .foregroundStyle(operation == item ? p.onKeyEquals : p.text)
            }
        }
        field("Вторая дробь", text: $second, palette: p)
        action("Вычислить", palette: p) {
            let value = try FractionTools.calculate(first, second, operation: operation)
            return "Точный ответ: \(value.mixed)\nДесятичный вид: \(value.decimal)\nСокращённая дробь: \(value.exact)"
        }
    }

    @ViewBuilder private func percentContent(_ p: MathFlowPalette) -> some View {
        Text("Процентный калькулятор").font(.title2.bold()).foregroundStyle(p.text)
        Picker("Операция", selection: $percentMode) {
            Text("% от числа").tag(0); Text("Какой процент").tag(1); Text("Изменение").tag(2)
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
        Text("Геометрия").font(.title2.bold()).foregroundStyle(p.text)
        Picker("Фигура", selection: $geometryMode) {
            Text("Прямоугольник").tag(0); Text("Окружность").tag(1); Text("Треугольник").tag(2)
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
        Text("Конвертер величин").font(.title2.bold()).foregroundStyle(p.text)
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(UnitConverter.categories) { item in
                    Button(item.title) { categoryID = item.id }
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
        Text("Построение y = f(x) по диапазону значений.").font(.subheadline).foregroundStyle(p.muted)
        Text("Графический модуль будет подключён после переноса вычислителя с поддержкой переменной x; показывать фиктивный график вместо вычислений нельзя.")
            .font(.caption).foregroundStyle(p.muted)
            .padding(14).frame(maxWidth: .infinity, alignment: .leading)
            .background(p.surface, in: RoundedRectangle(cornerRadius: 14))
    }
}

struct SolverView: View {
    @EnvironmentObject private var settings: AppSettings
    @State private var equation = ""
    @State private var answer = "Введите уравнение"
    var body: some View {
        let p = settings.palette
        VStack(alignment: .leading, spacing: 16) {
            Text("Уравнения").font(.largeTitle.bold()).foregroundStyle(p.text)
            TextField("Например: 2x + 3 = 11", text: $equation)
                .textFieldStyle(.roundedBorder)
            Text(answer).foregroundStyle(p.text)
            Spacer()
        }
        .padding()
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
                    Text("Здесь появятся результаты основного калькулятора.").foregroundStyle(p.muted)
                }
                ForEach(history.entries) { entry in
                    VStack(alignment: .leading, spacing: 5) {
                        Text(entry.expression).foregroundStyle(p.muted)
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
            .navigationTitle("История")
            .toolbar { Button("Очистить") { history.clear() } }
        }
    }
}

struct SettingsView: View {
    @EnvironmentObject private var settings: AppSettings
    private let themes = ["vanilla", "black", "white", "purple", "ocean", "emerald", "sunset", "rose", "midnight", "graphite"]
    var body: some View {
        let p = settings.palette
        NavigationStack {
            List {
                Section("Темы оформления") {
                    ForEach(themes, id: \.self) { theme in
                        Button {
                            withAnimation(.spring(response: 0.38, dampingFraction: 0.84)) { settings.themeID = theme }
                        } label: {
                            HStack {
                                Text(theme.capitalized).foregroundStyle(p.text)
                                Spacer()
                                if settings.themeID == theme { Image(systemName: "checkmark.circle.fill").foregroundStyle(p.accent) }
                            }
                        }
                        .listRowBackground(p.surface)
                    }
                }
                Section("О MathFlow") { Text("Native iOS · SwiftUI").foregroundStyle(p.muted).listRowBackground(p.surface) }
            }
            .scrollContentBackground(.hidden)
            .background(p.background)
            .navigationTitle("Настройки")
        }
    }
}

struct MathFlowPressStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.965 : 1)
            .animation(.spring(response: 0.22, dampingFraction: 0.8), value: configuration.isPressed)
    }
}

struct HistoryEntry: Identifiable {
    let id = UUID()
    let expression: String
    let result: String
    let steps: [String]
}

final class HistoryStore: ObservableObject {
    static let shared = HistoryStore()
    @Published private(set) var entries: [HistoryEntry] = []
    private let key = "mathflow.history.v1"
    private init() { load() }
    func add(expression: String, result: String, steps: [String]) {
        entries.insert(HistoryEntry(expression: expression, result: result, steps: steps), at: 0)
        entries = Array(entries.prefix(100))
        save()
    }
    func clear() { entries = []; save() }
    private func load() {
        guard let data = UserDefaults.standard.data(forKey: key),
              let decoded = try? JSONDecoder().decode([StoredEntry].self, from: data) else { return }
        entries = decoded.map { HistoryEntry(expression: $0.expression, result: $0.result, steps: $0.steps) }
    }
    private func save() {
        let values = entries.map { StoredEntry(expression: $0.expression, result: $0.result, steps: $0.steps) }
        UserDefaults.standard.set(try? JSONEncoder().encode(values), forKey: key)
    }
    private struct StoredEntry: Codable {
        let expression: String
        let result: String
        let steps: [String]
    }
}
