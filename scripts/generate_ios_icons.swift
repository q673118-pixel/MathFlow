import AppKit
import Foundation

struct Variant {
    let id: String
    let appIconName: String
    var launcherName: String { "launcher_\(id)" }
    var markName: String {
        switch id {
        case "black": return "mark_black"
        case "white", "purple": return "mark_\(id)"
        case "ocean": return "mark_ocean"
        case "emerald": return "mark_emerald"
        case "sunset": return "mark_sunset"
        case "rose": return "mark_rose"
        case "midnight": return "mark_midnight"
        case "graphite": return "mark_graphite"
        default: return "mark_vanilla"
        }
    }
}

let variants = [
    Variant(id: "vanilla", appIconName: "AppIcon"),
    Variant(id: "black", appIconName: "AppIconBlack"),
    Variant(id: "white", appIconName: "AppIconWhite"),
    Variant(id: "purple", appIconName: "AppIconPurple"),
    Variant(id: "ocean", appIconName: "AppIconOcean"),
    Variant(id: "emerald", appIconName: "AppIconEmerald"),
    Variant(id: "sunset", appIconName: "AppIconSunset"),
    Variant(id: "rose", appIconName: "AppIconRose"),
    Variant(id: "midnight", appIconName: "AppIconMidnight"),
    Variant(id: "graphite", appIconName: "AppIconGraphite")
]

let root = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
let drawable = root.appendingPathComponent("app/src/main/res/drawable")
let assets = root.appendingPathComponent("ios/MathFlow/Assets.xcassets")

func text(_ name: String) throws -> String {
    try String(contentsOf: drawable.appendingPathComponent("\(name).xml"), encoding: .utf8)
}

func matches(_ pattern: String, _ source: String) -> [String] {
    guard let regex = try? NSRegularExpression(pattern: pattern, options: [.dotMatchesLineSeparators]) else { return [] }
    let ns = source as NSString
    return regex.matches(in: source, range: NSRange(location: 0, length: ns.length)).map { ns.substring(with: $0.range) }
}

func capture(_ pattern: String, _ source: String) -> String? {
    guard let regex = try? NSRegularExpression(pattern: pattern, options: [.dotMatchesLineSeparators]),
          let match = regex.firstMatch(in: source, range: NSRange(location: 0, length: (source as NSString).length)),
          match.numberOfRanges > 1 else { return nil }
    return (source as NSString).substring(with: match.range(at: 1))
}

func color(_ value: String) -> NSColor {
    let raw = value.replacingOccurrences(of: "#", with: "")
    let hex = UInt64(raw, radix: 16) ?? 0xFFFFFFFF
    if raw.count == 8 {
        return NSColor(calibratedRed: CGFloat((hex >> 16) & 255) / 255,
                       green: CGFloat((hex >> 8) & 255) / 255,
                       blue: CGFloat(hex & 255) / 255,
                       alpha: CGFloat((hex >> 24) & 255) / 255)
    }
    return NSColor(calibratedRed: CGFloat((hex >> 16) & 255) / 255,
                   green: CGFloat((hex >> 8) & 255) / 255,
                   blue: CGFloat(hex & 255) / 255, alpha: 1)
}

func makePath(_ data: String) -> CGPath? {
    let numbers = matches("[+-]?(?:[0-9]+\\.?[0-9]*|\\.[0-9]+)", data)
        .compactMap { Double($0) }
    guard numbers.count >= 6, numbers.count % 2 == 0 else { return nil }
    let path = CGMutablePath()
    path.move(to: CGPoint(x: numbers[0], y: numbers[1]))
    var index = 2
    while index + 1 < numbers.count {
        path.addLine(to: CGPoint(x: numbers[index], y: numbers[index + 1]))
        index += 2
    }
    path.closeSubpath()
    return path
}

func render(_ variant: Variant) throws {
    let launcherXML = try text(variant.launcherName)
    let markXML = try text(variant.markName)
    let image = NSImage(size: NSSize(width: 1024, height: 1024))
    image.lockFocus()
    let rect = NSRect(x: 0, y: 0, width: 1024, height: 1024)

    if let start = capture("startColor=\"#([0-9A-Fa-f]{6}(?:[0-9A-Fa-f]{2})?)\"", launcherXML),
       let middle = capture("centerColor=\"#([0-9A-Fa-f]{6}(?:[0-9A-Fa-f]{2})?)\"", launcherXML),
       let end = capture("endColor=\"#([0-9A-Fa-f]{6}(?:[0-9A-Fa-f]{2})?)\"", launcherXML) {
        NSGradient(colors: [color("#" + start), color("#" + middle), color("#" + end)])?
            .draw(in: rect, angle: 45)
    } else if let solid = capture("solid android:color=\"#([0-9A-Fa-f]{6}(?:[0-9A-Fa-f]{2})?)\"", launcherXML) {
        color("#" + solid).setFill()
        rect.fill()
    } else {
        NSColor.black.setFill()
        rect.fill()
    }

    if let context = NSGraphicsContext.current?.cgContext {
        context.saveGState()
        context.translateBy(x: 0, y: 1024)
        context.scaleBy(x: 1024.0 / 1536.0, y: -1024.0 / 1536.0)
        let tags = matches("<path[^>]*>", markXML)
        for tag in tags {
            guard let data = capture("pathData=\"([^\"]+)\"", tag),
                  let fill = capture("fillColor=\"#([0-9A-Fa-f]{6}(?:[0-9A-Fa-f]{2})?)\"", tag),
                  let path = makePath(data) else { continue }
            context.addPath(path)
            context.setFillColor(color("#" + fill).cgColor)
            context.fillPath()
        }
        context.restoreGState()
    }
    image.unlockFocus()

    guard let tiff = image.tiffRepresentation,
          let bitmap = NSBitmapImageRep(data: tiff),
          let png = bitmap.representation(using: .png, properties: [:]) else {
        throw NSError(domain: "MathFlowIconGenerator", code: 1)
    }
    let folder = assets.appendingPathComponent("\(variant.appIconName).appiconset", isDirectory: true)
    try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
    try png.write(to: folder.appendingPathComponent("\(variant.appIconName).png"), options: .atomic)
    print("Generated \(variant.appIconName)")
}

for variant in variants { try render(variant) }
