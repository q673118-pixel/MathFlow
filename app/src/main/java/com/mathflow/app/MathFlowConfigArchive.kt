package com.mathflow.app

import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Portable MathFlow .mf archive primitives.
 *
 * Archive layout:
 *   manifest.json
 *   fonts/custom_font.ttf (optional)
 *
 * The settings object is deliberately separate from calculation history.
 */
internal object MathFlowConfigArchive {
    private const val FORMAT_VERSION = 1
    private const val APP_ID = "com.mathflow.app"
    private const val MANIFEST_ENTRY = "manifest.json"
    private const val FONT_PREFIX = "fonts/"
    private const val MAX_ARCHIVE_BYTES = 12L * 1024L * 1024L
    private const val MAX_FONT_BYTES = 8L * 1024L * 1024L
    private const val MAX_ENTRIES = 8

    data class ImportedConfig(
        val name: String,
        val settings: JSONObject,
        val fontFileName: String?,
        val fontBytes: ByteArray?
    )

    fun write(
        output: OutputStream,
        name: String,
        settings: JSONObject,
        fontFileName: String? = null,
        fontBytes: ByteArray? = null
    ) {
        require(name.isNotBlank() && name.length <= 48) { "Invalid configuration name" }
        require(fontBytes == null || fontBytes.size <= MAX_FONT_BYTES) { "Font file is too large" }
        require((fontFileName == null) == (fontBytes == null)) { "Font name and data must be provided together" }

        val manifest = JSONObject()
            .put("format", "MathFlow")
            .put("appId", APP_ID)
            .put("formatVersion", FORMAT_VERSION)
            .put("name", name.trim())
            .put("createdAt", System.currentTimeMillis())
            .put("settings", JSONObject(settings.toString()))

        if (fontFileName != null) {
            val safeName = safeFontName(fontFileName)
            manifest.put("fontEntry", FONT_PREFIX + safeName)
        }

        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
            zip.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            if (fontFileName != null && fontBytes != null) {
                zip.putNextEntry(ZipEntry(FONT_PREFIX + safeFontName(fontFileName)))
                zip.write(fontBytes)
                zip.closeEntry()
            }
            zip.finish()
        }
    }

    fun read(input: InputStream): ImportedConfig {
        val entries = LinkedHashMap<String, ByteArray>()
        var totalBytes = 0L
        var count = 0

        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                count++
                require(count <= MAX_ENTRIES) { "Too many archive entries" }
                require(!entry.isDirectory) { "Directories are not allowed in .mf" }

                val name = entry.name
                require(name == MANIFEST_ENTRY || (name.startsWith(FONT_PREFIX) && name.count { it == '/' } == 1)) {
                    "Unexpected archive entry"
                }
                require(!name.contains("..") && !name.startsWith("/") && !name.contains("\\\\")) {
                    "Unsafe archive path"
                }
                require(!entries.containsKey(name)) { "Duplicate archive entry" }

                val limit = if (name == MANIFEST_ENTRY) 256 * 1024 else MAX_FONT_BYTES.toInt()
                val buffer = ByteArrayOutputStream()
                val chunk = ByteArray(8192)
                while (true) {
                    val read = zip.read(chunk)
                    if (read < 0) break
                    totalBytes += read
                    require(totalBytes <= MAX_ARCHIVE_BYTES) { "Archive is too large" }
                    require(buffer.size() + read <= limit) { "Archive entry is too large" }
                    buffer.write(chunk, 0, read)
                }
                entries[name] = buffer.toByteArray()
                zip.closeEntry()
            }
        }

        val manifestBytes = requireNotNull(entries[MANIFEST_ENTRY]) { "manifest.json is missing" }
        val manifest = JSONObject(manifestBytes.toString(Charsets.UTF_8))
        require(manifest.optString("format") == "MathFlow") { "Not a MathFlow configuration" }
        require(manifest.optString("appId") == APP_ID) { "Configuration belongs to another app" }
        require(manifest.optInt("formatVersion", -1) == FORMAT_VERSION) { "Unsupported configuration version" }

        val name = manifest.optString("name").trim()
        require(name.isNotEmpty() && name.length <= 48) { "Invalid configuration name" }
        val settings = manifest.optJSONObject("settings") ?: error("Settings are missing")
        val fontEntry = manifest.optString("fontEntry").takeIf { it.isNotBlank() }
        val fontBytes = if (fontEntry != null) {
            require(fontEntry.startsWith(FONT_PREFIX) && fontEntry.count { it == '/' } == 1) {
                "Invalid font reference"
            }
            require(fontEntry.substringAfterLast('/').matches(Regex("[A-Za-z0-9._-]{1,80}\\.(ttf|otf)", RegexOption.IGNORE_CASE))) {
                "Invalid font filename"
            }
            requireNotNull(entries[fontEntry]) { "Font data is missing" }
        } else null

        require(entries.keys.all { it == MANIFEST_ENTRY || it == fontEntry }) {
            "Archive contains unreferenced data"
        }

        return ImportedConfig(name, JSONObject(settings.toString()), fontEntry?.substringAfterLast('/'), fontBytes)
    }

    private fun safeFontName(value: String): String {
        val name = value.substringAfterLast('/').substringAfterLast('\\')
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(80)
        require(name.matches(Regex("[A-Za-z0-9._-]{1,76}\\.(ttf|otf)", RegexOption.IGNORE_CASE))) {
            "Only TTF and OTF fonts are supported"
        }
        return name
    }
}
