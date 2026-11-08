package com.mathflow.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UnitConverterTest {
    @Test fun convertsKilometresToMetres() =
        assertEquals("1250", UnitConverter.convert("1.25", "length", "km", "m"))

    @Test fun convertsPoundsToKilograms() =
        assertEquals("0.45359237", UnitConverter.convert("1", "mass", "lb", "kg"))

    @Test fun convertsFahrenheitToCelsius() =
        assertEquals("0", UnitConverter.convert("32", "temperature", "f", "c"))

    @Test fun convertsCelsiusToFahrenheit() =
        assertEquals("212", UnitConverter.convert("100", "temperature", "c", "f"))

    @Test fun convertsKelvinToCelsius() =
        assertEquals("-273.15", UnitConverter.convert("0", "temperature", "k", "c"))

    @Test fun rejectsTemperatureBelowAbsoluteZero() {
        assertThrows(IllegalArgumentException::class.java) {
            UnitConverter.convert("-1", "temperature", "k", "c")
        }
    }

    @Test fun convertsDecimalAndBinaryDataUnits() {
        assertEquals("1000", UnitConverter.convert("1", "data", "gb", "mb"))
        assertEquals("1000", UnitConverter.convert("1", "data", "kb", "b"))
        assertEquals("1024", UnitConverter.convert("1", "data", "kib", "b"))
    }

    @Test fun acceptsCommaDecimalSeparator() =
        assertEquals("250", UnitConverter.convert("2,5", "length", "m", "cm"))

    @Test fun convertsMilesPerHourToKilometresPerHour() =
        assertEquals("1.609344", UnitConverter.convert("1", "speed", "mph", "kmh"))

    @Test fun rejectsUnknownUnits() {
        assertThrows(IllegalArgumentException::class.java) {
            UnitConverter.convert("1", "length", "kg", "m")
        }
    }

    @Test fun rejectsInvalidNumbers() {
        assertThrows(IllegalArgumentException::class.java) {
            UnitConverter.convert("abc", "length", "m", "km")
        }
    }
}
