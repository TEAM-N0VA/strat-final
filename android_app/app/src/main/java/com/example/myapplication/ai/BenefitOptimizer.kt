package com.example.myapplication.ai

import kotlin.math.min

class BenefitOptimizer(
    private val benefitRules: BenefitRules = BenefitRules.default()
) {

    // category 별 사용량 기준
    private val categoryValueModel = mapOf(
        "OTT" to Pair("time_minutes", 600.0),
        "MUSIC" to Pair("time_minutes", 300.0),
        "DELIVERY" to Pair("count", 4.0),
        "SHOPPING" to Pair("count", 2.0),
        "COFFEE" to Pair("count", 8.0)
    )

    fun optimize(
        userCards: List<String>,
        serviceToCategory: Map<String, String>,
        subscriptions: List<SubscriptionItem>,
        usageLogs: List<UsageLog>,
        paymentLogs: List<PaymentLog>
    ): OptimizationResult {

        val stats = aggregateUsage(usageLogs, serviceToCategory)
        val persona = inferPersona(stats)

        val efficiencyScores = subscriptions.associate { sub ->
            sub.service to efficiencyScoreForService(sub, usageLogs, serviceToCategory)
        }

        val recs = mutableListOf<Map<String, Any>>()

        // 1) low efficiency -> cancel/downgrade
        subscriptions.forEach { sub ->
            val score = efficiencyScores[sub.service] ?: 0.0
            val price = sub.price ?: 0
            if (score < 25.0 && price > 0) {
                recs += mapOf(
                    "type" to "CANCEL_OR_DOWNGRADE",
                    "service" to sub.service,
                    "reason" to "효율 점수 $score (낮음). 사용량 대비 가격이 큼.",
                    "action" to "해지 또는 더 저렴한 요금제로 변경"
                )
            }
        }

        // 2) card benefit optimization
        paymentLogs.forEach { p ->
            val best = userCards
                .map { card -> benefitRules.estimateDiscount(card, p.service, p.amount) }
                .maxByOrNull { it.discountAmount }

            if (best != null && best.discountAmount > 0) {
                recs += mapOf(
                    "type" to "SWITCH_PAYMENT_METHOD",
                    "service" to p.service,
                    "reason" to "${best.card}로 결제 시 예상 할인 ${best.discountAmount}원",
                    "action" to "결제카드를 ${best.card}로 변경",
                    "details" to mapOf(
                        "rate" to best.rate,
                        "cap" to best.cap,
                        "condition" to best.condition
                    )
                )
            }
        }

        val summary = "페르소나: $persona. 비효율 구독 ${efficiencyScores.values.count { it < 25.0 }}개 감지. 카드 최적화 추천 ${recs.count { it["type"] == "SWITCH_PAYMENT_METHOD" }}개."

        return OptimizationResult(
            persona = persona,
            efficiencyScores = efficiencyScores,
            recommendations = recs,
            summary = summary
        )
    }

    private fun aggregateUsage(
        usageLogs: List<UsageLog>,
        serviceToCategory: Map<String, String>
    ): Map<Pair<String, String>, Double> {
        val m = mutableMapOf<Pair<String, String>, Double>()
        usageLogs.forEach { u ->
            val cat = serviceToCategory[u.service] ?: "UNKNOWN"
            val key = Pair(cat, u.metricType)
            m[key] = (m[key] ?: 0.0) + u.value
        }
        return m
    }

    private fun inferPersona(stats: Map<Pair<String, String>, Double>): String {
        val ott = stats[Pair("OTT", "time_minutes")] ?: 0.0
        val del = stats[Pair("DELIVERY", "count")] ?: 0.0
        val disc = stats[Pair("DISCOUNT", "amount")] ?: 0.0

        return when {
            ott >= 1200.0 -> "콘텐츠 매니아"
            del >= 8.0 -> "배달 헤비유저"
            disc >= 30000.0 -> "절약형 소비자"
            stats.values.sum() < 200.0 -> "라이트 유저"
            else -> "일반 사용자"
        }
    }

    private fun efficiencyScoreForService(
        sub: SubscriptionItem,
        usageLogs: List<UsageLog>,
        serviceToCategory: Map<String, String>
    ): Double {
        val svc = sub.service
        val cat = serviceToCategory[svc] ?: "UNKNOWN"
        val price = sub.price ?: 0

        val model = categoryValueModel[cat]
        val metricTarget = model?.first
        val threshold = model?.second ?: 1.0

        val usageSum = usageLogs
            .filter { it.service == svc && (metricTarget == null || it.metricType == metricTarget) }
            .sumOf { it.value }

        val usageFactor = min(usageSum / threshold, 1.0)
        val priceFactor = if (price > 0) 1.0 / (1.0 + (price / 10000.0)) else 1.0

        return ((100.0 * usageFactor * priceFactor) * 100.0).toInt() / 100.0
    }
}

/** ---- Benefit Rules ---- */

data class DiscountEstimate(
    val card: String,
    val service: String,
    val discountAmount: Int,
    val condition: String,
    val rate: Double,
    val cap: Int
)

class BenefitRules(
    private val cards: List<CardRule>
) {
    data class CardRule(val name: String, val rules: List<ServiceRule>)
    data class ServiceRule(val service: String, val discountRate: Double, val monthlyCap: Int, val condition: String)

    fun estimateDiscount(cardName: String, service: String, amount: Int): DiscountEstimate {
        val card = cards.find { it.name == cardName } ?: return DiscountEstimate(cardName, service, 0, "", 0.0, 0)
        val rule = card.rules.find { it.service == service } ?: return DiscountEstimate(cardName, service, 0, "", 0.0, 0)

        var disc = (amount * rule.discountRate).toInt()
        if (rule.monthlyCap > 0) disc = min(disc, rule.monthlyCap)

        return DiscountEstimate(cardName, service, disc, rule.condition, rule.discountRate, rule.monthlyCap)
    }

    companion object {
        fun default(): BenefitRules {
            return BenefitRules(
                cards = listOf(
                    CardRule(
                        name = "KB_NORI2",
                        rules = listOf(
                            ServiceRule("NETFLIX", 0.2, 5000, "전월 실적 30만원 이상"),
                            ServiceRule("YOUTUBE_PREMIUM", 0.1, 3000, "전월 실적 30만원 이상")
                        )
                    )
                )
            )
        }
    }
}
