package com.catmistry.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_ph_game.*

class PhGameActivity : AppCompatActivity() {

    private fun clrSubChecks() {
        subOne.isChecked = false
        subTwo.isChecked = false
        subThree.isChecked = false
        subFour.isChecked = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var beakerPH = 1
        val subPhArr: ArrayList<Int> = ArrayList()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ph_game)

        // Generate a random beaker pH
        do { beakerPH = (1 until 15).random() }
        while (beakerPH == 7) // Ensure beaker pH is never 7

        // Onclick listeners
        endGame.setOnClickListener {
            onBackPressed()
        }

        // Add/remove universal pH
        uniIndicatorSw.setOnCheckedChangeListener { _, isChecked ->
            val resourceId = resources.getIdentifier(
                if (isChecked) "monster_ph$beakerPH"
                else "monster_regular",
                "drawable", packageName
            )

            try {
                phBeaker.setImageResource(resourceId) // Might throw random errors
            } catch (e: Exception) {}
        }

        // Set substance images
        // First generate random pHs
        repeat(4) {
            var temp = 0
            do { temp = (1 until 15).random() }
            while (temp == 7 || subPhArr.contains(temp)) // Ensure substance pH is never 7
            subPhArr.add(temp)
        }

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

        // Substance check handlers
        subOne.setOnClickListener {
            clrSubChecks()
            Snackbar.make(it, subPhArr[0].toString(), Snackbar.LENGTH_LONG).show()
            subOne.isChecked = true
        }
        subTwo.setOnClickListener {
            clrSubChecks()
            Snackbar.make(it, subPhArr[1].toString(), Snackbar.LENGTH_LONG).show()
            subTwo.isChecked = true
        }
        subThree.setOnClickListener {
            clrSubChecks()
            Snackbar.make(it, subPhArr[2].toString(), Snackbar.LENGTH_LONG).show()
            subThree.isChecked = true
        }
        subFour.setOnClickListener {
            clrSubChecks()
            Snackbar.make(it, subPhArr[3].toString(), Snackbar.LENGTH_LONG).show()
            subFour.isChecked = true
        }
    }
}