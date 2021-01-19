package com.catmistry.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_gas_game.*

class GasGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gas_game)

        gameBottomImg.setImageResource(resources.getIdentifier(
            intent.extras?.getString("bottomImg"),
            "drawable", packageName
        ))

        goBack.setOnClickListener {
            onBackPressed()
        }

        // Gas tests onclick listeners
        gasTestOne.setOnClickListener {

        }
        gasTestTwo.setOnClickListener {

        }
        gasTestThree.setOnClickListener {

        }
        gasTestFour.setOnClickListener {

        }
    }
}