package com.mathflow.app

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

internal data class MathFlowConfigProfile(
    val id: String,
    val name: String,
    val settings: JSONObject
)

internal object MathFlowConfigProfiles {
    private const val PREF_KEY = "mathflow_config_profiles"

    fun load(context: android.content.Context): List<MathFlowConfigProfile> = runCatching {
        val raw = context.getSharedPreferences("mathflow_settings", android.content.Context.MODE_PRIVATE)
            .getString(PREF_KEY, "[]") ?: "[]"
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val id = item.optString("id")
                val name = item.optString("name").trim()
                val settings = item.optJSONObject("settings") ?: continue
                if (id.isNotBlank() && name.isNotBlank()) {
                    add(MathFlowConfigProfile(id, name.take(48), JSONObject(settings.toString())))
                }
            }
        }
    }.getOrDefault(emptyList())

    fun save(context: android.content.Context, profiles: List<MathFlowConfigProfile>) {
        val array = JSONArray()
        profiles.forEach { profile ->
            array.put(JSONObject().put("id", profile.id).put("name", profile.name)
                .put("settings", JSONObject(profile.settings.toString())))
        }
        context.getSharedPreferences("mathflow_settings", android.content.Context.MODE_PRIVATE)
            .edit().putString(PREF_KEY, array.toString()).apply()
    }

    fun create(name: String, settings: JSONObject) = MathFlowConfigProfile(
        UUID.randomUUID().toString(), name.trim().take(48), JSONObject(settings.toString())
    )

    fun installFont(context: android.content.Context, fileName: String?, bytes: ByteArray?): String? {
        if (fileName == null || bytes == null) return null
        require(bytes.isNotEmpty() && bytes.size <= 8 * 1024 * 1024) { "Invalid font size" }
        val ext = fileName.substringAfterLast('.', "").lowercase()
        require(ext == "ttf" || ext == "otf") { "Unsupported font type" }
        val dir = File(context.filesDir, "fonts").apply { mkdirs() }
        val target = File(dir, "mathflow_profile_${UUID.randomUUID()}.$ext")
        target.writeBytes(bytes)
        try {
            android.graphics.Typeface.createFromFile(target)
        } catch (error: Throwable) {
            target.delete()
            throw IllegalArgumentException("Font file is invalid", error)
        }
        return target.absolutePath
    }
}
