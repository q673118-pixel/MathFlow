import SwiftUI

struct MathFlowPalette {
    let background: Color
    let surface: Color
    let key: Color
    let keyAccent: Color
    let keyEquals: Color
    let onKeyEquals: Color
    let text: Color
    let muted: Color
    let accent: Color
    let navActive: Color
    let error: Color
    let isDark: Bool

    static func named(_ id: String) -> MathFlowPalette {
        switch id {
        case "black": return .init(background: 0x08090D, surface: 0x12141A, key: 0x20232B, keyAccent: 0x343945, keyEquals: 0xB7A0FF, onKeyEquals: 0x1A1327, text: 0xF4F2FA, muted: 0x9296A3, accent: 0xC9B8FF, navActive: 0x252833, error: 0xFF8A80, isDark: true)
        case "white": return .init(background: 0xF7F6FA, surface: 0xFFFFFF, key: 0xECEAF2, keyAccent: 0xE1D8F3, keyEquals: 0x6A1B9A, onKeyEquals: 0xFFFFFF, text: 0x17131F, muted: 0x686274, accent: 0x7428B2, navActive: 0xE8DDF5, error: 0xB3261E, isDark: false)
        case "purple": return .init(background: 0x10051B, surface: 0x1C0C2B, key: 0x2C1240, keyAccent: 0x511C70, keyEquals: 0x8A2CC2, onKeyEquals: 0xFFFFFF, text: 0xF5E9FF, muted: 0xB59BBF, accent: 0xD49AFF, navActive: 0x321343, error: 0xFF8A80, isDark: true)
        case "ocean": return .init(background: 0x04142F, surface: 0x081E43, key: 0x102D58, keyAccent: 0x075C9B, keyEquals: 0x139EE0, onKeyEquals: 0xFFFFFF, text: 0xEAF7FF, muted: 0x8FB7D4, accent: 0x39C7FF, navActive: 0x0B3157, error: 0xFF8A80, isDark: true)
        case "emerald": return .init(background: 0x031D17, surface: 0x082B22, key: 0x104336, keyAccent: 0x14664C, keyEquals: 0x13A67A, onKeyEquals: 0xFFFFFF, text: 0xE8FFF6, muted: 0x90C9B3, accent: 0x4BE2B2, navActive: 0x0D3A2C, error: 0xFF8A80, isDark: true)
        case "sunset": return .init(background: 0x24101C, surface: 0x351729, key: 0x4C2035, keyAccent: 0xB34B37, keyEquals: 0xFF6D49, onKeyEquals: 0xFFFFFF, text: 0xFFF2EA, muted: 0xD3A6A0, accent: 0xFF9A66, navActive: 0x51243B, error: 0xFFB4AB, isDark: true)
        case "rose": return .init(background: 0xFFF3F8, surface: 0xFFFFFF, key: 0xF8DDEB, keyAccent: 0xF5BBD5, keyEquals: 0xE50070, onKeyEquals: 0xFFFFFF, text: 0x39182B, muted: 0x9C6B83, accent: 0xD60069, navActive: 0xF9D7E9, error: 0xB3261E, isDark: false)
        case "midnight": return .init(background: 0x100521, surface: 0x1A0B31, key: 0x2A124A, keyAccent: 0x6331A1, keyEquals: 0xA66BFF, onKeyEquals: 0xFFFFFF, text: 0xF6EDFF, muted: 0xB5A0D1, accent: 0xC69AFF, navActive: 0x321957, error: 0xFF8A80, isDark: true)
        case "graphite": return .init(background: 0x222431, surface: 0x2D3040, key: 0x3A3D4F, keyAccent: 0x4A4D62, keyEquals: 0xAEB4D8, onKeyEquals: 0x222431, text: 0xF1F2FF, muted: 0xB0B4C9, accent: 0xD1D5FF, navActive: 0x3C4056, error: 0xFF8A80, isDark: true)
        default: return .init(background: 0x0C0B10, surface: 0x16141C, key: 0x211F29, keyAccent: 0x54447F, keyEquals: 0x6A5AA3, onKeyEquals: 0xFFFFFF, text: 0xE9E5F3, muted: 0x9A94AB, accent: 0xB9A8F0, navActive: 0x2E2942, error: 0xFF8A80, isDark: true)
        }
    }
}

private extension MathFlowPalette {
    init(background: UInt32, surface: UInt32, key: UInt32, keyAccent: UInt32, keyEquals: UInt32, onKeyEquals: UInt32, text: UInt32, muted: UInt32, accent: UInt32, navActive: UInt32, error: UInt32, isDark: Bool) {
        self.background = Color(hex: background); self.surface = Color(hex: surface); self.key = Color(hex: key)
        self.keyAccent = Color(hex: keyAccent); self.keyEquals = Color(hex: keyEquals); self.onKeyEquals = Color(hex: onKeyEquals)
        self.text = Color(hex: text); self.muted = Color(hex: muted); self.accent = Color(hex: accent)
        self.navActive = Color(hex: navActive); self.error = Color(hex: error); self.isDark = isDark
    }
}

private extension Color {
    init(hex: UInt32) {
        self.init(.sRGB, red: Double((hex >> 16) & 0xFF) / 255, green: Double((hex >> 8) & 0xFF) / 255, blue: Double(hex & 0xFF) / 255, opacity: 1)
    }
}
