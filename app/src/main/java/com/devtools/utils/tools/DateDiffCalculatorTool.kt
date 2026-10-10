package com.devtools.utils.tools

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class DateDiffCalculatorTool {
    fun calculateDateDiff(startDate: String, endDate: String): Map<String, Int> {
        val start = parseDate(startDate, "البداية")
        val end = parseDate(endDate, "النهاية")
        val period = Period.between(start, end)
        val totalDays = ChronoUnit.DAYS.between(start, end)
        return mapOf(
            "years" to period.years,
            "months" to period.months,
            "days" to period.days,
            "totalDays" to totalDays.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
        )
    }
    private fun parseDate(value: String, label: String): LocalDate {
        require(value.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) { "تاريخ $label يجب أن يكون بصيغة YYYY-MM-DD." }
        return try {
            LocalDate.parse(value)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("تاريخ $label غير صالح. استخدم YYYY-MM-DD.", e)
        }
    }
}
