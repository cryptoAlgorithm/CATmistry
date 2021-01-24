package com.catmistry.android

import kotlin.math.roundToInt

// Data classes
// I actually put sensible defaults (in case the database gets screwed)
data class LearnQns (
    var correctAnswer: Int? = 0,
    var question: String? = "",
    var options: List<String>? = null
)
data class HomeTopics (
    var icon: String? = "gas_tests",
    var title: String? = "Gas Tests",
    var unlockPoints: Int? = 0,
    var description: String? = "No description",
    var gameIcon: String? = "cat_happy",
)
data class SubTopicContent (
    var content: String? = "Failed to retrieve content",
    var appBarTitle: String? = "Gas Tests",
    var bottomImage: String? = "gas_tests",
    var showPHSlider: Boolean? = false
)
data class PhArray(
    var phImg: String? = "ph_0",
    var phDesc: String? = "Stomach Acid",
)
data class NestedSubTopicContent (
    var firstContent: String? = "Failed to load data",
    var secondContent: String? = "",
    var thirdContent: String? = "",
    var emphasisText: String? = null, // These must be null, otherwise the defaults will be shown when there is no data rather than hiding the box
    var lowPH: Float? = null,
    var highPH: Float? = null,
    var lowPHColor: String? = "FF0000",
    var midPHColor: String? = "00FF00",
    var highPHColor: String? = "0000FF",
    var lowPHDesc: String? = "Red",
    var midPHDesc: String? = "Green",
    var highPHDesc: String? = "Blue",
    var mainImg: String? = "gas_tests"
)
data class GasGameArray(
    var correctGasTank: String? = "Hydrogen",
    var firstGasIsPositive: List<Boolean>? = null,
    var firstGasTank: String? = "Database Error",
    var forthGasIsPositive: List<Boolean>? = null,
    var fourthGasTank: String? = "Database Error",
    var secondGasIsPositive: List<Boolean>? = null,
    var secondGasTank: String? = "Database Error",
    var thirdGasIsPositive: List<Boolean>? = null,
    var thirdGasTank: String? = "Database Error",
)

// Interfaces
interface RecyclerViewClickListener {
    fun itemClicked(itemID: Int)
}
interface TableRowClickListener {
    fun rowClicked(itemID: Int)
}

// Extension functions
fun Float.round(decimals: Int): Float {
    var dotAt = 1
    repeat(decimals) { dotAt *= 10 }
    val roundedValue = (this * dotAt).roundToInt()
    return (roundedValue / dotAt) + (roundedValue % dotAt).toFloat() / dotAt
}

