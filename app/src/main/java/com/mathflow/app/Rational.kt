package com.mathflow.app

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

/**
 * Exact rational number. Numerator and denominator are always reduced;
 * denominator is positive and never zero.
 */
data class Rational private constructor(
    val numerator: BigInteger,
    val denominator: BigInteger
) : Comparable<Rational> {
    companion object {
        val ZERO = of(BigInteger.ZERO, BigInteger.ONE)
        val ONE = of(BigInteger.ONE, BigInteger.ONE)
        private val TEN = BigInteger.TEN

        fun of(numerator: BigInteger, denominator: BigInteger): Rational {
            require(denominator != BigInteger.ZERO) { "Division by zero" }
            var n = numerator
            var d = denominator
            if (d.signum() < 0) {
                n = n.negate()
                d = d.negate()
            }
            val gcd = n.abs().gcd(d)
            return Rational(n.divide(gcd), d.divide(gcd))
        }

        fun of(value: Long): Rational = of(BigInteger.valueOf(value), BigInteger.ONE)

        /** Parses a decimal exactly: 0.125 becomes 1/8, not a floating point approximation. */
        fun parseDecimal(value: String): Rational {
            val decimal = BigDecimal(value)
            val scale = decimal.scale().coerceAtLeast(0)
            val unscaled = decimal.unscaledValue()
            return if (scale == 0) of(unscaled, BigInteger.ONE)
            else of(unscaled, TEN.pow(scale))
        }
    }


    override fun compareTo(other: Rational): Int =
        (numerator * other.denominator).compareTo(other.numerator * denominator)

    operator fun plus(other: Rational) =
        of(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator)

    operator fun minus(other: Rational) =
        of(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator)

    operator fun times(other: Rational) =
        of(numerator * other.numerator, denominator * other.denominator)

    operator fun div(other: Rational): Rational {
        require(other.numerator != BigInteger.ZERO) { "Division by zero" }
        return of(numerator * other.denominator, denominator * other.numerator)
    }

    operator fun unaryMinus() = of(numerator.negate(), denominator)

    fun pow(exponent: Int): Rational {
        require(exponent >= -1000 && exponent <= 1000) { "Exponent is too large" }
        if (exponent == 0) return ONE
        if (exponent < 0) {
            require(numerator != BigInteger.ZERO) { "Division by zero" }
            return of(denominator.pow(-exponent), numerator.pow(-exponent))
        }
        return of(numerator.pow(exponent), denominator.pow(exponent))
    }

    fun toBigDecimal(mathContext: MathContext = MathContext(24, RoundingMode.HALF_EVEN)): BigDecimal =
        BigDecimal(numerator).divide(BigDecimal(denominator), mathContext)

    fun toDisplayString(): String {
        if (denominator == BigInteger.ONE) return numerator.toString()
        return "$numerator/$denominator"
    }

    fun toMixedDisplayString(): String {
        if (denominator == BigInteger.ONE) return numerator.toString()
        val whole = numerator.divide(denominator)
        val remainder = numerator.remainder(denominator).abs()
        return if (whole == BigInteger.ZERO) toDisplayString()
        else "$whole $remainder/$denominator"
    }

    fun toDecimalString(scale: Int = 10): String =
        toBigDecimal(MathContext(scale, RoundingMode.HALF_UP))
            .stripTrailingZeros().toPlainString()

    fun isInteger(): Boolean = denominator == BigInteger.ONE
}
