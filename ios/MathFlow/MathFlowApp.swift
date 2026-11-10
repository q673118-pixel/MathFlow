// Native SwiftUI port; Android remains unchanged on the redesign-ui branch.
import SwiftUI
import MathFlowCore
import UniformTypeIdentifiers
import CoreText
import UIKit

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

@MainActor
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

    func applyIcon(_ id: String) {
        guard UIApplication.shared.supportsAlternateIcons else { return }
        let alternateName: String? = id == "vanilla" ? nil : "AppIcon" + id.prefix(1).uppercased() + String(id.dropFirst())
        UIApplication.shared.setAlternateIconName(alternateName) { [weak self] error in
            guard error == nil else { return }
            Task { @MainActor in self?.iconID = id }
        }
    }
    @Published var fontPath: String? { didSet { UserDefaults.standard.set(fontPath, forKey: "mathflow.fontPath") } }
    @Published var fontPostScriptName: String? { didSet { UserDefaults.standard.set(fontPostScriptName, forKey: "mathflow.fontName") } }

    init() {
        let d = UserDefaults.standard
        let themes = ["vanilla", "black", "white", "purple", "ocean", "emerald", "sunset", "rose", "midnight", "graphite"]
        let languages = MathFlowLanguage.all.map(\.id)
        let keyStyles = ["spring", "press", "scale", "flash", "ripple", "bounce", "elastic", "minimal"]
        let navStyles = ["fluid_pill", "spring_motion", "light_trail", "morph", "expand", "soft_focus", "elastic_slide", "ripple_bloom"]
        let colors = ["accent", "purple", "cyan", "green", "pink", "orange"]
        let savedTheme = d.string(forKey: "mathflow.theme") ?? "vanilla"
        let savedLanguage = d.string(forKey: "mathflow.language") ?? "ru"
        themeID = themes.contains(savedTheme) ? savedTheme : "vanilla"
        languageID = languages.contains(savedLanguage) ? savedLanguage : "ru"
        let savedScale = d.object(forKey: "mathflow.textScale") as? Double ?? 1
        textScale = min(1.4, max(0.8, (savedScale * 20).rounded() / 20))
        let savedKeyStyle = d.string(forKey: "mathflow.keyStyle") ?? "spring"
        keyStyle = keyStyles.contains(savedKeyStyle) ? savedKeyStyle : "spring"
        keyDuration = min(400, max(70, d.object(forKey: "mathflow.keyDuration") as? Double ?? 140))
        keyOpacity = min(1, max(0.2, d.object(forKey: "mathflow.keyOpacity") as? Double ?? 0.72))
        keyIntensity = min(0.2, max(0.02, d.object(forKey: "mathflow.keyIntensity") as? Double ?? 0.10))
        keyColorEffect = d.object(forKey: "mathflow.keyColorEffect") as? Bool ?? false
        let savedColor = d.string(forKey: "mathflow.keyEffectColor") ?? "accent"
        keyEffectColorID = colors.contains(savedColor) ? savedColor : "accent"
        navEnabled = d.object(forKey: "mathflow.navEnabled") as? Bool ?? true
        let savedNavStyle = d.string(forKey: "mathflow.navStyle") ?? "fluid_pill"
        navStyle = navStyles.contains(savedNavStyle) ? savedNavStyle : "fluid_pill"
        navDuration = min(1000, max(100, d.object(forKey: "mathflow.navDuration") as? Double ?? 280))
        navIntensity = min(1, max(0, d.object(forKey: "mathflow.navIntensity") as? Double ?? 0.65))
        navIconScale = min(0.2, max(0, d.object(forKey: "mathflow.navIconScale") as? Double ?? 0.08))
        navOpacity = min(1, max(0, d.object(forKey: "mathflow.navOpacity") as? Double ?? 0.85))
        let savedIcon = d.string(forKey: "mathflow.icon") ?? "vanilla"
        iconID = themes.contains(savedIcon) ? savedIcon : "vanilla"
        let savedFontPath = d.string(forKey: "mathflow.fontPath")
        if let savedFontPath, FileManager.default.fileExists(atPath: savedFontPath) {
            fontPath = savedFontPath
            fontPostScriptName = d.string(forKey: "mathflow.fontName")
            _ = CTFontManagerRegisterFontsForURL(URL(fileURLWithPath: savedFontPath) as CFURL, .process, nil)
        } else {
            fontPath = nil
            fontPostScriptName = nil
        }
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
    @State private var showSplash = ProcessInfo.processInfo.environment["XCTestConfigurationFilePath"] == nil
    @State private var showWelcome = false
    @State private var splashScale: CGFloat = 0
    @State private var splashAlpha = 1.0
    @State private var dotOffset: CGFloat = -450
    @State private var dotAlpha = 0.0
    @State private var dotScaleX: CGFloat = 1
    @State private var dotScaleY: CGFloat = 1
    @ScaledMetric(relativeTo: .body) private var inheritedFontSize: CGFloat = 17

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
        .font(settings.fontPostScriptName.map { .custom($0, size: inheritedFontSize * CGFloat(settings.textScale)) } ?? .system(size: inheritedFontSize * CGFloat(settings.textScale)))
        .animation(.easeInOut(duration: 0.2), value: selection)
        .overlay {
            if showSplash {
                ZStack {
                    settings.palette.background.ignoresSafeArea()
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
        .alert(settings.languageID == "kk" ? "MathFlow қолданбасына қош келдіңіз" : "Добро пожаловать в MathFlow", isPresented: $showWelcome) {
            Button(settings.languageID == "kk" ? "ЖАРАЙДЫ" : "ОК") { UserDefaults.standard.set(true, forKey: "mathflow.welcomeSeen") }
        } message: {
            Text(settings.languageID == "kk"
                 ? "MathFlow қолданбасын жүктегеніңізге рақмет. Бұл қолданба математиканы түсінікті әрі ыңғайлы етуге көмектеседі деп сенеміз."
                 : "Спасибо, что скачали MathFlow. Мы надеемся, что приложение сделает работу с математикой удобнее и понятнее.")
        }
        .onAppear(perform: runIntro)
    }

    private func runIntro() {
        guard showSplash else { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            withAnimation(.timingCurve(0, 0, 0.2, 1, duration: 0.39)) { splashScale = 1.08 }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.39) {
                withAnimation(.spring(response: 0.42, dampingFraction: 0.48)) { splashScale = 1 }
                dotAlpha = 1
                withAnimation(.easeIn(duration: 0.42)) { dotOffset = 0 }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.42) {
                    withAnimation(.easeOut(duration: 0.06)) { dotScaleX = 1.18; dotScaleY = 0.78 }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.06) {
                        withAnimation(.easeOut(duration: 0.19)) { dotOffset = -42; dotScaleX = 0.90; dotScaleY = 1.12 }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.19) {
                            withAnimation(.easeIn(duration: 0.17)) { dotOffset = 0; dotScaleX = 1.02; dotScaleY = 0.98 }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.215) {
                                withAnimation(.easeOut(duration: 0.045)) { dotScaleX = 1.12; dotScaleY = 0.88 }
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.045) {
                                    withAnimation(.easeOut(duration: 0.125)) { dotOffset = -14; dotScaleX = 0.96; dotScaleY = 1.04 }
                                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.125) {
                                        withAnimation(.easeIn(duration: 0.115)) { dotOffset = 0; dotScaleX = 1; dotScaleY = 1 }
                                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.235) {
                                            withAnimation(.easeOut(duration: 1.0)) { splashAlpha = 0; dotAlpha = 0; splashScale = 0.96 }
                                            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                                                showSplash = false
                                                if ProcessInfo.processInfo.environment["XCTestConfigurationFilePath"] == nil && !UserDefaults.standard.bool(forKey: "mathflow.welcomeSeen") { showWelcome = true }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

struct NavPreviewSettings {
    let enabled: Bool
    let style: String
    let duration: Double
    let intensity: Double
    let iconScale: Double
    let opacity: Double
}

private struct BottomNavigation: View {
    @EnvironmentObject private var settings: AppSettings
    @Binding var selection: Int
    var previewSettings: NavPreviewSettings? = nil
    private let items: [(String,String)] = [("function","home"),("square.grid.2x2","tools"),("equal","solver"),("clock.arrow.circlepath","history"),("slider.horizontal.3","settings")]

    var body: some View {
        let p = settings.palette
        let enabled = previewSettings?.enabled ?? settings.navEnabled
        let style = previewSettings?.style ?? settings.navStyle
        let durationValue = previewSettings?.duration ?? settings.navDuration
        let intensity = previewSettings?.intensity ?? settings.navIntensity
        let iconScale = previewSettings?.iconScale ?? settings.navIconScale
        let opacity = previewSettings?.opacity ?? settings.navOpacity
        HStack(spacing: 3) {
            ForEach(items.indices, id: \.self) { index in
                let selected = selection == index
                Button {
                    withAnimation(animation) { selection = index }
                } label: {
                    Image(systemName: items[index].0)
                        .font(.system(size: 26, weight: .regular))
                        .scaleEffect(
                            x: enabled && selected && style == "morph" ? 1 + iconScale * 0.45 : (enabled && selected ? 1 + iconScale : 1),
                            y: enabled && selected && style == "morph" ? 1 - iconScale * (0.35 + intensity * 0.45) : (enabled && selected ? 1 + iconScale : 1)
                        )
                        .foregroundStyle(selected ? p.accent : p.muted)
                        .frame(maxWidth: .infinity).frame(height: 48)
                    .background {
                        if selected && enabled && !["soft_focus", "light_trail"].contains(style) {
                            RoundedRectangle(cornerRadius: style == "morph" ? 15 : 22, style: .continuous)
                                .fill(p.navActive.opacity(opacity))
                                .matchedGeometryEffect(id: "mathflow.nav.indicator", in: namespace)
                                .scaleEffect(x: style == "expand" ? 1 + intensity * 0.12 : style == "elastic_slide" ? 1 + intensity * 0.08 : style == "ripple_bloom" ? 0.72 + intensity * 0.28 : 1, y: 1)
                        }
                        if selected && enabled && style == "soft_focus" {
                            RoundedRectangle(cornerRadius: 22, style: .continuous)
                                .fill(p.navActive.opacity(opacity * 0.62))
                        }
                    }
                    .opacity(enabled && style == "soft_focus" && !selected ? 0.58 : 1)
                    .scaleEffect(enabled && selected && style == "expand" ? 1 + intensity * 0.12 : 1)
                    .overlay(alignment: .bottom) {
                        if selected && style == "light_trail" {
                            Capsule().fill(p.accent)
                                .frame(width: 38 + 24 * intensity, height: 3)
                                .padding(.bottom, 2)
                        }
                    }
                }
                .buttonStyle(MathFlowInteractionPressStyle())
                .accessibilityLabel(settings.text(items[index].1))
            }
        }
        .padding(.horizontal, 6).padding(.vertical, 5)
        .background(p.surface, in: RoundedRectangle(cornerRadius: 26, style: .continuous))
        .padding(.horizontal, 28).padding(.vertical, 8)
    }

    @Namespace private var namespace
    private var animation: Animation {
        let enabled = previewSettings?.enabled ?? settings.navEnabled
        let style = previewSettings?.style ?? settings.navStyle
        let durationValue = previewSettings?.duration ?? settings.navDuration
        guard enabled else { return .linear(duration: 0) }
        let duration = durationValue / 1000
        switch style {
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
        ["AC", "(", ")", "÷"],
        ["7", "8", "9", "×"],
        ["4", "5", "6", "−"],
        ["1", "2", "3", "+"],
        ["%", "0", ".", "="]
    ]
    private let scientificRows = [
        ["sin", "cos", "tan", "ln"],
        ["log", "abs", "π", "e"],
        ["√", "x²", "xʸ", "⌫"]
    ]

    var body: some View {
        let p = settings.palette
        VStack(spacing: 10) {
            VStack(alignment: .trailing, spacing: 10) {
                HStack(spacing: 8) {
                    Button("f(x)") { withAnimation(.easeInOut(duration: 0.18)) { scientific.toggle() } }
                        .mathFlowFont(size: 13, weight: .medium)
                        .foregroundStyle(scientific ? p.accent : p.muted)
                        .padding(.horizontal, 12).padding(.vertical, 7)
                        .background(scientific ? p.navActive : p.key, in: Capsule())
                    if scientific {
                        Button(degrees ? "DEG" : "RAD") { degrees.toggle() }
                            .mathFlowFont(size: 13, weight: .medium).foregroundStyle(p.accent)
                            .padding(.horizontal, 12).padding(.vertical, 7).background(p.navActive, in: Capsule())
                    }
                    if !steps.isEmpty {
                        Button(settings.text("steps")) { withAnimation(.easeInOut(duration: 0.2)) { showSteps.toggle() } }
                            .mathFlowFont(size: 13, weight: .medium)
                            .foregroundStyle(showSteps ? p.accent : p.muted)
                            .padding(.horizontal, 12).padding(.vertical, 7)
                            .background(showSteps ? p.navActive : p.key, in: Capsule())
                    }
                    Spacer()
                    Button { expression = String(expression.dropLast()) } label: {
                        Image(systemName: "delete.left").font(.system(size: 18)).foregroundStyle(p.muted)
                            .frame(width: 40, height: 40).background(p.key, in: Circle())
                    }
                }
                Text(expression.isEmpty ? " " : expression.replacingOccurrences(of: "sqrt", with: "√").replacingOccurrences(of: "pi", with: "π"))
                    .mathFlowFont(size: 23, weight: .regular, design: .rounded)
                    .foregroundStyle(p.muted)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .trailing)
                Text(result)
                    .mathFlowFont(size: result.count > 16 ? 24 : result.count > 11 ? 32 : result.count > 8 ? 40 : 52, weight: .medium, design: .rounded)
                    .foregroundStyle(error == nil ? p.text : p.error)
                    .lineLimit(1)
                    .minimumScaleFactor(0.5)
                    .contentTransition(.numericText())
                if let error { Text(error).mathFlowFont(size: 12).foregroundStyle(p.error) }
            }
            .padding(18)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)
            .background(p.surface, in: RoundedRectangle(cornerRadius: 27, style: .continuous))

            if !steps.isEmpty && showSteps {
                ScrollView {
                    VStack(alignment: .leading, spacing: 4) {
                        ForEach(Array(steps.enumerated()), id: \.offset) { index, step in
                            Text("\(index + 1). \(step)").mathFlowFont(size: 12).foregroundStyle(p.muted)
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
                                    .mathFlowFont(size: key.count > 1 && key != "AC" ? (scientific ? 18 : 20) : (scientific ? 22 : 28), weight: .regular, design: .rounded)
                                    .frame(maxWidth: .infinity, minHeight: 50)
                                     .background(key == "=" ? p.keyEquals : (key == "AC" || ["÷", "×", "−", "+"].contains(key)) ? p.keyAccent : p.key,
                                                in: Capsule())
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
                result = settings.text("error")
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
            Text(settings.text("tools")).mathFlowFont(size: 34, weight: .bold).foregroundStyle(p.text)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(options, id: \.self) { optionID in
                        Button(settings.text(optionID)) {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                selected = optionID; result = ""; error = nil
                            }
                        }
                        .mathFlowFont(size: 15, weight: .medium)
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
                            Text(settings.text("result")).mathFlowFont(size: 17, weight: .semibold).foregroundStyle(p.text)
                            Text(result).mathFlowFont(size: 17, weight: .medium, design: .rounded).foregroundStyle(p.text).textSelection(.enabled)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(p.surface, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
                        .transition(.opacity.combined(with: .move(edge: .top)))
                    }
                    if let error {
                        Text(error).mathFlowFont(size: 12).foregroundStyle(p.error).padding(.horizontal, 4)
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
            Text(title).mathFlowFont(size: 12, weight: .medium).foregroundStyle(p.muted)
            TextField("0", text: text)
                .keyboardType(decimal ? .decimalPad : .numbersAndPunctuation)
                .mathFlowFont(size: 19, weight: .medium, design: .rounded)
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
            Text(title).mathFlowFont(size: 16, weight: .semibold)
                .frame(maxWidth: .infinity).padding(.vertical, 15)
                .background(p.keyEquals, in: RoundedRectangle(cornerRadius: 15, style: .continuous))
                .foregroundStyle(p.onKeyEquals)
        }.buttonStyle(MathFlowInteractionPressStyle())
    }

    @ViewBuilder private func fractionContent(_ p: MathFlowPalette) -> some View {
        Text(settings.text("fractions")).mathFlowFont(size: 22, weight: .bold).foregroundStyle(p.text)
        field(settings.text("fraction_one"), text: $first, palette: p)
        HStack(spacing: 8) {
            ForEach(FractionOperation.allCases, id: \.self) { item in
                Button(item.rawValue) { operation = item }
                    .mathFlowFont(size: 22, weight: .semibold).frame(maxWidth: .infinity).padding(.vertical, 11)
                    .background(operation == item ? p.keyEquals : p.key, in: RoundedRectangle(cornerRadius: 12))
                    .foregroundStyle(operation == item ? p.onKeyEquals : p.text)
            }
        }
        field(settings.text("fraction_two"), text: $second, palette: p)
        action(settings.text("calculate"), palette: p) {
            let value = try FractionTools.calculate(first, second, operation: operation)
            return "Точный ответ: \(value.mixed)\nДесятичный вид: \(value.decimal)\nСокращённая дробь: \(value.exact)"
        }
    }

    @ViewBuilder private func percentContent(_ p: MathFlowPalette) -> some View {
        Text(settings.text("percentages")).mathFlowFont(size: 22, weight: .bold).foregroundStyle(p.text)
        Picker("Операция", selection: $percentMode) {
            Text(settings.text("percent_of")).tag(0); Text(settings.text("what_percent")).tag(1); Text(settings.text("change_percent")).tag(2)
        }.pickerStyle(.segmented)
        field(percentMode == 1 ? settings.text("part") : settings.text("percent"), text: $first, palette: p)
        field(percentMode == 1 ? settings.text("whole") : settings.text("number"), text: $second, palette: p)
        if percentMode == 2 {
            Picker("Изменение", selection: $increase) {
                Text(settings.text("increase")).tag(true); Text(settings.text("percent_decrease")).tag(false)
            }.pickerStyle(.segmented)
        }
        action(settings.text("calculate"), palette: p) {
            switch percentMode {
            case 0: return "\(first)% от \(second) = \(try PercentTools.percentOf(first, second))"
            case 1: return "\(first) составляет \(try PercentTools.whatPercent(first, second))% от \(second)"
            default: return (increase ? "После увеличения" : "После уменьшения") + ": " + (try PercentTools.change(second, percent: first, increase: increase))
            }
        }
    }

    @ViewBuilder private func geometryContent(_ p: MathFlowPalette) -> some View {
        Text(settings.text("geometry")).mathFlowFont(size: 22, weight: .bold).foregroundStyle(p.text)
        Picker("Фигура", selection: $geometryMode) {
            Text(settings.text("rectangle")).tag(0); Text(settings.text("circle")).tag(1); Text(settings.text("triangle")).tag(2)
        }.pickerStyle(.segmented)
        if geometryMode == 0 {
            field(settings.text("side_a"), text: $first, palette: p); field(settings.text("side_b"), text: $second, palette: p)
        } else if geometryMode == 1 {
            field(settings.text("radius"), text: $first, palette: p)
        } else {
            field("Сторона A", text: $first, palette: p); field("Сторона B", text: $second, palette: p); field(settings.text("side_c"), text: $third, palette: p)
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
        Text(settings.text("converter_title")).mathFlowFont(size: 22, weight: .bold).foregroundStyle(p.text)
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(UnitConverter.categories) { item in
                    Button(settings.text(item.id)) { categoryID = item.id }
                        .mathFlowFont(size: 12, weight: .semibold).padding(.horizontal, 12).padding(.vertical, 9)
                        .background(categoryID == item.id ? p.keyEquals : p.key, in: Capsule())
                        .foregroundStyle(categoryID == item.id ? p.onKeyEquals : p.text)
                }
            }
        }
        field(settings.text("value"), text: $converterValue, palette: p)
        HStack(spacing: 8) {
            unitPicker(settings.text("from"), selection: $fromID, p: p)
            Button { swap(&fromID, &toID) } label: {
                Image(systemName: "arrow.left.arrow.right").font(.title3).foregroundStyle(p.accent)
                    .frame(width: 42, height: 48).background(p.key, in: Circle())
            }
            unitPicker(settings.text("to"), selection: $toID, p: p)
        }
        action("Конвертировать", palette: p) {
            let value = try UnitConverter.convert(converterValue, category: categoryID, from: fromID, to: toID)
            return "\(converterValue) \(fromUnit.label) = \(value) \(toUnit.label)"
        }
    }

    private func unitPicker(_ title: String, selection: Binding<String>, p: MathFlowPalette) -> some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(title).mathFlowFont(size: 12).foregroundStyle(p.muted)
            Menu {
                ForEach(category.units) { unit in
                    Button(unit.label) { selection.wrappedValue = unit.id }
                }
            } label: {
                HStack { Text(category.units.first(where: { $0.id == selection.wrappedValue })?.label ?? "—"); Spacer(); Image(systemName: "chevron.down") }
                    .mathFlowFont(size: 15, weight: .medium).foregroundStyle(p.text).padding(13)
                    .background(p.key, in: RoundedRectangle(cornerRadius: 13))
            }
        }.frame(maxWidth: .infinity)
    }

    @ViewBuilder private func graphContent(_ p: MathFlowPalette) -> some View {
        Text(settings.text("graph")).mathFlowFont(size: 22, weight: .bold).foregroundStyle(p.text)
        field(settings.text("graph_input"), text: $graphExpression, palette: p, decimal: false)
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
        Text("Поддерживаются выражения калькулятора и переменная x.").mathFlowFont(size: 12).foregroundStyle(p.muted)
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
            Text(settings.text("solver")).mathFlowFont(size: 34, weight: .bold).foregroundStyle(p.text)
            Text(settings.text("equation_help")).mathFlowFont(size: 15).foregroundStyle(p.muted)
            TextField(settings.text("example_equation"), text: $equation)
                .textFieldStyle(.plain)
                .mathFlowFont(size: 18, weight: .medium, design: .rounded)
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
                Text(settings.text("solve")).mathFlowFont(size: 16, weight: .semibold)
                    .frame(maxWidth: .infinity).padding(.vertical, 15)
                    .background(p.keyEquals, in: RoundedRectangle(cornerRadius: 15))
                    .foregroundStyle(p.onKeyEquals)
            }.buttonStyle(MathFlowInteractionPressStyle())
            if !answer.isEmpty {
                VStack(alignment: .leading, spacing: 10) {
                    Text(settings.text("result")).mathFlowFont(size: 12, weight: .semibold).foregroundStyle(p.muted)
                    Text(answer).mathFlowFont(size: 23, weight: .semibold, design: .rounded).foregroundStyle(p.text)
                }.frame(maxWidth: .infinity, alignment: .leading).padding(16)
                    .background(p.surface, in: RoundedRectangle(cornerRadius: 18))
                VStack(alignment: .leading, spacing: 9) {
                    Text(settings.text("steps")).mathFlowFont(size: 17, weight: .semibold).foregroundStyle(p.text)
                    ForEach(Array(steps.enumerated()), id: \.offset) { index, step in
                        HStack(alignment: .top, spacing: 9) {
                            Text("\(index + 1).").foregroundStyle(p.accent).fontWeight(.semibold)
                            Text(step).foregroundStyle(p.muted)
                        }.mathFlowFont(size: 15)
                    }
                }.padding(16).frame(maxWidth: .infinity, alignment: .leading)
                    .background(p.surface, in: RoundedRectangle(cornerRadius: 18))
            }
            if let error { Text(error).mathFlowFont(size: 12).foregroundStyle(p.error) }
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
                        Text(entry.createdAt.formatted(date: .abbreviated, time: .shortened)).mathFlowFont(size: 11).foregroundStyle(p.muted)
                        Text(entry.result).mathFlowFont(size: 22, weight: .semibold).foregroundStyle(p.text)
                        ForEach(Array(entry.steps.enumerated()), id: \.offset) { _, step in
                            Text(step).mathFlowFont(size: 12).foregroundStyle(p.muted)
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
    @State private var languageSearch = ""
    @State private var savedFonts: [SavedFont] = []
    @State private var previewNavSelection = 0
    @State private var initialized = false

    private let themes = ["vanilla", "black", "white", "purple", "ocean", "emerald", "sunset", "rose", "midnight", "graphite"]
    private let keyStyles = [("spring","Spring"),("press","Press"),("scale","Scale"),("flash","Flash"),("ripple","Ripple"),("bounce","Bounce"),("elastic","Elastic"),("minimal","Minimal")]
    private let navStyles = [("fluid_pill","Fluid Pill"),("spring_motion","Spring Motion"),("light_trail","Light Trail"),("morph","Morph"),("expand","Expand"),("soft_focus","Soft Focus"),("elastic_slide","Elastic Slide"),("ripple_bloom","Ripple Bloom")]

    private var navStyleDescriptionKey: String {
        switch navStyle {
        case "fluid_pill": return "nav_style_fluid_desc"
        case "spring_motion": return "nav_style_spring_desc"
        case "light_trail": return "nav_style_trail_desc"
        case "morph": return "nav_style_morph_desc"
        case "expand": return "nav_style_expand_desc"
        case "soft_focus": return "nav_style_focus_desc"
        case "elastic_slide": return "nav_style_elastic_desc"
        default: return "nav_style_ripple_desc"
        }
    }

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
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(id.capitalized).mathFlowFont(size: 14, weight: .medium).foregroundStyle(p.text)
                                    Text(themeDescription(id)).mathFlowFont(size: 11).foregroundStyle(p.muted)
                                }
                                Spacer()
                                if theme == id { Image(systemName: "checkmark.circle.fill").foregroundStyle(p.accent) }
                            }.padding(.vertical, 3)
                        }.listRowBackground(p.surface)
                    }
                    Text(settings.text("theme_hint")).mathFlowFont(size: 12).foregroundStyle(p.muted).listRowBackground(p.surface)
                }
                Section(settings.text("app_language")) {
                    ForEach(MathFlowLanguage.all.filter {
                        languageSearch.isEmpty || $0.nativeName.localizedCaseInsensitiveContains(languageSearch) || $0.id.localizedCaseInsensitiveContains(languageSearch)
                    }) { item in
                        Button { language = item.id } label: {
                            HStack { Text(item.nativeName).foregroundStyle(p.text); Spacer(); if language == item.id { Image(systemName:"checkmark").foregroundStyle(p.accent) } }
                        }.listRowBackground(p.surface)
                    }
                }
                Section(settings.text("text_size")) {
                    HStack { Text("\(Int(scale * 100))%").foregroundStyle(p.text); Spacer(); Slider(value: $scale, in: 0.8...1.4, step: 0.05).tint(p.accent).frame(maxWidth: 190) }
                        .listRowBackground(p.surface)
                }
                Section(settings.text("app_icons")) {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                        ForEach(themes, id: \.self) { id in
                            let iconPalette = MathFlowPalette.named(id)
                            Button { iconID = id } label: {
                                HStack(spacing: 8) {
                                    ZStack {
                                        iconPalette.background
                                        MathFlowMark().fill(iconPalette.text).padding(5)
                                    }
                                    .frame(width: 30, height: 30)
                                    .clipShape(RoundedRectangle(cornerRadius: 7, style: .continuous))
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(id.capitalized).mathFlowFont(size: 12, weight: .medium)
                                        Text(iconDescription(id)).mathFlowFont(size: 9).foregroundStyle(p.muted).lineLimit(2)
                                    }
                                    Spacer(minLength: 0)
                                    if iconID == id { Image(systemName: "checkmark.circle.fill").foregroundStyle(p.accent) }
                                }
                                .foregroundStyle(p.text).padding(9).background(p.key, in: RoundedRectangle(cornerRadius: 12))
                            }
                        }
                    }.listRowBackground(p.surface)
                    Text(settings.text("icon_hint")).mathFlowFont(size: 12).foregroundStyle(p.muted).listRowBackground(p.surface)
                }
                Section(settings.text("font")) {
                    Button { showFontPicker = true } label: {
                        Label(fontName ?? settings.text("choose_font"), systemImage: "textformat")
                            .foregroundStyle(p.accent)
                    }.listRowBackground(p.surface)
                    ForEach(savedFonts) { saved in
                        Button {
                            _ = CTFontManagerRegisterFontsForURL(URL(fileURLWithPath: saved.path) as CFURL, .process, nil)
                            fontPath = saved.path
                            fontName = postScriptName(for: URL(fileURLWithPath: saved.path)) ?? saved.name
                        } label: {
                            HStack {
                                Text(saved.name).foregroundStyle(p.text)
                                Spacer()
                                if fontPath == saved.path { Image(systemName: "checkmark.circle.fill").foregroundStyle(p.accent) }
                            }
                        }.listRowBackground(p.surface)
                    }
                    if savedFonts.isEmpty {
                        Text(settings.text("no_saved_fonts")).mathFlowFont(size: 12).foregroundStyle(p.muted).listRowBackground(p.surface)
                    }
                    if fontPath != nil {
                        Button(settings.text("reset"), role: .destructive) { fontPath = nil; fontName = nil }
                            .listRowBackground(p.surface)
                    }
                    if let fontMessage { Text(fontMessage).mathFlowFont(size: 12).foregroundStyle(p.muted).listRowBackground(p.surface) }
                }
                Section(settings.text("animations")) {
                    Picker(settings.text("animation_spring"), selection: $keyStyle) {
                        ForEach(Array(keyStyles.enumerated()), id: \.offset) { entry in Text(settings.text("animation_" + entry.element.0)).tag(entry.element.0) }
                    }.listRowBackground(p.surface)
                    settingSlider(settings.text("animation_duration"), value: $keyDuration, range: 70...400, suffix: " мс", p: p)
                    settingSlider(settings.text("animation_opacity"), value: $keyOpacity, range: 0.2...1, suffix: "%", p: p, percent: true)
                    settingSlider(settings.text("animation_intensity"), value: $keyIntensity, range: 0.02...0.2, suffix: "%", p: p, percent: true)
                    Toggle(settings.text("button_color_effect"), isOn: $keyColorEffect).tint(p.accent).listRowBackground(p.surface)
                    HStack(spacing: 13) {
                        ForEach(["accent","purple","cyan","green","pink","orange"], id: \.self) { colorID in
                            Circle().fill(effectColor(for: colorID, palette: p)).frame(width: 27, height: 27)
                                .overlay(Circle().stroke(keyEffectColorID == colorID ? p.text : .clear, lineWidth: 2).padding(-3))
                                .onTapGesture { keyEffectColorID = colorID }
                        }
                    }.padding(.vertical, 5).listRowBackground(p.surface)
                    HStack {
                        Text(settings.text("animation_preview")).foregroundStyle(p.muted)
                        Spacer()
                        Button("123") {}
                            .mathFlowFont(size: 18, weight: .semibold, design: .rounded)
                            .padding(.horizontal, 22).padding(.vertical, 10)
                            .background(p.key, in: Capsule())
                            .buttonStyle(MathFlowPressStyle(
                                styleOverride: keyStyle,
                                durationOverride: keyDuration,
                                opacityOverride: keyOpacity,
                                intensityOverride: keyIntensity,
                                colorEffectOverride: keyColorEffect,
                                colorOverride: effectColor(for: keyEffectColorID, palette: p)
                            ))
                    }.listRowBackground(p.surface)
                }
                Section(settings.text("nav_animations")) {
                    Toggle(settings.text("enable_animation"), isOn: $navEnabled).tint(p.accent).listRowBackground(p.surface)
                    Picker(settings.text("animations"), selection: $navStyle) {
                        ForEach(Array(navStyles.enumerated()), id: \.offset) { entry in Text(entry.element.1).tag(entry.element.0) }
                    }.listRowBackground(p.surface)
                    Text(settings.text(navStyleDescriptionKey)).mathFlowFont(size: 12).foregroundStyle(p.muted).listRowBackground(p.surface)
                    Text(settings.text("animation_preview")).mathFlowFont(size: 12).foregroundStyle(p.muted).listRowBackground(p.surface)
                    BottomNavigation(
                        selection: $previewNavSelection,
                        previewSettings: NavPreviewSettings(enabled: navEnabled, style: navStyle, duration: navDuration, intensity: navIntensity, iconScale: navIconScale, opacity: navOpacity)
                    )
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(p.surface)
                    settingSlider(settings.text("nav_duration"), value: $navDuration, range: 100...1000, suffix: " мс", p: p)
                    settingSlider(settings.text("animation_intensity"), value: $navIntensity, range: 0...1, suffix: "%", p: p, percent: true)
                    settingSlider(settings.text("nav_icon_scale"), value: $navIconScale, range: 0...0.2, suffix: "%", p: p, percent: true)
                    settingSlider(settings.text("nav_indicator_opacity"), value: $navOpacity, range: 0...1, suffix: "%", p: p, percent: true)
                }
                Section("MathFlow") { Text("Native iOS · SwiftUI").foregroundStyle(p.muted).listRowBackground(p.surface) }
            }
            .scrollContentBackground(.hidden)
            .background(p.background)
            .navigationTitle(settings.text("settings"))
            .searchable(text: $languageSearch, prompt: settings.text("search_languages"))
            .safeAreaInset(edge: .bottom) {
                if hasChanges {
                    Button {
                        settings.themeID = theme; settings.languageID = language; settings.textScale = scale
                        settings.keyStyle = keyStyle; settings.keyDuration = min(400,max(70,keyDuration))
                        settings.keyOpacity = min(1,max(0.2,keyOpacity)); settings.keyIntensity = min(0.2,max(0.02,keyIntensity))
                        settings.keyColorEffect = keyColorEffect; settings.keyEffectColorID = keyEffectColorID; settings.navEnabled = navEnabled; settings.navStyle = navStyle
                        settings.navDuration = min(1000,max(100,navDuration)); settings.navIntensity = navIntensity
                        settings.navIconScale = navIconScale; settings.navOpacity = navOpacity; settings.applyIcon(iconID)
                        settings.fontPath = fontPath; settings.fontPostScriptName = fontName
                        withAnimation(.spring(response: 0.38, dampingFraction: 0.84)) { }
                    } label: {
                        Text(settings.text("apply")).mathFlowFont(size: 15, weight: .semibold)
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
            loadSavedFonts()
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
                loadSavedFonts()
                fontMessage = "Шрифт добавлен: \(fontName ?? source.lastPathComponent). Нажмите «Применить»."
            } catch { fontMessage = error.localizedDescription }
        }
    }

    private func themeDescription(_ id: String) -> String {
        switch id {
        case "vanilla": return "Фирменная фиолетовая палитра MathFlow."
        case "black": return "Почти чёрный интерфейс с нейтральными поверхностями."
        case "white": return "Светлая тема с чистыми белыми поверхностями."
        case "purple": return "Глубокий фиолетовый с насыщенными акцентами."
        case "ocean": return "Холодные синие оттенки и яркий голубой акцент."
        case "emerald": return "Тёмно-зелёная палитра с мятными акцентами."
        case "sunset": return "Тёплые оттенки заката: коралл, оранжевый и розовый."
        case "rose": return "Мягкая розовая тема с ярким малиновым акцентом."
        case "midnight": return "Ночная фиолетовая палитра с мягкими лавандовыми акцентами."
        default: return "Спокойные графитовые оттенки с холодным светлым акцентом."
        }
    }

    private func iconDescription(_ id: String) -> String {
        switch id {
        case "vanilla": return "Оригинальная фиолетовая иконка MathFlow."
        case "black": return "Чёрный фон и белый фирменный знак."
        case "white": return "Белый фон с фиолетовым знаком."
        case "purple": return "Тёмный фон с насыщенным фиолетовым знаком."
        case "ocean": return "Глубокий синий фон и яркий голубой знак."
        case "emerald": return "Тёмно-зелёный фон и мятный знак."
        case "sunset": return "Градиент заката с белым фирменным знаком."
        case "rose": return "Нежно-розовый фон и малиновый знак."
        case "midnight": return "Глубокий ночной фон и лавандовый знак."
        default: return "Графитовый фон и светлый холодный знак."
        }
    }

    private func loadSavedFonts() {
        let folder = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent("Fonts", isDirectory: true)
        let urls = (try? FileManager.default.contentsOfDirectory(at: folder, includingPropertiesForKeys: nil)) ?? []
        savedFonts = urls
            .filter { ["ttf", "otf", "ttc"].contains($0.pathExtension.lowercased()) }
            .map { SavedFont(path: $0.path, name: $0.deletingPathExtension().lastPathComponent) }
            .sorted { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
    }

    private func postScriptName(for url: URL) -> String? {
        let descriptors = CTFontManagerCreateFontDescriptorsFromURL(url as CFURL) as? [CTFontDescriptor]
        return descriptors?.first.flatMap { CTFontDescriptorCopyAttribute($0, kCTFontNameAttribute) as? String }
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

struct MathFlowFontModifier: ViewModifier {
    @EnvironmentObject private var settings: AppSettings
    @ScaledMetric private var dynamicSize: CGFloat
    let weight: Font.Weight
    let design: Font.Design

    init(size: CGFloat, weight: Font.Weight = .regular, design: Font.Design = .default) {
        self.weight = weight
        self.design = design
        _dynamicSize = ScaledMetric(wrappedValue: size, relativeTo: .body)
    }

    func body(content: Content) -> some View {
        let size = dynamicSize * CGFloat(settings.textScale)
        if let postScriptName = settings.fontPostScriptName {
            content.font(.custom(postScriptName, size: size)).fontWeight(weight)
        } else {
            content.font(.system(size: size, weight: weight, design: design))
        }
    }
}

extension View {
    func mathFlowFont(size: CGFloat, weight: Font.Weight = .regular, design: Font.Design = .default) -> some View {
        modifier(MathFlowFontModifier(size: size, weight: weight, design: design))
    }
}

struct MathFlowInteractionPressStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.965 : 1)
            .animation(.spring(response: 0.18, dampingFraction: 1.0, blendDuration: 0), value: configuration.isPressed)
    }
}

struct MathFlowPressStyle: ButtonStyle {
    @EnvironmentObject private var settings: AppSettings
    var styleOverride: String? = nil
    var durationOverride: Double? = nil
    var opacityOverride: Double? = nil
    var intensityOverride: Double? = nil
    var colorEffectOverride: Bool? = nil
    var colorOverride: Color? = nil

    func makeBody(configuration: Configuration) -> some View {
        let style = styleOverride ?? settings.keyStyle
        let amount = CGFloat(intensityOverride ?? settings.keyIntensity)
        let durationValue = durationOverride ?? settings.keyDuration
        let opacityValue = opacityOverride ?? settings.keyOpacity
        let colorEffectEnabled = colorEffectOverride ?? settings.keyColorEffect
        let effectColor = colorOverride ?? settings.effectColor
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
        let duration = durationValue / 1000
        let isSpring = ["spring", "bounce", "elastic"].contains(style)
        let effectAlpha = style == "flash" ? opacityValue : style == "ripple" ? opacityValue * 0.34 : opacityValue * 0.22

        return configuration.label
            .scaleEffect(x: scales.0, y: scales.1, anchor: .center)
            .overlay {
                if pressed && colorEffectEnabled {
                    if style == "ripple" {
                        Circle()
                            .fill(effectColor.opacity(effectAlpha))
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

struct SavedFont: Identifiable {
    let path: String
    let name: String
    var id: String { path }
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
