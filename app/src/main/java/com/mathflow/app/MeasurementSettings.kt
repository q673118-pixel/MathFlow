package com.mathflow.app

import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

internal val LocalMeasurementSystem = staticCompositionLocalOf { "metric" }

internal fun resolveMeasurementSystem(setting: String, locale: Locale = Locale.getDefault()): String {
    if (setting == "metric" || setting == "us") return setting
    return if (locale.country.uppercase(Locale.ROOT) in setOf("US", "LR", "MM")) "us" else "metric"
}

internal fun preferredUnits(categoryId: String, system: String): Pair<String, String> {
    val us = system == "us"
    return when (categoryId) {
        "length" -> if (us) "mi" to "ft" else "km" to "m"
        "mass" -> if (us) "lb" to "oz" else "kg" to "g"
        "area" -> if (us) "acre" to "ft2" else "km2" to "m2"
        "volume" -> if (us) "gal" to "cup" else "l" to "ml"
        "temperature" -> if (us) "f" to "c" else "c" to "k"
        "speed" -> if (us) "mph" to "kmh" else "kmh" to "ms"
        "time" -> "h" to "min"
        "pressure" -> "bar" to "pa"
        "energy" -> "kj" to "j"
        "data" -> "gb" to "mb"
        else -> "km" to "m"
    }
}

private val regionalText = mapOf(
 "measurements" to listOf("Регион и измерения","Region & measurements","Аймақ және өлшемдер","Región y medidas","Région et mesures","Region und Maße","Região e medidas","Area e unità","Bölge ve ölçü birimleri","المنطقة ووحدات القياس","क्षेत्र और माप","地区与度量单位","地域と単位","지역 및 측정 단위","Hudud va o‘lchovlar"),
 "measurements_subtitle" to listOf("Система единиц и региональные форматы","Unit system and regional formats","Өлшем жүйесі және аймақтық пішімдер","Sistema de unidades y formatos regionales","Système d’unités et formats régionaux","Einheitensystem und regionale Formate","Sistema de unidades e formatos regionais","Sistema di unità e formati regionali","Birim sistemi ve bölgesel biçimler","نظام الوحدات والتنسيقات الإقليمية","इकाई प्रणाली और क्षेत्रीय प्रारूप","单位制和地区格式","単位系と地域形式","단위 체계 및 지역 형식","O‘lchov tizimi va hududiy formatlar"),
 "automatic" to listOf("Автоматически","Automatic","Автоматты","Automático","Automatique","Automatisch","Automático","Automatico","Otomatik","تلقائي","स्वचालित","自动","自動","자동","Avtomatik"),
 "automatic_desc" to listOf("Определять по региону устройства","Use the device region","Құрылғы аймағын пайдалану","Usar la región del dispositivo","Utiliser la région de l’appareil","Geräteregion verwenden","Usar a região do dispositivo","Usa la regione del dispositivo","Cihaz bölgesini kullan","استخدام منطقة الجهاز","डिवाइस का क्षेत्र उपयोग करें","使用设备地区","端末の地域設定を使用","기기 지역 사용","Qurilma hududidan foydalanish"),
 "metric" to listOf("Метрическая","Metric","Метрикалық","Métrico","Métrique","Metrisch","Métrico","Metrico","Metrik","متري","मीट्रिक","公制","メートル法","미터법","Metrik"),
 "metric_desc" to listOf("км, кг, °C, л","km, kg, °C, L","км, кг, °C, л","km, kg, °C, L","km, kg, °C, L","km, kg, °C, l","km, kg, °C, L","km, kg, °C, L","km, kg, °C, L","كم، كجم، °C، لتر","km, kg, °C, L","km、kg、°C、L","km、kg、°C、L","km, kg, °C, L","km, kg, °C, L"),
 "us_customary" to listOf("Американская","US customary","АҚШ жүйесі","Estadounidense","Américaine","US-amerikanisch","Americano","Statunitense","ABD ölçü sistemi","النظام الأمريكي","अमेरिकी","美制","米国慣用単位","미국 관용 단위","AQSh o‘lchov tizimi"),
 "us_desc" to listOf("мили, фунты, °F, галлоны США","miles, pounds, °F, US gallons","миль, фунт, °F, АҚШ галлоны","millas, libras, °F, galones de EE. UU.","miles, livres, °F, gallons US","Meilen, Pfund, °F, US-Gallonen","milhas, libras, °F, galões dos EUA","miglia, libbre, °F, galloni USA","mil, pound, °F, ABD galonu","ميل، رطل، °F، جالون أمريكي","मील, पाउंड, °F, अमेरिकी गैलन","英里、磅、°F、美制加仑","マイル、ポンド、°F、米ガロン","마일, 파운드, °F, 미국 갤런","mil, funt, °F, AQSh galloni"),
 "units_note" to listOf("Язык интерфейса не влияет на систему измерений. Единицы можно менять вручную в конвертере.","The app language does not change the unit system. Choose units manually in the converter at any time.","Интерфейс тілі өлшем жүйесіне әсер етпейді. Конвертерде бірліктерді қолмен өзгертуге болады.","El idioma no cambia el sistema de unidades. Puedes elegirlas en el convertidor.","La langue ne modifie pas le système d’unités. Vous pouvez choisir les unités dans le convertisseur.","Die App-Sprache ändert das Einheitensystem nicht. Einheiten lassen sich manuell wählen.","O idioma não altera o sistema de unidades. É possível escolher unidades manualmente.","La lingua non cambia il sistema di unità. Puoi scegliere le unità manualmente.","Uygulama dili ölçü sistemini değiştirmez. Birimleri elle seçebilirsiniz.","لغة التطبيق لا تغيّر نظام الوحدات. يمكنك اختيار الوحدات يدويًا.","ऐप की भाषा इकाई प्रणाली नहीं बदलती। इकाइयाँ मैन्युअल चुनें।","应用语言不会改变单位制。你可以手动选择单位。","アプリの言語と単位系は別々に設定できます。","앱 언어와 단위 체계는 별개입니다. 단위를 직접 선택할 수 있습니다.","Ilova tili o‘lchov tizimini o‘zgartirmaydi. Birliklarni qo‘lda tanlash mumkin."),
 "welcome_title" to listOf("Настроим MathFlow","Set up MathFlow","MathFlow баптау","Configura MathFlow","Configurer MathFlow","MathFlow einrichten","Configurar MathFlow","Configura MathFlow","MathFlow’u ayarla","إعداد MathFlow","MathFlow सेट करें","设置 MathFlow","MathFlow の設定","MathFlow 설정","MathFlow sozlamalari"),
 "welcome_desc" to listOf("Выберите язык, систему измерений и оформление. Всё это можно изменить позже в настройках.","Choose your language, unit system, and appearance. You can change everything later in Settings.","Тілді, өлшем жүйесін және безендіруді таңдаңыз. Кейін өзгертуге болады.","Elige idioma, unidades y aspecto. Podrás cambiarlo después.","Choisissez la langue, les unités et l’apparence. Vous pourrez tout modifier plus tard.","Wähle Sprache, Einheitensystem und Design. Alles lässt sich später ändern.","Escolha idioma, unidades e aparência. Você pode alterar depois.","Scegli lingua, unità e aspetto. Potrai modificarli in seguito.","Dil, ölçü birimi ve görünümü seçin. Daha sonra değiştirebilirsiniz.","اختر اللغة ونظام الوحدات والمظهر. يمكنك تغييرها لاحقًا.","भाषा, इकाई प्रणाली और रूप चुनें। बाद में बदल सकते हैं।","选择语言、单位制和外观。之后可在设置中更改。","言語、単位系、外観を選択してください。後から変更できます。","언어, 단위 체계, 모양을 선택하세요. 나중에 변경할 수 있습니다.","Til, o‘lchov tizimi va ko‘rinishni tanlang. Keyin o‘zgartirish mumkin."),
 "language" to listOf("Язык","Language","Тіл","Idioma","Langue","Sprache","Idioma","Lingua","Dil","اللغة","भाषा","语言","言語","언어","Til"),
 "appearance" to listOf("Оформление","Appearance","Безендіру","Apariencia","Apparence","Darstellung","Aparência","Aspetto","Görünüm","المظهر","रूप","外观","外観","모양","Ko‘rinish"),
 "dark" to listOf("Тёмная","Dark","Қараңғы","Oscuro","Sombre","Dunkel","Escuro","Scuro","Koyu","داكن","डार्क","深色","ダーク","다크","To‘q"),
 "light" to listOf("Светлая","Light","Ашық","Claro","Clair","Hell","Claro","Chiaro","Açık","فاتح","लाइट","浅色","ライト","라이트","Yorug‘"),
 "continue" to listOf("Продолжить","Continue","Жалғастыру","Continuar","Continuer","Weiter","Continuar","Continua","Devam et","متابعة","जारी रखें","继续","続行","계속","Davom et")
)
private val regionalTags = listOf("ru","en","kk","es","fr","de","pt","it","tr","ar","hi","zh","ja","ko","uz")
internal fun measurementText(key: String, language: String): String {
    val index = regionalTags.indexOf(language).takeIf { it >= 0 } ?: 1
    return regionalText[key]?.getOrNull(index) ?: key
}

private val localizedUnits = mapOf(
 "mm" to listOf("миллиметр","millimeter","миллиметр","milímetro","millimètre","Millimeter","milímetro","millimetro","milimetre","مليمتر","मिलीमीटर","毫米","ミリメートル","밀리미터","millimetr"),
 "cm" to listOf("сантиметр","centimeter","сантиметр","centímetro","centimètre","Zentimeter","centímetro","centimetro","santimetre","سنتيمتر","सेंटीमीटर","厘米","センチメートル","센티미터","santimetr"),
 "m" to listOf("метр","meter","метр","metro","mètre","Meter","metro","metro","metre","متر","मीटर","米","メートル","미터","metr"),
 "km" to listOf("километр","kilometer","километр","kilómetro","kilomètre","Kilometer","quilômetro","chilometro","kilometre","كيلومتر","किलोमीटर","千米","キロメートル","킬로미터","kilometr"),
 "in" to listOf("дюйм","inch","дюйм","pulgada","pouce","Zoll","polegada","pollice","inç","بوصة","इंच","英寸","インチ","인치","dyuym"),
 "ft" to listOf("фут","foot","фут","pie","pied","Fuß","pé","piede","fit","قدم","फुट","英尺","フィート","피트","fut"),
 "mi" to listOf("миля","mile","миль","milla","mille","Meile","milha","miglio","mil","ميل","मील","英里","マイル","마일","mil"),
 "kg" to listOf("килограмм","kilogram","килограмм","kilogramo","kilogramme","Kilogramm","quilograma","chilogrammo","kilogram","كيلوغرام","किलोग्राम","千克","キログラム","킬로그램","kilogram"),
 "g" to listOf("грамм","gram","грамм","gramo","gramme","Gramm","grama","grammo","gram","غرام","ग्राम","克","グラム","그램","gram"),
 "lb" to listOf("фунт","pound","фунт","libra","livre","Pfund","libra","libbra","libre","رطل","पाउंड","磅","ポンド","파운드","funt"),
 "oz" to listOf("унция","ounce","унция","onza","once","Unze","onça","oncia","ons","أونصة","औंस","盎司","オンス","온스","unsiya"),
 "l" to listOf("литр","liter","литр","litro","litre","Liter","litro","litro","litre","لتر","लीटर","升","リットル","리터","litr"),
 "ml" to listOf("миллилитр","milliliter","миллилитр","mililitro","millilitre","Milliliter","mililitro","millilitro","mililitre","ملليلتر","मिलीलीटर","毫升","ミリリットル","밀리리터","millilitr"),
 "gal" to listOf("галлон США","US gallon","АҚШ галлоны","galón estadounidense","gallon américain","US-Gallone","galão americano","gallone USA","ABD galonu","جالون أمريكي","अमेरिकी गैलन","美制加仑","米ガロン","미국 갤런","AQSh galloni"),
 "c" to listOf("градус Цельсия","degree Celsius","Цельсий градусы","grado Celsius","degré Celsius","Grad Celsius","grau Celsius","grado Celsius","Santigrat derece","درجة مئوية","डिग्री सेल्सियस","摄氏度","摂氏度","섭씨","Selsiy darajasi"),
 "f" to listOf("градус Фаренгейта","degree Fahrenheit","Фаренгейт градусы","grado Fahrenheit","degré Fahrenheit","Grad Fahrenheit","grau Fahrenheit","grado Fahrenheit","Fahrenheit derece","درجة فهرنهايت","डिग्री फ़ारेनहाइट","华氏度","華氏度","화씨","Farengeyt darajasi"),
 "kmh" to listOf("километр в час","kilometer per hour","сағатына километр","kilómetro por hora","kilomètre par heure","Kilometer pro Stunde","quilômetro por hora","chilometro orario","saatte kilometre","كيلومتر في الساعة","किलोमीटर प्रति घंटा","千米每小时","キロメートル毎時","킬로미터 매시간","soatiga kilometr"),
 "mph" to listOf("миля в час","mile per hour","сағатына миль","milla por hora","mille par heure","Meile pro Stunde","milha por hora","miglio orario","saatte mil","ميل في الساعة","मील प्रति घंटा","英里每小时","マイル毎時","마일 매시간","soatiga mil")
)
internal fun unitDisplayName(unitId: String, language: String): String {
    val index = regionalTags.indexOf(language).takeIf { it >= 0 } ?: 1
    return localizedUnits[unitId]?.getOrNull(index) ?: unitId
}
