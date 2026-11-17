package com.mathflow.app

data class LinearSystemSolution(val kind: String, val answer: String, val steps: List<String>)

/** Exact Gaussian elimination for systems with two or three variables. */
class LinearSystemSolver {
    private data class Form(val c: List<Rational>) {
        operator fun plus(o: Form) = Form(c.indices.map { c[it] + o.c[it] })
        operator fun minus(o: Form) = Form(c.indices.map { c[it] - o.c[it] })
        operator fun unaryMinus() = Form(c.map { -it })
        operator fun times(k: Rational) = Form(c.map { it * k })
        operator fun div(k: Rational) = Form(c.map { it / k })
        val isConstant get() = c.dropLast(1).all { it == Rational.ZERO }
    }

    fun solve(equations: List<String>): LinearSystemSolution {
        require(equations.size in 2..3) { "Введите 2 или 3 уравнения" }
        val variables = listOf("x", "y", "z").filter { v -> equations.any { Regex("(?i)$v").containsMatchIn(it) } }
        require(variables.size in 2..3) { "Система должна содержать 2 или 3 неизвестных: x, y, z" }
        val rows = equations.map { equation ->
            val eq = equation.indexOf('=')
            require(eq > 0 && equation.indexOf('=', eq + 1) < 0) { "В каждом уравнении должен быть один знак =" }
            val left = Parser(equation.substring(0, eq), variables).parse()
            val right = Parser(equation.substring(eq + 1), variables).parse()
            left - right
        }
        val n = variables.size
        val matrix = rows.map { f -> (0 until n).map { f.c[it] }.toMutableList().apply { add(-f.c.last()) } }.toMutableList()
        val steps = mutableListOf<String>()
        steps += "Записываем систему в расширенную матрицу:"
        steps += matrix.joinToString("\n") { row -> row.joinToString("  ") { it.toDisplayString() } }
        var pivotRow = 0
        val pivotColumns = mutableListOf<Int>()
        for (column in 0 until n) {
            val candidate = (pivotRow until matrix.size).firstOrNull { matrix[it][column] != Rational.ZERO } ?: continue
            if (candidate != pivotRow) {
                val temp = matrix[pivotRow]; matrix[pivotRow] = matrix[candidate]; matrix[candidate] = temp
                steps += "Меняем местами строки ${pivotRow + 1} и ${candidate + 1}"
            }
            val pivot = matrix[pivotRow][column]
            if (pivot != Rational.ONE) {
                matrix[pivotRow] = matrix[pivotRow].map { it / pivot }.toMutableList()
                steps += "Делим строку ${pivotRow + 1} на ${pivot.toDisplayString()}"
            }
            for (row in matrix.indices) {
                if (row == pivotRow) continue
                val factor = matrix[row][column]
                if (factor == Rational.ZERO) continue
                matrix[row] = matrix[row].indices.map { matrix[row][it] - factor * matrix[pivotRow][it] }.toMutableList()
                steps += "Из строки ${row + 1} вычитаем ${factor.toDisplayString()} · строку ${pivotRow + 1}"
            }
            pivotColumns += column
            pivotRow++
            if (pivotRow == matrix.size) break
        }
        steps += "Приведённая матрица:"
        steps += matrix.joinToString("\n") { row -> row.joinToString("  ") { it.toDisplayString() } }
        val badRow = matrix.firstOrNull { row -> row.take(n).all { it == Rational.ZERO } && row[n] != Rational.ZERO }
        if (badRow != null) {
            steps += "Получено противоречие: 0 = ${badRow[n].toDisplayString()}"
            return LinearSystemSolution("no_solution", "Система несовместна: решений нет", steps)
        }
        if (pivotColumns.size < n) {
            val free = variables.filterIndexed { index, _ -> index !in pivotColumns }
            steps += "Свободные переменные: ${free.joinToString()}"
            return LinearSystemSolution("infinite", "Система имеет бесконечно много решений", steps)
        }
        val values = MutableList(n) { Rational.ZERO }
        pivotColumns.forEachIndexed { row, column -> values[column] = matrix[row][n] }
        val answer = variables.indices.joinToString(", ") { "${variables[it]} = ${values[it].toDisplayString()}" }
        steps += "Ответ: $answer"
        return LinearSystemSolution("unique", answer, steps)
    }

    private class Parser(source: String, private val variables: List<String>) {
        private val input = source.replace("−", "-").replace("×", "*").replace("÷", "/").filterNot(Char::isWhitespace)
        private val tokens = Regex("""\d+(?:[.,]\d+)?|[xyzXYZ]|[()+\-*/]""").findAll(input).map { it.value }.toList()
        private var index = 0
        init { require(input.isNotEmpty() && tokens.joinToString("") == input) { "Недопустимый символ в выражении: $source" } }
        fun parse(): Form { val v = expression(); require(index == tokens.size) { "Проверьте запись выражения" }; return v }
        private fun expression(): Form {
            var v = term()
            while (peek() == "+" || peek() == "-") { val op = take(); val r = term(); v = if (op == "+") v + r else v - r }
            return v
        }
        private fun term(): Form {
            var v = unary()
            while (peek() == "*" || peek() == "/" || startsPrimary(peek())) {
                val op = if (peek() == "*" || peek() == "/") take() else "*"
                val r = unary()
                if (op == "/") { require(r.isConstant && r.c.last() != Rational.ZERO) { "Делить можно только на ненулевое число" }; v = v / r.c.last() }
                else { require(v.isConstant || r.isConstant) { "Поддерживаются только линейные выражения" }; v = if (v.isConstant) r * v.c.last() else v * r.c.last() }
            }
            return v
        }
        private fun startsPrimary(t: String?) = t == "(" || t?.firstOrNull()?.isDigit() == true || t?.lowercase() in variables
        private fun unary(): Form = when (peek()) { "+" -> { take(); unary() }; "-" -> { take(); -unary() }; else -> primary() }
        private fun primary(): Form {
            val t = take(); val size = variables.size + 1
            if (t == "(") { val v = expression(); require(take() == ")") { "Не закрыта скобка" }; return v }
            val vi = variables.indexOf(t.lowercase())
            if (vi >= 0) return Form(List(size) { if (it == vi) Rational.ONE else Rational.ZERO })
            val number = Rational.parseDecimal(t.replace(',', '.'))
            return Form(List(size) { if (it == size - 1) number else Rational.ZERO })
        }
        private fun peek() = tokens.getOrNull(index)
        private fun take() = tokens.getOrNull(index++) ?: throw IllegalArgumentException("Неожиданный конец выражения")
    }
}
