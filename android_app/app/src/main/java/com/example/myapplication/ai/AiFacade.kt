package com.example.myapplication.ai

class AiFacade(
    private val classifier: SubscriptionClassifier = SubscriptionClassifier(),
    private val optimizer: BenefitOptimizer = BenefitOptimizer(),
    private val rag: RagEngine = RagEngine.default(),
    private val graph: GraphEngine = GraphEngine.default()
) {

    fun classify(text: String, amount: Int? = null, timestamp: String? = null): ClassificationResult {
        return classifier.classifyTransaction(text, amount, timestamp)
    }

    fun optimize(
        userCards: List<String>,
        serviceToCategory: Map<String, String>,
        subs: List<SubscriptionItem>,
        usage: List<UsageLog>,
        payments: List<PaymentLog>
    ): OptimizationResult {
        return optimizer.optimize(userCards, serviceToCategory, subs, usage, payments)
    }

    fun chat(question: String, card: String? = null, service: String? = null): String {
        val hits = rag.search(question, k = 2)
        val discounts = if (card != null && service != null) graph.findDiscount(card, service) else emptyList()

        val sb = StringBuilder()
        sb.append("질문: ").append(question).append("\n")

        if (hits.isNotEmpty()) {
            sb.append("\n[약관/FAQ 근거 요약]\n")
            hits.forEach { sb.append("- ").append(it.text.take(200)).append("\n") }
        }

        if (discounts.isNotEmpty()) {
            sb.append("\n[카드/제휴 혜택]\n")
            discounts.forEach { sb.append("- 할인: ").append(it.amount).append(" / 조건: ").append(it.condition).append("\n") }
        }

        if (hits.isEmpty() && discounts.isEmpty()) {
            sb.append("\n현재 저장된 지식에서 근거를 찾지 못했어요. 데이터 확장이 필요해요.")
        }

        return sb.toString()
    }
}
