package com.example.myapplication.ai

class SubscriptionClassifier {

    private val serviceToCategory = mutableMapOf(
        "NETFLIX" to "OTT",
        "YOUTUBE_PREMIUM" to "OTT",
        "DISNEY_PLUS" to "OTT",
        "WATCHA" to "OTT",
        "WAVVE" to "OTT",
        "TVING" to "OTT",
        "COUPANG_PLAY" to "OTT",
        "SPOTIFY" to "MUSIC",
        "MELON" to "MUSIC",
        "GENIE" to "MUSIC",
        "BAEMIN" to "DELIVERY",
        "YOGIYO" to "DELIVERY"
    )

    fun classifyTransaction(text: String, amount: Int? = null, timestamp: String? = null): ClassificationResult {
        val norm = TextNormalizer.basicNormalize(text)
        val merchant = TextNormalizer.extractMerchant(norm)

        val (svc, conf) = TextNormalizer.matchService(norm)
        val category = svc?.let { serviceToCategory[it] }

        val (periodDays, periodReason) = PeriodicityInferer.inferPeriodDays(text)

        return ClassificationResult(
            normalizedText = norm,
            merchant = merchant,
            service = svc,
            category = category,
            confidence = conf,
            periodDays = periodDays,
            periodReason = periodReason,
            meta = mapOf("amount" to amount, "timestamp" to timestamp)
        )
    }
}
