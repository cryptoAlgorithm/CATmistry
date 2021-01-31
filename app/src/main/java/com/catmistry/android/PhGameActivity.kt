package com.catmistry.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_ph_game.*

class PhGameActivity : AppCompatActivity() {
    private fun clrSubChecks() {
        subOne.isChecked = false
        subTwo.isChecked = false
        subThree.isChecked = false
        subFour.isChecked = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ph_game)

        // Onclick listeners
        endGame.setOnClickListener {
            onBackPressed()
        }

        // Substance check handlers
        subOne.setOnClickListener {
            clrSubChecks()
            subOne.isChecked = true
        }
        subTwo.setOnClickListener {
            clrSubChecks()
            subTwo.isChecked = true
        }
        subThree.setOnClickListener {
            clrSubChecks()
            subThree.isChecked = true
        }
        subFour.setOnClickListener {
            clrSubChecks()
            subFour.isChecked = true
        }
    }
}