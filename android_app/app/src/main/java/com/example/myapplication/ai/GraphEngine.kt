package com.example.myapplication.ai

class GraphEngine(private val edges: List<DiscountEdge>) {

    fun findDiscount(card: String, service: String): List<DiscountEdge> {
        return edges.filter { it.from == card && it.to == service }
    }

    companion object {
        fun default(): GraphEngine {
            return GraphEngine(
                listOf(
                    DiscountEdge("KB_NORI2", "NETFLIX", "20%", "전월 실적 30만원"),
                    DiscountEdge("KB_NORI2", "YOUTUBE_PREMIUM", "10%", "전월 실적 30만원")
                )
            )
        }
    }
}
