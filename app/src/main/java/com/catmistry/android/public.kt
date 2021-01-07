package com.catmistry.android

import androidx.fragment.app.Fragment

// Data classes
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

// Interfaces
interface RecyclerViewClickListener {
    fun itemClicked(itemID: Int)
}

