package com.catmistry.android

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

// Interfaces
interface RecyclerViewClickListener {
    fun itemClicked(itemID: Int)
}

