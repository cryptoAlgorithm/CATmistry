package com.catmistry.android

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_game_end.*
import nl.dionsegijn.konfetti.emitters.StreamEmitter
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import android.content.Intent




class GameEndActivity : AppCompatActivity() {
    override fun onBackPressed() { // Back also goes home
        super.onBackPressed()

        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_end)

        val won = intent.extras?.getBoolean("won")!!

        if (won) gameResult.text = getText(R.string.game_won)
        else {
            popConfetti.visibility = View.GONE
            gameResult.text = getString(R.string.game_lost)
        }

        popConfetti.setOnClickListener {
            konfettiView.build()
                .addColors(
                    Color.rgb(237, 103, 83), Color.rgb(97, 198, 164),
                    Color.rgb(245, 205, 165), Color.rgb(86, 192, 213),
                    Color.rgb(150, 100, 132)
                )
                .setDirection(0.0, 359.0)
                .setSpeed(2f, 10f)
                .setFadeOutEnabled(true)
                .setTimeToLive(8000L)
                .addShapes(Shape.Square, Shape.Circle)
                .addSizes(Size(12), Size(16, 6f))
                .setPosition(
                    konfettiView.x + konfettiView.width / 2,
                    konfettiView.y + konfettiView.height / 2
                )
                .burst(200)
        }

        endHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}