package com.catmistry.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_start_game.*

class StartGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_game)

        gameIntro.text = intent.extras?.getString("gameDesc")
        gameBottomImg.setImageResource(resources.getIdentifier(
            intent.extras?.getString("gameIcon"),
            "drawable", packageName
        ))

        difficultySlider.setLabelFormatter { value ->
            when (value.toInt()) {
                1 -> "Easy"
                2 -> "Normal"
                3 -> "Hard"
                4 -> "Extreme"
                else -> value.toString()
            }
        }

        startGame.setOnClickListener {
            when(intent.extras?.getInt("gameIndex")) {
                0 -> startActivity(Intent(this, GasGameActivity::class.java)
                    .putExtra("bottomImg", intent.extras?.getString("gameIcon"))
                    .putExtra("difficulty", difficultySlider.value.toDouble()))
                1 -> startActivity(Intent(this, RetrieveGameActivity::class.java)
                    .putExtra("bottomImg", intent.extras?.getString("gameIcon"))
                    .putExtra("difficulty", difficultySlider.value.toDouble()))
                else -> Snackbar.make(it, "No game activity for this item", Snackbar.LENGTH_SHORT).show()
            }
        }

        goBack.setOnClickListener {
            onBackPressed()
        }
    }
}