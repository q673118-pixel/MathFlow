package com.mathflow.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

internal data class AppLanguage(
    val tag: String,
    val nativeName: String,
    val englishName: String
)

internal val AppLanguages = listOf(
    AppLanguage("ru", "Русский", "Russian"),
    AppLanguage("en", "English", "English"),
    AppLanguage("kk", "Қазақша", "Kazakh"),
    AppLanguage("es", "Español", "Spanish"),
    AppLanguage("fr", "Français", "French"),
    AppLanguage("de", "Deutsch", "German"),
    AppLanguage("pt", "Português", "Portuguese"),
    AppLanguage("it", "Italiano", "Italian"),
    AppLanguage("tr", "Türkçe", "Turkish"),
    AppLanguage("ar", "العربية", "Arabic"),
    AppLanguage("hi", "हिन्दी", "Hindi"),
    AppLanguage("zh", "简体中文", "Chinese (Simplified)"),
    AppLanguage("ja", "日本語", "Japanese"),
    AppLanguage("ko", "한국어", "Korean"),
    AppLanguage("uz", "O‘zbek", "Uzbek")
)

internal val LocalLanguage = staticCompositionLocalOf { "ru" }

private val uiText: Map<String, Map<String, String>> = mapOf(
    "home" to mapOf("ru" to "Главная", "en" to "Home", "kk" to "Басты бет", "es" to "Inicio", "fr" to "Accueil", "de" to "Start", "pt" to "Início", "it" to "Home", "tr" to "Ana sayfa", "ar" to "الرئيسية", "hi" to "होम", "zh" to "主页", "ja" to "ホーム", "ko" to "홈", "uz" to "Bosh sahifa"),
    "tools" to mapOf("ru" to "Инструменты", "en" to "Tools", "kk" to "Құралдар", "es" to "Herramientas", "fr" to "Outils", "de" to "Werkzeuge", "pt" to "Ferramentas", "it" to "Strumenti", "tr" to "Araçlar", "ar" to "الأدوات", "hi" to "टूल", "zh" to "工具", "ja" to "ツール", "ko" to "도구", "uz" to "Asboblar"),
    "solver" to mapOf("ru" to "Решатель", "en" to "Equation solver", "kk" to "Теңдеу шешуші", "es" to "Ecuaciones", "fr" to "Résolution", "de" to "Gleichungslöser", "pt" to "Equações", "it" to "Equazioni", "tr" to "Denklem çözücü", "ar" to "حل المعادلات", "hi" to "समीकरण हल", "zh" to "方程求解", "ja" to "方程式ソルバー", "ko" to "방정식 풀이", "uz" to "Tenglama yechish"),
    "history" to mapOf("ru" to "История", "en" to "History", "kk" to "Тарих", "es" to "Historial", "fr" to "Historique", "de" to "Verlauf", "pt" to "Histórico", "it" to "Cronologia", "tr" to "Geçmiş", "ar" to "السجل", "hi" to "इतिहास", "zh" to "历史记录", "ja" to "履歴", "ko" to "기록", "uz" to "Tarix"),
    "settings" to mapOf("ru" to "Настройки", "en" to "Settings", "kk" to "Баптаулар", "es" to "Ajustes", "fr" to "Paramètres", "de" to "Einstellungen", "pt" to "Configurações", "it" to "Impostazioni", "tr" to "Ayarlar", "ar" to "الإعدادات", "hi" to "सेटिंग", "zh" to "设置", "ja" to "設定", "ko" to "설정", "uz" to "Sozlamalar"),
    "app_icons" to mapOf("ru" to "Иконки приложения", "en" to "App icons", "kk" to "Қолданба белгішелері", "es" to "Iconos de la aplicación", "fr" to "Icônes de l’application", "de" to "App-Symbole", "pt" to "Ícones do app", "it" to "Icone dell’app", "tr" to "Uygulama simgeleri", "ar" to "أيقونات التطبيق", "hi" to "ऐप आइकन", "zh" to "应用图标", "ja" to "アプリアイコン", "ko" to "앱 아이콘", "uz" to "Ilova ikonkalari"),
    "app_language" to mapOf("ru" to "Язык приложения", "en" to "App language", "kk" to "Қолданба тілі", "es" to "Idioma de la aplicación", "fr" to "Langue de l’application", "de" to "App-Sprache", "pt" to "Idioma do app", "it" to "Lingua dell’app", "tr" to "Uygulama dili", "ar" to "لغة التطبيق", "hi" to "ऐप की भाषा", "zh" to "应用语言", "ja" to "アプリの言語", "ko" to "앱 언어", "uz" to "Ilova tili"),
    "themes" to mapOf("ru" to "Темы оформления", "en" to "Themes", "kk" to "Тақырыптар", "es" to "Temas", "fr" to "Thèmes", "de" to "Designs", "pt" to "Temas", "it" to "Temi", "tr" to "Temalar", "ar" to "السمات", "hi" to "थीम", "zh" to "主题", "ja" to "テーマ", "ko" to "테마", "uz" to "Mavzular"),
    "history_empty" to mapOf("ru" to "Здесь появятся результаты основного калькулятора.", "en" to "Your calculator results will appear here.", "kk" to "Мұнда калькулятор нәтижелері көрсетіледі.", "es" to "Aquí aparecerán los resultados de la calculadora.", "fr" to "Les résultats du calculateur apparaîtront ici.", "de" to "Hier erscheinen deine Rechnergebnisse.", "pt" to "Os resultados aparecerão aqui.", "it" to "Qui appariranno i risultati.", "tr" to "Hesaplama sonuçların burada görünür.", "ar" to "ستظهر نتائج الحساب هنا.", "hi" to "गणना के परिणाम यहाँ दिखेंगे।", "zh" to "计算结果将显示在这里。", "ja" to "計算結果がここに表示されます。", "ko" to "계산 결과가 여기에 표시됩니다.", "uz" to "Hisoblash natijalari shu yerda ko‘rinadi."),
    "clear_history" to mapOf("ru" to "Очистить историю", "en" to "Clear history", "kk" to "Тарихты тазарту", "es" to "Borrar historial", "fr" to "Effacer l’historique", "de" to "Verlauf löschen", "pt" to "Limpar histórico", "it" to "Cancella cronologia", "tr" to "Geçmişi temizle", "ar" to "مسح السجل", "hi" to "इतिहास मिटाएँ", "zh" to "清除历史记录", "ja" to "履歴を消去", "ko" to "기록 지우기", "uz" to "Tarixni tozalash"),
    "calculate" to mapOf("ru" to "Вычислить", "en" to "Calculate", "kk" to "Есептеу", "es" to "Calcular", "fr" to "Calculer", "de" to "Berechnen", "pt" to "Calcular", "it" to "Calcola", "tr" to "Hesapla", "ar" to "احسب", "hi" to "गणना करें", "zh" to "计算", "ja" to "計算", "ko" to "계산", "uz" to "Hisoblash"),
    "solve" to mapOf("ru" to "Решить", "en" to "Solve", "kk" to "Шешу", "es" to "Resolver", "fr" to "Résoudre", "de" to "Lösen", "pt" to "Resolver", "it" to "Risolvi", "tr" to "Çöz", "ar" to "حل", "hi" to "हल करें", "zh" to "求解", "ja" to "解く", "ko" to "풀기", "uz" to "Yechish"),
    "font" to mapOf("ru" to "Шрифт", "en" to "Font", "kk" to "Қаріп", "es" to "Fuente", "fr" to "Police", "de" to "Schriftart", "pt" to "Fonte", "it" to "Carattere", "tr" to "Yazı tipi", "ar" to "الخط", "hi" to "फ़ॉन्ट", "zh" to "字体", "ja" to "フォント", "ko" to "글꼴", "uz" to "Shrift"),
    "choose_font" to mapOf("ru" to "Выбрать TTF / OTF", "en" to "Choose TTF / OTF", "kk" to "TTF / OTF таңдау", "es" to "Elegir TTF / OTF", "fr" to "Choisir TTF / OTF", "de" to "TTF / OTF auswählen", "pt" to "Escolher TTF / OTF", "it" to "Scegli TTF / OTF", "tr" to "TTF / OTF seç", "ar" to "اختر TTF / OTF", "hi" to "TTF / OTF चुनें", "zh" to "选择 TTF / OTF", "ja" to "TTF / OTF を選択", "ko" to "TTF / OTF 선택", "uz" to "TTF / OTF tanlash"),
    "reset" to mapOf("ru" to "Сброс", "en" to "Reset", "kk" to "Қалпына келтіру", "es" to "Restablecer", "fr" to "Réinitialiser", "de" to "Zurücksetzen", "pt" to "Redefinir", "it" to "Ripristina", "tr" to "Sıfırla", "ar" to "إعادة ضبط", "hi" to "रीसेट", "zh" to "重置", "ja" to "リセット", "ko" to "초기화", "uz" to "Tiklash"),
    "text_size" to mapOf("ru" to "Размер текста", "en" to "Text size", "kk" to "Мәтін өлшемі", "es" to "Tamaño del texto", "fr" to "Taille du texte", "de" to "Textgröße", "pt" to "Tamanho do texto", "it" to "Dimensione testo", "tr" to "Metin boyutu", "ar" to "حجم النص", "hi" to "टेक्स्ट आकार", "zh" to "文字大小", "ja" to "文字サイズ", "ko" to "텍스트 크기", "uz" to "Matn o‘lchami"),
    "language_hint" to mapOf("ru" to "Выберите язык интерфейса. Выбор сохраняется на устройстве.", "en" to "Choose the interface language. Your choice is saved on this device.", "kk" to "Интерфейс тілін таңдаңыз. Таңдау құрылғыда сақталады.", "es" to "Elige el idioma de la interfaz. La selección se guarda en este dispositivo.", "fr" to "Choisissez la langue de l’interface. Votre choix est enregistré sur cet appareil.", "de" to "Wähle die Sprache der Oberfläche. Die Auswahl wird auf diesem Gerät gespeichert.", "pt" to "Escolha o idioma da interface. A escolha fica salva neste dispositivo.", "it" to "Scegli la lingua dell’interfaccia. La scelta viene salvata sul dispositivo.", "tr" to "Arayüz dilini seçin. Seçiminiz bu cihazda saklanır.", "ar" to "اختر لغة الواجهة. سيتم حفظ اختيارك على هذا الجهاز.", "hi" to "इंटरफ़ेस की भाषा चुनें। चयन इस डिवाइस पर सहेजा जाएगा।", "zh" to "选择界面语言。你的选择将保存在此设备上。", "ja" to "インターフェースの言語を選択します。選択内容は端末に保存されます。", "ko" to "인터페이스 언어를 선택하세요. 선택한 언어는 이 기기에 저장됩니다.", "uz" to "Interfeys tilini tanlang. Tanlov ushbu qurilmada saqlanadi."),
    "theme_hint" to mapOf("ru" to "Тема не зависит от выбранной иконки приложения.", "en" to "The theme is independent of the selected app icon.", "kk" to "Тақырып қолданба белгішесіне тәуелді емес.", "es" to "El tema es independiente del icono de la aplicación.", "fr" to "Le thème est indépendant de l’icône de l’application.", "de" to "Das Design ist unabhängig vom App-Symbol.", "pt" to "O tema é independente do ícone do aplicativo.", "it" to "Il tema è indipendente dall’icona dell’app.", "tr" to "Tema, seçilen uygulama simgesinden bağımsızdır.", "ar" to "السمة مستقلة عن أيقونة التطبيق المحددة.", "hi" to "थीम चुने गए ऐप आइकन से स्वतंत्र है।", "zh" to "主题与所选应用图标无关。", "ja" to "テーマは選択したアプリアイコンとは独立しています。", "ko" to "테마는 선택한 앱 아이콘과 별개입니다.", "uz" to "Mavzu tanlangan ilova ikonkasi bilan bog‘liq emas."),
    "icon_hint" to mapOf("ru" to "Выберите вариант. Изменения вступят в силу после нажатия «Применить».", "en" to "Choose an option. Changes take effect after tapping Apply.", "kk" to "Нұсқаны таңдаңыз. Өзгерістер «Қолдану» батырмасын басқаннан кейін күшіне енеді.", "es" to "Elige una opción. Los cambios se aplicarán al tocar Aplicar.", "fr" to "Choisissez une option. Les modifications seront appliquées après avoir appuyé sur Appliquer.", "de" to "Wähle eine Option. Änderungen werden nach Tippen auf Anwenden übernommen.", "pt" to "Escolha uma opção. As alterações serão aplicadas ao tocar em Aplicar.", "it" to "Scegli un’opzione. Le modifiche verranno applicate toccando Applica.", "tr" to "Bir seçenek belirleyin. Değişiklikler Uygula'ya dokununca etkinleşir.", "ar" to "اختر خيارًا. ستُطبق التغييرات بعد الضغط على تطبيق.", "hi" to "विकल्प चुनें। लागू करें दबाने पर बदलाव प्रभावी होंगे।", "zh" to "选择一个选项。点击“应用”后更改生效。", "ja" to "オプションを選択してください。「適用」をタップすると反映されます。", "ko" to "옵션을 선택하세요. 적용을 누르면 변경 사항이 반영됩니다.", "uz" to "Variantni tanlang. O‘zgarishlar Qo‘llash tugmasi bosilgach kuchga kiradi."),
    "steps" to mapOf("ru" to "Решение", "en" to "Steps", "kk" to "Шешімі", "es" to "Pasos", "fr" to "Étapes", "de" to "Schritte", "pt" to "Etapas", "it" to "Passaggi", "tr" to "Çözüm adımları", "ar" to "الخطوات", "hi" to "चरण", "zh" to "步骤", "ja" to "手順", "ko" to "풀이 단계", "uz" to "Bosqichlar"),
    "delete" to mapOf("ru" to "Удалить", "en" to "Delete", "kk" to "Жою", "es" to "Eliminar", "fr" to "Supprimer", "de" to "Löschen", "pt" to "Excluir", "it" to "Elimina", "tr" to "Sil", "ar" to "حذف", "hi" to "हटाएँ", "zh" to "删除", "ja" to "削除", "ko" to "삭제", "uz" to "O‘chirish"),
    "error" to mapOf("ru" to "Ошибка", "en" to "Error", "kk" to "Қате", "es" to "Error", "fr" to "Erreur", "de" to "Fehler", "pt" to "Erro", "it" to "Errore", "tr" to "Hata", "ar" to "خطأ", "hi" to "त्रुटि", "zh" to "错误", "ja" to "エラー", "ko" to "오류", "uz" to "Xato"),
    "fractions" to mapOf("ru" to "Дроби", "en" to "Fractions", "kk" to "Бөлшектер", "es" to "Fracciones", "fr" to "Fractions", "de" to "Brüche", "pt" to "Frações", "it" to "Frazioni", "tr" to "Kesirler", "ar" to "الكسور", "hi" to "भिन्न", "zh" to "分数", "ja" to "分数", "ko" to "분수", "uz" to "Kasrlar"),
    "percentages" to mapOf("ru" to "Проценты", "en" to "Percentages", "kk" to "Пайыздар", "es" to "Porcentajes", "fr" to "Pourcentages", "de" to "Prozentrechnung", "pt" to "Porcentagens", "it" to "Percentuali", "tr" to "Yüzdeler", "ar" to "النسب المئوية", "hi" to "प्रतिशत", "zh" to "百分比", "ja" to "パーセント", "ko" to "백분율", "uz" to "Foizlar"),
    "geometry" to mapOf("ru" to "Геометрия", "en" to "Geometry", "kk" to "Геометрия", "es" to "Geometría", "fr" to "Géométrie", "de" to "Geometrie", "pt" to "Geometria", "it" to "Geometria", "tr" to "Geometri", "ar" to "الهندسة", "hi" to "ज्यामिति", "zh" to "几何", "ja" to "幾何", "ko" to "기하학", "uz" to "Geometriya"),
    "converter" to mapOf("ru" to "Конвертер", "en" to "Converter", "kk" to "Түрлендіргіш", "es" to "Conversor", "fr" to "Convertisseur", "de" to "Umrechner", "pt" to "Conversor", "it" to "Convertitore", "tr" to "Dönüştürücü", "ar" to "المحوّل", "hi" to "कन्वर्टर", "zh" to "换算器", "ja" to "変換", "ko" to "변환기", "uz" to "Konvertor"),
    "percent_of" to mapOf("ru" to "% от числа", "en" to "% of a number", "kk" to "Санның пайызы", "es" to "% de un número", "fr" to "% d’un nombre", "de" to "% einer Zahl", "pt" to "% de um número", "it" to "% di un numero", "tr" to "Sayının yüzdesi", "ar" to "٪ من عدد", "hi" to "संख्या का %", "zh" to "数值的百分比", "ja" to "数値の％", "ko" to "숫자의 %", "uz" to "Sonning foizi"),
    "what_percent" to mapOf("ru" to "Сколько %", "en" to "What percent", "kk" to "Неше пайыз", "es" to "Qué porcentaje", "fr" to "Quel pourcentage", "de" to "Wie viel Prozent", "pt" to "Qual porcentagem", "it" to "Quanti percento", "tr" to "Yüzde kaç", "ar" to "كم النسبة", "hi" to "कितने %", "zh" to "占百分之几", "ja" to "何パーセント", "ko" to "몇 퍼센트", "uz" to "Necha foiz"),
    "change_percent" to mapOf("ru" to "± на %", "en" to "Change by %", "kk" to "%-ға өзгерту", "es" to "Cambiar en %", "fr" to "Variation en %", "de" to "Um % ändern", "pt" to "Alterar em %", "it" to "Variazione %", "tr" to "% değiştir", "ar" to "التغيير بنسبة ٪", "hi" to "% से बदलें", "zh" to "按百分比变化", "ja" to "％で増減", "ko" to "% 변경", "uz" to "% ga o‘zgartirish"),
    "rectangle" to mapOf("ru" to "Прямоугольник", "en" to "Rectangle", "kk" to "Тіктөртбұрыш", "es" to "Rectángulo", "fr" to "Rectangle", "de" to "Rechteck", "pt" to "Retângulo", "it" to "Rettangolo", "tr" to "Dikdörtgen", "ar" to "مستطيل", "hi" to "आयत", "zh" to "矩形", "ja" to "長方形", "ko" to "직사각형", "uz" to "To‘g‘ri to‘rtburchak"),
    "circle" to mapOf("ru" to "Круг", "en" to "Circle", "kk" to "Шеңбер", "es" to "Círculo", "fr" to "Cercle", "de" to "Kreis", "pt" to "Círculo", "it" to "Cerchio", "tr" to "Daire", "ar" to "دائرة", "hi" to "वृत्त", "zh" to "圆形", "ja" to "円", "ko" to "원", "uz" to "Doira"),
    "triangle" to mapOf("ru" to "Треугольник", "en" to "Triangle", "kk" to "Үшбұрыш", "es" to "Triángulo", "fr" to "Triangle", "de" to "Dreieck", "pt" to "Triângulo", "it" to "Triangolo", "tr" to "Üçgen", "ar" to "مثلث", "hi" to "त्रिभुज", "zh" to "三角形", "ja" to "三角形", "ko" to "삼각형", "uz" to "Uchburchak"),
    "fraction_one" to mapOf("ru" to "Дробь 1", "en" to "Fraction 1", "kk" to "Бөлшек 1", "es" to "Fracción 1", "fr" to "Fraction 1", "de" to "Bruch 1", "pt" to "Fração 1", "it" to "Frazione 1", "tr" to "Kesir 1", "ar" to "الكسر 1", "hi" to "भिन्न 1", "zh" to "分数 1", "ja" to "分数 1", "ko" to "분수 1", "uz" to "Kasr 1"),
    "fraction_two" to mapOf("ru" to "Дробь 2", "en" to "Fraction 2", "kk" to "Бөлшек 2", "es" to "Fracción 2", "fr" to "Fraction 2", "de" to "Bruch 2", "pt" to "Fração 2", "it" to "Frazione 2", "tr" to "Kesir 2", "ar" to "الكسر 2", "hi" to "भिन्न 2", "zh" to "分数 2", "ja" to "分数 2", "ko" to "분수 2", "uz" to "Kasr 2"),
    "part" to mapOf("ru" to "Часть", "en" to "Part", "kk" to "Бөлік", "es" to "Parte", "fr" to "Partie", "de" to "Teil", "pt" to "Parte", "it" to "Parte", "tr" to "Parça", "ar" to "الجزء", "hi" to "भाग", "zh" to "部分", "ja" to "部分", "ko" to "부분", "uz" to "Qism"),
    "whole" to mapOf("ru" to "Целое", "en" to "Whole", "kk" to "Бүтін", "es" to "Total", "fr" to "Total", "de" to "Ganzes", "pt" to "Total", "it" to "Intero", "tr" to "Bütün", "ar" to "الكل", "hi" to "पूर्ण", "zh" to "整体", "ja" to "全体", "ko" to "전체", "uz" to "Butun"),
    "percent" to mapOf("ru" to "Процент", "en" to "Percent", "kk" to "Пайыз", "es" to "Porcentaje", "fr" to "Pourcentage", "de" to "Prozent", "pt" to "Porcentagem", "it" to "Percentuale", "tr" to "Yüzde", "ar" to "النسبة المئوية", "hi" to "प्रतिशत", "zh" to "百分比", "ja" to "パーセント", "ko" to "퍼센트", "uz" to "Foiz"),
    "number" to mapOf("ru" to "Число", "en" to "Number", "kk" to "Сан", "es" to "Número", "fr" to "Nombre", "de" to "Zahl", "pt" to "Número", "it" to "Numero", "tr" to "Sayı", "ar" to "عدد", "hi" to "संख्या", "zh" to "数字", "ja" to "数値", "ko" to "숫자", "uz" to "Son"),
    "radius" to mapOf("ru" to "Радиус", "en" to "Radius", "kk" to "Радиус", "es" to "Radio", "fr" to "Rayon", "de" to "Radius", "pt" to "Raio", "it" to "Raggio", "tr" to "Yarıçap", "ar" to "نصف القطر", "hi" to "त्रिज्या", "zh" to "半径", "ja" to "半径", "ko" to "반지름", "uz" to "Radius"),
    "side_a" to mapOf("ru" to "Сторона a", "en" to "Side a", "kk" to "a қабырғасы", "es" to "Lado a", "fr" to "Côté a", "de" to "Seite a", "pt" to "Lado a", "it" to "Lato a", "tr" to "Kenar a", "ar" to "الضلع a", "hi" to "भुजा a", "zh" to "边 a", "ja" to "辺 a", "ko" to "변 a", "uz" to "a tomoni"),
    "side_b" to mapOf("ru" to "Сторона b", "en" to "Side b", "kk" to "b қабырғасы", "es" to "Lado b", "fr" to "Côté b", "de" to "Seite b", "pt" to "Lado b", "it" to "Lato b", "tr" to "Kenar b", "ar" to "الضلع b", "hi" to "भुजा b", "zh" to "边 b", "ja" to "辺 b", "ko" to "변 b", "uz" to "b tomoni"),
    "side_c" to mapOf("ru" to "Сторона c", "en" to "Side c", "kk" to "c қабырғасы", "es" to "Lado c", "fr" to "Côté c", "de" to "Seite c", "pt" to "Lado c", "it" to "Lato c", "tr" to "Kenar c", "ar" to "الضلع c", "hi" to "भुजा c", "zh" to "边 c", "ja" to "辺 c", "ko" to "변 c", "uz" to "c tomoni"),
    "applied" to mapOf("ru" to "Применяется", "en" to "Applied", "kk" to "Қолданылған", "es" to "Aplicado", "fr" to "Appliqué", "de" to "Wird verwendet", "pt" to "Aplicado", "it" to "Applicato", "tr" to "Uygulandı", "ar" to "مُطبّق", "hi" to "लागू", "zh" to "已应用", "ja" to "適用中", "ko" to "적용됨", "uz" to "Qo‘llanmoqda"),
    "selected" to mapOf("ru" to "Выбрана", "en" to "Selected", "kk" to "Таңдалған", "es" to "Seleccionado", "fr" to "Sélectionné", "de" to "Ausgewählt", "pt" to "Selecionado", "it" to "Selezionato", "tr" to "Seçildi", "ar" to "محدد", "hi" to "चुना गया", "zh" to "已选择", "ja" to "選択済み", "ko" to "선택됨", "uz" to "Tanlangan"),
    "apply" to mapOf("ru" to "Применить", "en" to "Apply", "kk" to "Қолдану", "es" to "Aplicar", "fr" to "Appliquer", "de" to "Anwenden", "pt" to "Aplicar", "it" to "Applica", "tr" to "Uygula", "ar" to "تطبيق", "hi" to "लागू करें", "zh" to "应用", "ja" to "適用", "ko" to "적용", "uz" to "Qo‘llash"),
    "already_applied" to mapOf("ru" to "Уже применяется", "en" to "Already applied", "kk" to "Қазірдің өзінде қолданылған", "es" to "Ya aplicado", "fr" to "Déjà appliqué", "de" to "Bereits angewendet", "pt" to "Já aplicado", "it" to "Già applicato", "tr" to "Zaten uygulanıyor", "ar" to "مُطبق بالفعل", "hi" to "पहले से लागू", "zh" to "已应用", "ja" to "適用済み", "ko" to "이미 적용됨", "uz" to "Allaqachon qo‘llangan"),
    "default_font" to mapOf("ru" to "Возвращён стандартный шрифт", "en" to "Default font restored", "kk" to "Әдепкі қаріп қалпына келтірілді", "es" to "Se restauró la fuente predeterminada", "fr" to "Police par défaut restaurée", "de" to "Standardschrift wiederhergestellt", "pt" to "Fonte padrão restaurada", "it" to "Carattere predefinito ripristinato", "tr" to "Varsayılan yazı tipi geri yüklendi", "ar" to "تمت استعادة الخط الافتراضي", "hi" to "डिफ़ॉल्ट फ़ॉन्ट बहाल", "zh" to "已恢复默认字体", "ja" to "標準フォントに戻しました", "ko" to "기본 글꼴 복원됨", "uz" to "Standart shrift tiklandi"),
    "languages_available" to mapOf("ru" to "15 языков интерфейса. Выбор применяется сразу и сохраняется на устройстве.", "en" to "15 interface languages. Your choice applies instantly and is saved on this device.", "kk" to "Интерфейстің 15 тілі. Таңдау бірден қолданылады және құрылғыда сақталады.", "es" to "15 idiomas de interfaz. La selección se aplica al instante y se guarda en este dispositivo.", "fr" to "15 langues d’interface. Le choix est appliqué immédiatement et enregistré sur cet appareil.", "de" to "15 Oberflächensprachen. Die Auswahl wird sofort übernommen und auf diesem Gerät gespeichert.", "pt" to "15 idiomas de interface. A escolha é aplicada imediatamente e salva neste dispositivo.", "it" to "15 lingue dell’interfaccia. La scelta viene applicata subito e salvata sul dispositivo.", "tr" to "15 arayüz dili. Seçiminiz anında uygulanır ve bu cihaza kaydedilir.", "ar" to "15 لغة للواجهة. يُطبق اختيارك فورًا ويُحفظ على هذا الجهاز.", "hi" to "इंटरफ़ेस की 15 भाषाएँ। चयन तुरंत लागू होता है और इस डिवाइस पर सहेजा जाता है।", "zh" to "提供 15 种界面语言。选择会立即生效并保存在此设备上。", "ja" to "15言語のインターフェースに対応。選択はすぐに反映され、この端末に保存されます。", "ko" to "15개 인터페이스 언어를 제공합니다. 선택은 즉시 적용되고 이 기기에 저장됩니다.", "uz" to "Interfeysning 15 ta tili. Tanlov darhol qo‘llanadi va qurilmada saqlanadi."),
    "fraction_error" to mapOf("ru" to "Проверьте дроби", "en" to "Check the fractions", "kk" to "Бөлшектерді тексеріңіз", "es" to "Comprueba las fracciones", "fr" to "Vérifiez les fractions", "de" to "Brüche überprüfen", "pt" to "Verifique as frações", "it" to "Controlla le frazioni", "tr" to "Kesirleri kontrol edin", "ar" to "تحقق من الكسور", "hi" to "भिन्न जाँचें", "zh" to "检查分数", "ja" to "分数を確認してください", "ko" to "분수를 확인하세요", "uz" to "Kasrlarni tekshiring"),
    "values_error" to mapOf("ru" to "Проверьте значения", "en" to "Check the values", "kk" to "Мәндерді тексеріңіз", "es" to "Comprueba los valores", "fr" to "Vérifiez les valeurs", "de" to "Werte überprüfen", "pt" to "Verifique os valores", "it" to "Controlla i valori", "tr" to "Değerleri kontrol edin", "ar" to "تحقق من القيم", "hi" to "मान जाँचें", "zh" to "检查数值", "ja" to "値を確認してください", "ko" to "값을 확인하세요", "uz" to "Qiymatlarni tekshiring"),
    "sizes_error" to mapOf("ru" to "Проверьте размеры", "en" to "Check the dimensions", "kk" to "Өлшемдерді тексеріңіз", "es" to "Comprueba las dimensiones", "fr" to "Vérifiez les dimensions", "de" to "Maße überprüfen", "pt" to "Verifique as dimensões", "it" to "Controlla le dimensioni", "tr" to "Ölçüleri kontrol edin", "ar" to "تحقق من الأبعاد", "hi" to "माप जाँचें", "zh" to "检查尺寸", "ja" to "寸法を確認してください", "ko" to "치수를 확인하세요", "uz" to "O‘lchamlarni tekshiring"),
    "equation_error" to mapOf("ru" to "Проверьте запись уравнения", "en" to "Check the equation", "kk" to "Теңдеудің жазылуын тексеріңіз", "es" to "Comprueba la ecuación", "fr" to "Vérifiez l’équation", "de" to "Gleichung überprüfen", "pt" to "Verifique a equação", "it" to "Controlla l’equazione", "tr" to "Denklemi kontrol edin", "ar" to "تحقق من المعادلة", "hi" to "समीकरण जाँचें", "zh" to "检查方程", "ja" to "方程式を確認してください", "ko" to "방정식을 확인하세요", "uz" to "Tenglamani tekshiring"),
    "example_equation" to mapOf("ru" to "Например: x² − 5x + 6 = 0", "en" to "Example: x² − 5x + 6 = 0", "kk" to "Мысалы: x² − 5x + 6 = 0", "es" to "Ejemplo: x² − 5x + 6 = 0", "fr" to "Exemple : x² − 5x + 6 = 0", "de" to "Beispiel: x² − 5x + 6 = 0", "pt" to "Exemplo: x² − 5x + 6 = 0", "it" to "Esempio: x² − 5x + 6 = 0", "tr" to "Örnek: x² − 5x + 6 = 0", "ar" to "مثال: x² − 5x + 6 = 0", "hi" to "उदाहरण: x² − 5x + 6 = 0", "zh" to "例如：x² − 5x + 6 = 0", "ja" to "例：x² − 5x + 6 = 0", "ko" to "예: x² − 5x + 6 = 0", "uz" to "Masalan: x² − 5x + 6 = 0"),
    "hide_solution" to mapOf("ru" to "  ·  скрыть решение", "en" to "  ·  hide solution", "kk" to "  ·  шешімді жасыру", "es" to "  ·  ocultar solución", "fr" to "  ·  masquer la solution", "de" to "  ·  Lösung ausblenden", "pt" to "  ·  ocultar solução", "it" to "  ·  nascondi soluzione", "tr" to "  ·  çözümü gizle", "ar" to "  ·  إخفاء الحل", "hi" to "  ·  हल छिपाएँ", "zh" to "  ·  隐藏解答", "ja" to "  ·  解答を非表示", "ko" to "  ·  풀이 숨기기", "uz" to "  ·  yechimni yashirish"),
    "show_solution" to mapOf("ru" to "  ·  показать решение", "en" to "  ·  show solution", "kk" to "  ·  шешімді көрсету", "es" to "  ·  mostrar solución", "fr" to "  ·  afficher la solution", "de" to "  ·  Lösung anzeigen", "pt" to "  ·  mostrar solução", "it" to "  ·  mostra soluzione", "tr" to "  ·  çözümü göster", "ar" to "  ·  عرض الحل", "hi" to "  ·  हल दिखाएँ", "zh" to "  ·  显示解答", "ja" to "  ·  解答を表示", "ko" to "  ·  풀이 표시", "uz" to "  ·  yechimni ko‘rsatish"),
    "about_description" to mapOf("ru" to "Научный калькулятор, дроби, проценты, геометрия и решатель уравнений. Всё считается на устройстве, без интернета.", "en" to "Scientific calculator, fractions, percentages, geometry and equation solver. Everything is calculated on your device, offline.", "kk" to "Ғылыми калькулятор, бөлшектер, пайыздар, геометрия және теңдеу шешуші. Барлық есептеулер құрылғыда, интернетсіз орындалады.", "es" to "Calculadora científica, fracciones, porcentajes, geometría y resolución de ecuaciones. Todo se calcula en el dispositivo, sin internet.", "fr" to "Calculatrice scientifique, fractions, pourcentages, géométrie et résolution d’équations. Tout est calculé sur l’appareil, sans Internet.", "de" to "Wissenschaftlicher Rechner, Brüche, Prozente, Geometrie und Gleichungslöser. Alle Berechnungen erfolgen offline auf dem Gerät.", "pt" to "Calculadora científica, frações, porcentagens, geometria e resolução de equações. Tudo é calculado no dispositivo, sem internet.", "it" to "Calcolatrice scientifica, frazioni, percentuali, geometria e risoluzione di equazioni. Tutto viene calcolato sul dispositivo, offline.", "tr" to "Bilimsel hesap makinesi, kesirler, yüzdeler, geometri ve denklem çözücü. Tüm hesaplamalar internet olmadan cihazda yapılır.", "ar" to "آلة حاسبة علمية وكسور ونسب مئوية وهندسة وحل معادلات. تتم جميع العمليات على الجهاز دون إنترنت.", "hi" to "वैज्ञानिक कैलकुलेटर, भिन्न, प्रतिशत, ज्यामिति और समीकरण हल। सभी गणनाएँ बिना इंटरनेट डिवाइस पर होती हैं।", "zh" to "科学计算器、分数、百分比、几何和方程求解。所有计算均在设备本地离线完成。", "ja" to "関数電卓、分数、パーセント、幾何、方程式ソルバー。すべて端末上でオフライン計算します。", "ko" to "공학용 계산기, 분수, 백분율, 기하학 및 방정식 풀이. 모든 계산은 인터넷 없이 기기에서 처리됩니다.", "uz" to "Ilmiy kalkulyator, kasrlar, foizlar, geometriya va tenglama yechish. Barcha hisob-kitoblar internetsiz qurilmada bajariladi."),
    "converter_title" to mapOf("ru" to "Конвертер величин", "en" to "Unit converter", "kk" to "Өлшем бірліктерін түрлендіргіш", "es" to "Conversor de unidades", "fr" to "Convertisseur d’unités", "de" to "Einheitenumrechner", "pt" to "Conversor de unidades", "it" to "Convertitore di unità", "tr" to "Birim dönüştürücü", "ar" to "محول الوحدات", "hi" to "इकाई कनवर्टर", "zh" to "单位换算器", "ja" to "単位変換", "ko" to "단위 변환기", "uz" to "Birlik konvertori"),
    "value" to mapOf("ru" to "Значение", "en" to "Value", "kk" to "Мән", "es" to "Valor", "fr" to "Valeur", "de" to "Wert", "pt" to "Valor", "it" to "Valore", "tr" to "Değer", "ar" to "القيمة", "hi" to "मान", "zh" to "数值", "ja" to "値", "ko" to "값", "uz" to "Qiymat"),
    "from" to mapOf("ru" to "Из", "en" to "From", "kk" to "Бастап", "es" to "De", "fr" to "De", "de" to "Von", "pt" to "De", "it" to "Da", "tr" to "Şuradan", "ar" to "من", "hi" to "से", "zh" to "从", "ja" to "変換元", "ko" to "변환 전", "uz" to "Dan"),
    "to" to mapOf("ru" to "В", "en" to "To", "kk" to "Дейін", "es" to "A", "fr" to "Vers", "de" to "Nach", "pt" to "Para", "it" to "A", "tr" to "Şuraya", "ar" to "إلى", "hi" to "में", "zh" to "到", "ja" to "変換先", "ko" to "변환 후", "uz" to "Ga"),
    "swap_units" to mapOf("ru" to "Поменять единицы местами", "en" to "Swap units", "kk" to "Өлшем бірліктерін ауыстыру", "es" to "Intercambiar unidades", "fr" to "Inverser les unités", "de" to "Einheiten tauschen", "pt" to "Inverter unidades", "it" to "Inverti unità", "tr" to "Birimleri değiştir", "ar" to "تبديل الوحدات", "hi" to "इकाइयाँ बदलें", "zh" to "交换单位", "ja" to "単位を入れ替え", "ko" to "단위 바꾸기", "uz" to "Birliklarni almashtirish"),
    "result" to mapOf("ru" to "РЕЗУЛЬТАТ", "en" to "RESULT", "kk" to "НӘТИЖЕ", "es" to "RESULTADO", "fr" to "RÉSULTAT", "de" to "ERGEBNIS", "pt" to "RESULTADO", "it" to "RISULTATO", "tr" to "SONUÇ", "ar" to "النتيجة", "hi" to "परिणाम", "zh" to "结果", "ja" to "結果", "ko" to "결과", "uz" to "NATIJA"),
    "enter_value" to mapOf("ru" to "Введите значение", "en" to "Enter a value", "kk" to "Мәнді енгізіңіз", "es" to "Introduce un valor", "fr" to "Saisissez une valeur", "de" to "Wert eingeben", "pt" to "Insira um valor", "it" to "Inserisci un valore", "tr" to "Bir değer girin", "ar" to "أدخل قيمة", "hi" to "मान दर्ज करें", "zh" to "输入数值", "ja" to "値を入力", "ko" to "값을 입력하세요", "uz" to "Qiymat kiriting"),
    "offline_conversion" to mapOf("ru" to "Пересчёт выполняется автоматически. Интернет не требуется.", "en" to "Conversion is automatic. No internet connection is required.", "kk" to "Түрлендіру автоматты түрде орындалады. Интернет қажет емес.", "es" to "La conversión es automática. No se necesita internet.", "fr" to "La conversion est automatique. Aucune connexion Internet n’est nécessaire.", "de" to "Die Umrechnung erfolgt automatisch. Internet ist nicht erforderlich.", "pt" to "A conversão é automática. Não é necessária internet.", "it" to "La conversione è automatica. Non serve Internet.", "tr" to "Dönüştürme otomatik yapılır. İnternet gerekmez.", "ar" to "يتم التحويل تلقائيًا. لا يلزم الاتصال بالإنترنت.", "hi" to "रूपांतरण अपने आप होता है। इंटरनेट की आवश्यकता नहीं है।", "zh" to "自动换算，无需联网。", "ja" to "変換は自動で行われます。インターネットは不要です。", "ko" to "자동으로 변환됩니다. 인터넷 연결이 필요하지 않습니다.", "uz" to "O‘girish avtomatik bajariladi. Internet kerak emas."),
    "length" to mapOf("ru" to "Длина", "en" to "Length", "kk" to "Ұзындық", "es" to "Longitud", "fr" to "Longueur", "de" to "Länge", "pt" to "Comprimento", "it" to "Lunghezza", "tr" to "Uzunluk", "ar" to "الطول", "hi" to "लंबाई", "zh" to "长度", "ja" to "長さ", "ko" to "길이", "uz" to "Uzunlik"),
    "mass" to mapOf("ru" to "Масса", "en" to "Mass", "kk" to "Масса", "es" to "Masa", "fr" to "Masse", "de" to "Masse", "pt" to "Massa", "it" to "Massa", "tr" to "Kütle", "ar" to "الكتلة", "hi" to "द्रव्यमान", "zh" to "质量", "ja" to "質量", "ko" to "질량", "uz" to "Massa"),
    "area" to mapOf("ru" to "Площадь", "en" to "Area", "kk" to "Аудан", "es" to "Área", "fr" to "Surface", "de" to "Fläche", "pt" to "Área", "it" to "Area", "tr" to "Alan", "ar" to "المساحة", "hi" to "क्षेत्रफल", "zh" to "面积", "ja" to "面積", "ko" to "넓이", "uz" to "Maydon"),
    "volume" to mapOf("ru" to "Объём", "en" to "Volume", "kk" to "Көлем", "es" to "Volumen", "fr" to "Volume", "de" to "Volumen", "pt" to "Volume", "it" to "Volume", "tr" to "Hacim", "ar" to "الحجم", "hi" to "आयतन", "zh" to "体积", "ja" to "体積", "ko" to "부피", "uz" to "Hajm"),
    "temperature" to mapOf("ru" to "Температура", "en" to "Temperature", "kk" to "Температура", "es" to "Temperatura", "fr" to "Température", "de" to "Temperatur", "pt" to "Temperatura", "it" to "Temperatura", "tr" to "Sıcaklık", "ar" to "درجة الحرارة", "hi" to "तापमान", "zh" to "温度", "ja" to "温度", "ko" to "온도", "uz" to "Harorat"),
    "speed" to mapOf("ru" to "Скорость", "en" to "Speed", "kk" to "Жылдамдық", "es" to "Velocidad", "fr" to "Vitesse", "de" to "Geschwindigkeit", "pt" to "Velocidade", "it" to "Velocità", "tr" to "Hız", "ar" to "السرعة", "hi" to "गति", "zh" to "速度", "ja" to "速度", "ko" to "속도", "uz" to "Tezlik"),
    "time" to mapOf("ru" to "Время", "en" to "Time", "kk" to "Уақыт", "es" to "Tiempo", "fr" to "Temps", "de" to "Zeit", "pt" to "Tempo", "it" to "Tempo", "tr" to "Zaman", "ar" to "الوقت", "hi" to "समय", "zh" to "时间", "ja" to "時間", "ko" to "시간", "uz" to "Vaqt"),
    "pressure" to mapOf("ru" to "Давление", "en" to "Pressure", "kk" to "Қысым", "es" to "Presión", "fr" to "Pression", "de" to "Druck", "pt" to "Pressão", "it" to "Pressione", "tr" to "Basınç", "ar" to "الضغط", "hi" to "दाब", "zh" to "压力", "ja" to "圧力", "ko" to "압력", "uz" to "Bosim"),
    "energy" to mapOf("ru" to "Энергия", "en" to "Energy", "kk" to "Энергия", "es" to "Energía", "fr" to "Énergie", "de" to "Energie", "pt" to "Energia", "it" to "Energia", "tr" to "Enerji", "ar" to "الطاقة", "hi" to "ऊर्जा", "zh" to "能量", "ja" to "エネルギー", "ko" to "에너지", "uz" to "Energiya"),
    "data" to mapOf("ru" to "Данные", "en" to "Data", "kk" to "Деректер", "es" to "Datos", "fr" to "Données", "de" to "Daten", "pt" to "Dados", "it" to "Dati", "tr" to "Veri", "ar" to "البيانات", "hi" to "डेटा", "zh" to "数据", "ja" to "データ", "ko" to "데이터", "uz" to "Ma’lumotlar"),
    "equation_help" to mapOf("ru" to "Линейные и квадратные уравнения с одной переменной x. Деление на выражение с x и степени выше второй не поддерживаются.", "en" to "Linear and quadratic equations in one variable x. Division by expressions containing x and powers above two are not supported.", "kk" to "Бір x айнымалысы бар сызықтық және квадрат теңдеулер. x бар өрнекке бөлу және екінші дәрежеден жоғары дәрежелер қолдау таппайды.", "es" to "Ecuaciones lineales y cuadráticas con una variable x. No se admiten divisiones por expresiones con x ni potencias superiores al segundo grado.", "fr" to "Équations linéaires et quadratiques à une variable x. Les divisions par des expressions contenant x et les puissances supérieures au deuxième degré ne sont pas prises en charge.", "de" to "Lineare und quadratische Gleichungen mit einer Variablen x. Division durch Ausdrücke mit x und Potenzen über dem zweiten Grad werden nicht unterstützt.", "pt" to "Equações lineares e quadráticas com uma variável x. Divisão por expressões com x e potências acima do segundo grau não são compatíveis.", "it" to "Equazioni lineari e quadratiche con una variabile x. Non sono supportate divisioni per espressioni con x e potenze superiori al secondo grado.", "tr" to "Tek değişkenli x içeren doğrusal ve ikinci derece denklemler. x içeren ifadelere bölme ve ikinci dereceden yüksek üsler desteklenmez.", "ar" to "معادلات خطية وتربيعية بمتغير واحد x. لا يدعم القسمة على تعبيرات تحتوي على x أو القوى الأعلى من الدرجة الثانية.", "hi" to "एक चर x वाले रैखिक और द्विघात समीकरण। x वाले व्यंजकों से भाग और दूसरी घात से ऊपर की घातें समर्थित नहीं हैं।", "zh" to "支持含一个变量 x 的一次和二次方程。不支持除以含 x 的表达式以及高于二次的幂。", "ja" to "1つの変数 x を持つ一次・二次方程式に対応します。x を含む式による除算と三次以上の累乗には対応していません。", "ko" to "하나의 변수 x를 사용하는 일차 및 이차 방정식을 지원합니다. x가 포함된 식으로 나누거나 이차보다 높은 차수는 지원하지 않습니다.", "uz" to "Bitta x o‘zgaruvchili chiziqli va kvadrat tenglamalar. x qatnashgan ifodaga bo‘lish va ikkinchi darajadan yuqori darajalar qo‘llanmaydi."),
    "divide_zero" to mapOf("ru" to "Деление на ноль", "en" to "Division by zero", "kk" to "Нөлге бөлу", "es" to "División por cero", "fr" to "Division par zéro", "de" to "Division durch null", "pt" to "Divisão por zero", "it" to "Divisione per zero", "tr" to "Sıfıra bölme", "ar" to "القسمة على صفر", "hi" to "शून्य से भाग", "zh" to "除以零", "ja" to "ゼロ除算", "ko" to "0으로 나누기", "uz" to "Nolga bo‘lish"),
    "bad_expression" to mapOf("ru" to "Проверьте выражение", "en" to "Check the expression", "kk" to "Өрнекті тексеріңіз", "es" to "Comprueba la expresión", "fr" to "Vérifiez l’expression", "de" to "Ausdruck überprüfen", "pt" to "Verifique a expressão", "it" to "Controlla l’espressione", "tr" to "İfadeyi kontrol edin", "ar" to "تحقق من التعبير", "hi" to "व्यंजक जाँचें", "zh" to "检查表达式", "ja" to "式を確認してください", "ko" to "식을 확인하세요", "uz" to "Ifodani tekshiring"),
    "calc_failed" to mapOf("ru" to "Не удалось вычислить выражение", "en" to "Could not evaluate the expression", "kk" to "Өрнекті есептеу мүмкін болмады", "es" to "No se pudo evaluar la expresión", "fr" to "Impossible de calculer l’expression", "de" to "Der Ausdruck konnte nicht berechnet werden", "pt" to "Não foi possível calcular a expressão", "it" to "Impossibile calcolare l’espressione", "tr" to "İfade hesaplanamadı", "ar" to "تعذر حساب التعبير", "hi" to "व्यंजक की गणना नहीं हो सकी", "zh" to "无法计算表达式", "ja" to "式を計算できませんでした", "ko" to "식을 계산할 수 없습니다", "uz" to "Ifodani hisoblab bo‘lmadi"),
    "font_file_type" to mapOf("ru" to "Выберите файл TTF или OTF","en" to "Choose a TTF or OTF file","kk" to "TTF немесе OTF файлын таңдаңыз"),
    "font_open_error" to mapOf("ru" to "Не удалось открыть файл шрифта","en" to "Could not open the font file","kk" to "Қаріп файлын ашу мүмкін болмады"),
    "font_empty" to mapOf("ru" to "Файл шрифта пустой","en" to "The font file is empty","kk" to "Қаріп файлы бос"),
    "font_install_error" to mapOf("ru" to "Не удалось установить шрифт","en" to "Could not install the font","kk" to "Қаріпті орнату мүмкін болмады"),
    "font_applied" to mapOf("ru" to "Шрифт применён","en" to "Font applied","kk" to "Қаріп қолданылды"),
    "percent_decrease" to mapOf("ru" to "Процент (− уменьшить)","en" to "Percent (− decrease)","kk" to "Пайыз (− азайту)","es" to "Porcentaje (− reducir)","fr" to "Pourcentage (− réduire)","de" to "Prozent (− verringern)","pt" to "Porcentagem (− diminuir)","it" to "Percentuale (− riduci)","tr" to "Yüzde (− azalt)","ar" to "النسبة (− تقليل)","hi" to "प्रतिशत (− घटाएँ)","zh" to "百分比（− 减少）","ja" to "パーセント（− 減少）","ko" to "퍼센트 (− 감소)","uz" to "Foiz (− kamaytirish)"),
    "conversion_error" to mapOf("ru" to "Не удалось выполнить конвертацию","en" to "Conversion could not be completed","kk" to "Түрлендіру орындалмады","es" to "No se pudo completar la conversión","fr" to "La conversion a échoué","de" to "Umrechnung fehlgeschlagen","pt" to "Não foi possível concluir a conversão","it" to "Conversione non riuscita","tr" to "Dönüştürme tamamlanamadı","ar" to "تعذر إكمال التحويل","hi" to "रूपांतरण पूरा नहीं हो सका","zh" to "无法完成换算","ja" to "変換できませんでした","ko" to "변환을 완료할 수 없습니다","uz" to "O‘girishni bajarib bo‘lmadi"),
    "home" to mapOf("ru" to "Главная","en" to "Home","kk" to "Басты бет"),
    "font_file_type" to mapOf("ru" to "Выберите файл TTF или OTF","en" to "Choose a TTF or OTF file","kk" to "TTF немесе OTF файлын таңдаңыз"),
    "font_open_error" to mapOf("ru" to "Не удалось открыть файл шрифта","en" to "Could not open the font file","kk" to "Қаріп файлын ашу мүмкін болмады"),
    "font_empty" to mapOf("ru" to "Файл шрифта пустой","en" to "The font file is empty","kk" to "Қаріп файлы бос"),
    "font_install_error" to mapOf("ru" to "Не удалось установить шрифт","en" to "Could not install the font","kk" to "Қаріпті орнату мүмкін болмады"),
    "font_applied" to mapOf("ru" to "Шрифт применён","en" to "Font applied","kk" to "Қаріп қолданылды"),
    "theme_violet" to mapOf("ru" to "Фирменная фиолетовая палитра MathFlow.","en" to "MathFlow signature purple palette.","kk" to "MathFlow фирмалық күлгін палитрасы."),
    "theme_black" to mapOf("ru" to "Почти чёрный интерфейс с нейтральными поверхностями.","en" to "Near-black interface with neutral surfaces.","kk" to "Қараға жақын интерфейс және бейтарап беттер."),
    "theme_white" to mapOf("ru" to "Светлая тема с чистыми белыми поверхностями.","en" to "Light theme with clean white surfaces.","kk" to "Таза ақ беттері бар ашық тақырып."),
    "theme_purple" to mapOf("ru" to "Глубокий фиолетовый с насыщенными акцентами.","en" to "Deep purple with rich accents.","kk" to "Қанық екпіндері бар қою күлгін."),
    "theme_ocean" to mapOf("ru" to "Холодные синие оттенки и яркий голубой акцент.","en" to "Cool blue tones with a bright cyan accent.","kk" to "Салқын көк реңктер және ашық көгілдір екпін."),
    "theme_emerald" to mapOf("ru" to "Тёмно-зелёная палитра с мятными акцентами.","en" to "Dark green palette with mint accents.","kk" to "Жалбыз түсті екпіндері бар қою жасыл палитра."),
    "theme_sunset" to mapOf("ru" to "Тёплые оттенки заката: коралл, оранжевый и розовый.","en" to "Warm sunset tones: coral, orange and pink.","kk" to "Күн батар шақтың жылы реңктері: маржан, қызғылт сары және қызғылт."),
    "theme_rose" to mapOf("ru" to "Мягкая розовая тема с ярким малиновым акцентом.","en" to "Soft pink theme with a vivid raspberry accent.","kk" to "Ашық таңқурай екпіні бар жұмсақ қызғылт тақырып."),
    "theme_midnight" to mapOf("ru" to "Ночная фиолетовая палитра с мягкими лавандовыми акцентами.","en" to "Night purple palette with soft lavender accents.","kk" to "Жұмсақ лаванда екпіндері бар түнгі күлгін палитра."),
    "theme_graphite" to mapOf("ru" to "Спокойные графитовые оттенки с холодным светлым акцентом.","en" to "Calm graphite tones with a cool light accent.","kk" to "Салқын ашық екпіні бар байсалды графит реңктері."),
    "icon_vanilla" to mapOf("ru" to "Оригинальная фиолетовая иконка MathFlow.","en" to "Original purple MathFlow icon.","kk" to "MathFlow бастапқы күлгін белгішесі."),
    "icon_black" to mapOf("ru" to "Чёрный фон и белый фирменный знак.","en" to "Black background with a white logo.","kk" to "Қара фон және ақ фирмалық белгі."),
    "icon_white" to mapOf("ru" to "Белый фон с фиолетовым знаком.","en" to "White background with a purple logo.","kk" to "Ақ фон және күлгін белгі."),
    "icon_purple" to mapOf("ru" to "Тёмный фон с насыщенным фиолетовым знаком.","en" to "Dark background with a vivid purple logo.","kk" to "Қою фон және қанық күлгін белгі."),
    "icon_ocean" to mapOf("ru" to "Глубокий синий фон и яркий голубой знак.","en" to "Deep blue background with a bright cyan logo.","kk" to "Қою көк фон және ашық көгілдір белгі."),
    "icon_emerald" to mapOf("ru" to "Тёмно-зелёный фон и мятный знак.","en" to "Dark green background with a mint logo.","kk" to "Қою жасыл фон және жалбыз түсті белгі."),
    "icon_sunset" to mapOf("ru" to "Градиент заката с белым фирменным знаком.","en" to "Sunset gradient with a white logo.","kk" to "Күн бату градиенті және ақ белгі."),
    "icon_rose" to mapOf("ru" to "Нежно-розовый фон и малиновый знак.","en" to "Soft pink background with a raspberry logo.","kk" to "Нәзік қызғылт фон және таңқурай түсті белгі."),
    "icon_midnight" to mapOf("ru" to "Глубокий ночной фон и лавандовый знак.","en" to "Deep night background with a lavender logo.","kk" to "Қою түнгі фон және лаванда түсті белгі."),
    "icon_graphite" to mapOf("ru" to "Графитовый фон и светлый холодный знак.","en" to "Graphite background with a cool light logo.","kk" to "Графит фон және ашық салқын түсті белгі.")
)

@Composable
internal fun tx(key: String): String {
    val language = LocalLanguage.current
    return uiText[key]?.get(language) ?: uiText[key]?.get("en") ?: key
}

@Composable
internal fun displayText(source: String): String {
    val key = when (source) {
        "Длина" -> "length"
        "Масса" -> "mass"
        "Площадь" -> "area"
        "Объём" -> "volume"
        "Температура" -> "temperature"
        "Скорость" -> "speed"
        "Время" -> "time"
        "Давление" -> "pressure"
        "Энергия" -> "energy"
        "Данные" -> "data"
        "Главная" -> "home"
        "Инструменты" -> "tools"
        "Решатель" -> "solver"
        "История" -> "history"
        "Настройки" -> "settings"
        "Иконки приложения" -> "app_icons"
        "Темы оформления" -> "themes"
        "Язык приложения" -> "app_language"
        "Дроби" -> "fractions"
        "Проценты" -> "percentages"
        "Геометрия" -> "geometry"
        "Конвертер" -> "converter"
        "% от числа" -> "percent_of"
        "Сколько %" -> "what_percent"
        "± на %" -> "change_percent"
        "Прямоугольник" -> "rectangle"
        "Круг" -> "circle"
        "Треугольник" -> "triangle"
        "Дробь 1" -> "fraction_one"
        "Дробь 2" -> "fraction_two"
        "Часть" -> "part"
        "Целое" -> "whole"
        "Процент" -> "percent"
        "Число" -> "number"
        "Радиус" -> "radius"
        "Сторона a" -> "side_a"
        "Сторона b" -> "side_b"
        "Сторона c" -> "side_c"
        "Применяется" -> "applied"
        "Выбрана" -> "selected"
        "Применить" -> "apply"
        "Уже применяется" -> "already_applied"
        "Возвращён стандартный шрифт" -> "default_font"
        "Конвертер величин" -> "converter_title"
        "Значение" -> "value"
        "Из" -> "from"
        "В" -> "to"
        "Поменять единицы местами" -> "swap_units"
        "РЕЗУЛЬТАТ" -> "result"
        "Введите значение" -> "enter_value"
        "Пересчёт выполняется автоматически. Интернет не требуется." -> "offline_conversion"
        "Проверьте дроби" -> "fraction_error"
        "Проверьте значения" -> "values_error"
        "Проверьте размеры" -> "sizes_error"
        "Проверьте запись уравнения" -> "equation_error"
        "Например: x² − 5x + 6 = 0" -> "example_equation"
        "  ·  скрыть решение" -> "hide_solution"
        "  ·  показать решение" -> "show_solution"
        "15 языков интерфейса. Выбор применяется сразу и сохраняется на устройстве." -> "languages_available"
        "Процент (− уменьшить)" -> "percent_decrease"
        "Division by zero" -> "divide_zero"
        "Не удалось выполнить конвертацию" -> "conversion_error"
        "Фирменная фиолетовая палитра MathFlow." -> "theme_violet"
        "Почти чёрный интерфейс с нейтральными поверхностями." -> "theme_black"
        "Светлая тема с чистыми белыми поверхностями." -> "theme_white"
        "Глубокий фиолетовый с насыщенными акцентами." -> "theme_purple"
        "Холодные синие оттенки и яркий голубой акцент." -> "theme_ocean"
        "Тёмно-зелёная палитра с мятными акцентами." -> "theme_emerald"
        "Тёплые оттенки заката: коралл, оранжевый и розовый." -> "theme_sunset"
        "Мягкая розовая тема с ярким малиновым акцентом." -> "theme_rose"
        "Ночная фиолетовая палитра с мягкими лавандовыми акцентами." -> "theme_midnight"
        "Спокойные графитовые оттенки с холодным светлым акцентом." -> "theme_graphite"
        "Оригинальная фиолетовая иконка MathFlow." -> "icon_vanilla"
        "Чёрный фон и белый фирменный знак." -> "icon_black"
        "Белый фон с фиолетовым знаком." -> "icon_white"
        "Тёмный фон с насыщенным фиолетовым знаком." -> "icon_purple"
        "Глубокий синий фон и яркий голубой знак." -> "icon_ocean"
        "Тёмно-зелёный фон и мятный знак." -> "icon_emerald"
        "Градиент заката с белым фирменным знаком." -> "icon_sunset"
        "Нежно-розовый фон и малиновый знак." -> "icon_rose"
        "Глубокий ночной фон и лавандовый знак." -> "icon_midnight"
        "Графитовый фон и светлый холодный знак." -> "icon_graphite"
        "Линейные и квадратные уравнения с одной переменной x. Деление на выражение с x и степени выше второй не поддерживаются." -> "equation_help"
        "Деление на ноль" -> "divide_zero"
        "Проверьте выражение" -> "bad_expression"
        "Не удалось вычислить выражение" -> "calc_failed"
        "Ошибка" -> "error"
        "Выберите файл TTF или OTF" -> "font_file_type"
        "Не удалось открыть файл шрифта" -> "font_open_error"
        "Файл шрифта пустой" -> "font_empty"
        "Не удалось установить шрифт" -> "font_install_error"
        "Шрифт применён" -> "font_applied"
        "Фирменная фиолетовая палитра MathFlow." -> "theme_violet"
        "Почти чёрный интерфейс с нейтральными поверхностями." -> "theme_black"
        "Светлая тема с чистыми белыми поверхностями." -> "theme_white"
        "Глубокий фиолетовый с насыщенными акцентами." -> "theme_purple"
        "Холодные синие оттенки и яркий голубой акцент." -> "theme_ocean"
        "Тёмно-зелёная палитра с мятными акцентами." -> "theme_emerald"
        "Тёплые оттенки заката: коралл, оранжевый и розовый." -> "theme_sunset"
        "Мягкая розовая тема с ярким малиновым акцентом." -> "theme_rose"
        "Ночная фиолетовая палитра с мягкими лавандовыми акцентами." -> "theme_midnight"
        "Спокойные графитовые оттенки с холодным светлым акцентом." -> "theme_graphite"
        "Оригинальная фиолетовая иконка MathFlow." -> "icon_vanilla"
        "Чёрный фон и белый фирменный знак." -> "icon_black"
        "Белый фон с фиолетовым знаком." -> "icon_white"
        "Тёмный фон с насыщенным фиолетовым знаком." -> "icon_purple"
        "Глубокий синий фон и яркий голубой знак." -> "icon_ocean"
        "Тёмно-зелёный фон и мятный знак." -> "icon_emerald"
        "Градиент заката с белым фирменным знаком." -> "icon_sunset"
        "Нежно-розовый фон и малиновый знак." -> "icon_rose"
        "Глубокий ночной фон и лавандовый знак." -> "icon_midnight"
        "Графитовый фон и светлый холодный знак." -> "icon_graphite"
        "Линейные и квадратные уравнения с одной переменной x. Деление на выражение с x и степени выше второй не поддерживаются." -> "equation_help"
        "Деление на ноль" -> "divide_zero"
        "Проверьте выражение" -> "bad_expression"
        "Не удалось вычислить выражение" -> "calc_failed"
        "Ошибка" -> "error"
        else -> return source
    }
    return tx(key)
}
