package com.mathflow.app

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementSettingsTest {
    @Test fun automaticUsesUsCustomaryUnitsOnlyForUsRegion() {
        assertEquals("us", resolveMeasurementSystem("auto", Locale.US))
        assertEquals("metric", resolveMeasurementSystem("auto", Locale("ru", "KZ")))
        assertEquals("metric", resolveMeasurementSystem("auto", Locale.UK))
    }

    @Test fun explicitSelectionOverridesDeviceRegion() {
        assertEquals("metric", resolveMeasurementSystem("metric", Locale.US))
        assertEquals("us", resolveMeasurementSystem("us", Locale("ru", "KZ")))
    }

    @Test fun preferredUnitPairsMatchSelectedSystem() {
        assertEquals("mi" to "ft", preferredUnits("length", "us"))
        assertEquals("lb" to "oz", preferredUnits("mass", "us"))
        assertEquals("f" to "c", preferredUnits("temperature", "us"))
        assertEquals("km" to "m", preferredUnits("length", "metric"))
        assertEquals("kg" to "g", preferredUnits("mass", "metric"))
        assertEquals("c" to "k", preferredUnits("temperature", "metric"))
    }

    @Test fun convertsNewSquareFootUnit() {
        assertEquals("0.09290304", UnitConverter.convert("1", "area", "ft2", "m2"))
    }
}
