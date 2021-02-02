package com.catmistry.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_ph_game.*

class PhGameActivity : AppCompatActivity() {
    private var difficulty: Double? = null
    private var beakerPH: Int = 0
    private val subPhArr: ArrayList<Int> = ArrayList()

    private fun clrSubChecks() {
        subOne.isChecked = false
        subTwo.isChecked = false
        subThree.isChecked = false
        subFour.isChecked = false
    }

    private fun refreshBeaker() {
        if (difficulty == 1.0) {
            beakerPhHint.text = getString(R.string.beaker_ph, beakerPH.toString())
            beakerPhHint.visibility = View.VISIBLE
        }
        else beakerPhHint.visibility = View.GONE
        if (uniIndicatorSw.isChecked) {
            when {
                subOne.isChecked -> phBeaker.setImageResource(resources.getIdentifier(
                        "monster_ph${subPhArr[0]}",
                        "drawable", packageName
                ))
                subTwo.isChecked -> phBeaker.setImageResource(resources.getIdentifier(
                        "monster_ph${subPhArr[1]}",
                        "drawable", packageName
                ))
                subThree.isChecked -> phBeaker.setImageResource(resources.getIdentifier(
                        "monster_ph${subPhArr[2]}",
                        "drawable", packageName
                ))
                subFour.isChecked -> phBeaker.setImageResource(resources.getIdentifier(
                        "monster_ph${subPhArr[3]}",
                        "drawable", packageName
                ))
                else -> phBeaker.setImageResource(resources.getIdentifier(
                        "monster_ph$beakerPH",
                        "drawable", packageName
                ))
            }
        }
        else phBeaker.setImageResource(R.drawable.monster_regular)

        // Enable/disable submit button
        // If substance is selected answer can be submitted
        submitPhAns.isEnabled = (subOne.isChecked || subTwo.isChecked || subThree.isChecked || subFour.isChecked)
    }

    private fun nextQn() {
        // Reset array
        subPhArr.clear() // Delete all elements

        // Generate a random beaker pH
        do { beakerPH = (1 until 15).random() }
        while (beakerPH == 7) // Ensure beaker pH is never 7

        // Set substance images
        // First generate random pHs
        repeat(4) {
            var temp: Int
            do { temp = (1 until 15).random() }
            while (subPhArr.contains(temp))
            subPhArr.add(temp)
        }

        refreshBeaker() // Update beaker image

        // Then set the respective substance images
        subOneImg.setImageResource(resources.getIdentifier(
                "ph_${subPhArr[0]}",
                "drawable", packageName
        ))
        subTwoImg.setImageResource(resources.getIdentifier(
                "ph_${subPhArr[1]}",
                "drawable", packageName
        ))
        subThreeImg.setImageResource(resources.getIdentifier(
                "ph_${subPhArr[2]}",
                "drawable", packageName
        ))
        subFourImg.setImageResource(resources.getIdentifier(
                "ph_${subPhArr[3]}",
                "drawable", packageName
        ))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        difficulty = intent.extras?.getDouble("difficulty")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ph_game)

        // Init for the first time
        nextQn()

        // Onclick listeners
        endGame.setOnClickListener {
            onBackPressed()
        }

        // Add/remove universal pH
        uniIndicatorSw.setOnCheckedChangeListener { _, _ ->
            refreshBeaker()
        }

        // Submit button onclick listener
        submitPhAns.setOnClickListener {
            // Check if ans is correct
            val selectedPh = when {
                subOne.isChecked -> subPhArr[0]
                subTwo.isChecked -> subPhArr[1]
                subThree.isChecked -> subPhArr[2]
                subFour.isChecked -> subPhArr[3]
                else -> 7 // Always wrong
            }
            if ((selectedPh < 7 && beakerPH > 7) || (selectedPh > 7 && beakerPH < 7))
                Snackbar.make(it, R.string.ph_correct, Snackbar.LENGTH_SHORT).show() // Correct
            else Snackbar.make(it, R.string.ph_wrong, Snackbar.LENGTH_SHORT).show()  // Wrong
            nextQn()
        }

        // Substance check handlers
        subOne.setOnClickListener {
            val prevState = subOne.isChecked
            clrSubChecks()
            if (difficulty == 1.0 && !prevState) Snackbar.make(it, getString(R.string.sub_ph_hint, subPhArr[0].toString()), Snackbar.LENGTH_SHORT).show()
            subOne.isChecked = !prevState
            refreshBeaker()
        }
        subTwo.setOnClickListener {
            val prevState = subTwo.isChecked
            clrSubChecks()
            if (difficulty == 1.0 && !prevState) Snackbar.make(it, getString(R.string.sub_ph_hint, subPhArr[1].toString()), Snackbar.LENGTH_SHORT).show()
            subTwo.isChecked = true
            subTwo.isChecked = !prevState
            refreshBeaker()
        }
        subThree.setOnClickListener {
            val prevState = subThree.isChecked
            clrSubChecks()
            if (difficulty == 1.0 && !prevState) Snackbar.make(it, getString(R.string.sub_ph_hint, subPhArr[2].toString()), Snackbar.LENGTH_SHORT).show()
            subThree.isChecked = true
            subThree.isChecked = !prevState
            refreshBeaker()
        }
        subFour.setOnClickListener {
            val prevState = subFour.isChecked
            clrSubChecks()
            if (difficulty == 1.0 && !prevState) Snackbar.make(it, getString(R.string.sub_ph_hint, subPhArr[3].toString()), Snackbar.LENGTH_SHORT).show()
            subFour.isChecked = true
            subFour.isChecked = !prevState
            refreshBeaker()
        }
    }
}