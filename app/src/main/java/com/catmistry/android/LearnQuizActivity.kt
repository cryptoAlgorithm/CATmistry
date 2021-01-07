package com.catmistry.android

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_learn_quiz.*
import nl.dionsegijn.konfetti.emitters.StreamEmitter
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class LearnQuizActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_quiz)

        // Start konfetti
        konfettiView.build()
            .addColors(Color.rgb(237, 103, 83), Color.rgb(97, 198, 164),
                Color.rgb(245, 205, 165), Color.rgb(86, 192, 213), Color.rgb(150, 100, 132))
            .setDirection(180.0, 0.0)
            .setSpeed(0.5f, 2f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(12))
            .setPosition(-50f, konfettiView.width + 10000f, -50f, -50f)
            .streamFor(300, StreamEmitter.INDEFINITE)
    }
}