import SwiftUI

struct MathFlowMark: Shape {
    func path(in rect: CGRect) -> Path {
        let points: [(CGFloat, CGFloat)] = [
            (359,390),(345,412),(338,440),(338,1111),(348,1143),(364,1162),(400,1179),
            (441,1174),(469,1152),(483,1122),(485,638),(702,1060),(726,1084),(762,1096),
            (809,1085),(836,1057),(1050,640),(1053,1125),(1077,1163),(1107,1178),
            (1147,1176),(1179,1155),(1191,1137),(1198,1109),(1198,441),(1191,412),
            (1167,379),(1132,360),(1098,357),(1060,369),(1033,392),(768,895),(503,390),
            (478,369),(439,357),(394,364)
        ]
        return polygon(points, in: rect)
    }
}

struct MathFlowDot: Shape {
    func path(in rect: CGRect) -> Path {
        let points: [(CGFloat, CGFloat)] = [
            (761,404),(739,410),(713,431),(700,459),(699,481),(709,509),(725,527),
            (752,540),(772,542),(797,536),(821,518),(835,492),(837,463),(825,433),
            (808,416),(786,406)
        ]
        return polygon(points, in: rect)
    }
}

private func polygon(_ points: [(CGFloat, CGFloat)], in rect: CGRect) -> Path {
    var path = Path()
    guard let first = points.first else { return path }
    func point(_ value: (CGFloat, CGFloat)) -> CGPoint {
        CGPoint(x: rect.minX + value.0 / 1536 * rect.width,
                y: rect.minY + value.1 / 1536 * rect.height)
    }
    path.move(to: point(first))
    for value in points.dropFirst() { path.addLine(to: point(value)) }
    path.closeSubpath()
    return path
}
