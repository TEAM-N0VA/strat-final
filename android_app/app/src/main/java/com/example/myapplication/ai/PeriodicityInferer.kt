package com.example.myapplication.ai

object PeriodicityInferer {

    fun inferPeriodDays(rawText: String): Pair<Int?, String> {
        val t = rawText.uppercase()

        fun hasAny(vararg keys: String) = keys.any { t.contains(it.uppercase()) }

        if (hasAny("매달", "매월", "월간", "1개월", "MONTHLY", "PER MONTH")) return 30 to "keyword_monthly"
        if (hasAny("매년", "연간", "1년", "YEARLY", "ANNUAL")) return 365 to "keyword_yearly"
        if (hasAny("주간", "매주", "WEEKLY", "PER WEEK")) return 7 to "keyword_weekly"

        // amount heuristics
        val amount = extractAmount(t)
        if (amount != null) {
            if (amount in 9000..25000) return 30 to "amount_range_typical_monthly"
            if (amount in 80000..200000) return 365 to "amount_range_typical_yearly"
        }

        return null to "unknown"
    }

    private fun extractAmount(t: String): Int? {
        val m = Regex("(\\d{1,3}(?:,\\d{3})+|\\d+)\\s*(원|KRW)?").find(t) ?: return null
        return m.groupValues[1].replace(",", "").toIntOrNull()
    }
}
