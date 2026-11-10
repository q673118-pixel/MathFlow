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
    private let options = ["Дроби", "Проценты", "Геометрия", "Конвертер", "График"]

    var body: some View {
        let p = settings.palette
        VStack(alignment: .leading, spacing: 14) {
            Text("Инструменты").font(.largeTitle.bold()).foregroundStyle(p.text)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(options, id: \.self) { option in
                        Button(option) { withAnimation(.easeInOut(duration: 0.2)) { selected = option } }
                            .font(.subheadline.weight(.medium))
                            .padding(.horizontal, 13).padding(.vertical, 9)
                            .background(selected == option ? p.keyEquals : p.key, in: Capsule())
                            .foregroundStyle(selected == option ? p.onKeyEquals : p.text)
                    }
                }
            }
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text(selected).font(.title2.weight(.semibold)).foregroundStyle(p.text)
                    Text("Раздел будет перенесён вместе с математической логикой Android-версии.")
                        .foregroundStyle(p.muted)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(18)
                .background(p.surface, in: RoundedRectangle(cornerRadius: 22))
            }
            Spacer(minLength: 0)
        }
        .padding(16)
        .background(p.background)
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
