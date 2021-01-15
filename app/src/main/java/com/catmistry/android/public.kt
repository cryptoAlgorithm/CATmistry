package com.catmistry.android

import kotlin.math.roundToInt

// Data classes
// I actually put sensible defaults
data class LearnQns (
    var correctAnswer: Int? = 0,
    var question: String? = "",
    var options: List<String>? = null
)
data class LearnTopics (
    var icon: String? = "gas_tests",
    var title: String? = "Gas Tests",
    var unlockPoints: Int? = 0
)
data class SubTopicContent (
    var content: String? = "Failed to retrieve content",
    var appBarTitle: String? = "Gas Tests",
    var bottomImage: String? = "gas_tests",
    var showPHSlider: Boolean? = false
)
data class PhArray (
    var pHImg: String? = "ph_0",
    var pHDesc: String? = "Stomach Acid",
)
data class NestedSubTopicContent (
    var firstContent: String? = "Failed to load data",
    var secondContent: String? = "",
    var thirdContent: String? = "",
    var showPHSlider: Boolean? = false
)

// Interfaces
interface RecyclerViewClickListener {
    fun itemClicked(itemID: Int)
}

// Extension functions
fun Float.round(decimals: Int): Float {
    var dotAt = 1
    repeat(decimals) { dotAt *= 10 }
    val roundedValue = (this * dotAt).roundToInt()
    return (roundedValue / dotAt) + (roundedValue % dotAt).toFloat() / dotAt
}

