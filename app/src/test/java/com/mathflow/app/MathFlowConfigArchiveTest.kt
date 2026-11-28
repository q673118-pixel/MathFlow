package com.mathflow.app

import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class MathFlowConfigArchiveTest {
    @Test
    fun roundTripPreservesSettingsAndEmbeddedFont() {
        val settings = JSONObject()
            .put("theme", "ocean")
            .put("fontPath", "/private/path/custom.ttf")
            .put("preferences", JSONObject().put("intro_speed", 1.4))
        val font = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val output = ByteArrayOutputStream()

        MathFlowConfigArchive.write(output, "Ocean setup", settings, "custom_font.ttf", font)

        val bytes = output.toByteArray()
        assertTrue(bytes.size > font.size)
        assertEquals('P'.code.toByte(), bytes[0])
        assertEquals('K'.code.toByte(), bytes[1])

        val imported = MathFlowConfigArchive.read(ByteArrayInputStream(bytes))
        assertEquals("Ocean setup", imported.name)
        assertEquals("ocean", imported.settings.getString("theme"))
        assertEquals(1.4, imported.settings.getJSONObject("preferences").getDouble("intro_speed"), 0.0001)
        assertEquals("custom_font.ttf", imported.fontFileName)
        assertArrayEquals(font, imported.fontBytes)
    }

    @Test
    fun roundTripWithoutCustomFont() {
        val output = ByteArrayOutputStream()
        MathFlowConfigArchive.write(output, "Default", JSONObject().put("theme", "vanilla"))

        val imported = MathFlowConfigArchive.read(ByteArrayInputStream(output.toByteArray()))
        assertEquals("Default", imported.name)
        assertNull(imported.fontFileName)
        assertNull(imported.fontBytes)
    }
}
