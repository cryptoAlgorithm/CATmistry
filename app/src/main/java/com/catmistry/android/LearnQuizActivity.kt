package com.catmistry.android

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_learn_quiz.*
import nl.dionsegijn.konfetti.emitters.StreamEmitter
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class LearnQuizActivity : AppCompatActivity() {
    var continueTimer = false
    private fun startTimer(totalTime: Double = 20000.0) {
        var timeRemaining = totalTime // Time in ms left for question

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                if (timeRemaining >= 0 && continueTimer) mainHandler.postDelayed(this, 50)
                else Toast.makeText(this@LearnQuizActivity, "ayo", Toast.LENGTH_SHORT).show()
                val newProg = ((timeRemaining / totalTime) * 100.0).toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(newProg, true)
                }
                else progressBar.progress = newProg
                timeRemaining -= 50
            }
        })
    }

    private fun checkAns(enteredAns: Int, correctAns: Int) {
        if (enteredAns == correctAns) Toast.makeText(this@LearnQuizActivity, "Korrect", Toast.LENGTH_SHORT).show()
        else Toast.makeText(this@LearnQuizActivity, "Wrong", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI(qnData: LearnQns?) {
        // Set title text
        questionText.text = qnData?.question
        // Set options button text
        optOne.text = qnData?.options?.get(0) ?: getString(R.string.opt_err)
        optTwo.text = qnData?.options?.get(1) ?: getString(R.string.opt_err)
        optThree.text = qnData?.options?.get(2) ?: getString(R.string.opt_err)
        optFour.text = qnData?.options?.get(3) ?: getString(R.string.opt_err)
        // Set onclick listeners
        optOne.setOnClickListener {
            checkAns(1, qnData?.correctAnswer!!)
        }
        optTwo.setOnClickListener {
            checkAns(2, qnData?.correctAnswer!!)
        }
        optThree.setOnClickListener {
            checkAns(3, qnData?.correctAnswer!!)
        }
        optFour.setOnClickListener {
            checkAns(4, qnData?.correctAnswer!!)
        }
    }

    override fun onBackPressed() {
        MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.end_quiz_title))
                .setMessage(resources.getString(R.string.end_quiz_body))
                .setNegativeButton(resources.getString(R.string.end_quiz_ok)) { _, _ ->
                    // Respond to negative button press
                    super.onBackPressed()
                }
                .setPositiveButton(resources.getString(R.string.end_quiz_no)) { _, _ ->
                    // Respond to positive button press
                    // Do nothing
                }
                .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_quiz)

        // Start konfetti
        /* konfettiView.build()
            .addColors(Color.rgb(237, 103, 83), Color.rgb(97, 198, 164),
                Color.rgb(245, 205, 165), Color.rgb(86, 192, 213),
                Color.rgb(150, 100, 132))
            .setDirection(0.0, 180.0)
            .setSpeed(0.8f, 1.2f)
            .setFadeOutEnabled(true)
            .setTimeToLive(100000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(10))
            .setPosition(-50f, konfettiView.width + 10000f, -50f, 0f)
            .streamFor(200, StreamEmitter.INDEFINITE) */

        Toast.makeText(this, intent.extras?.getString("quizTopic"), Toast.LENGTH_SHORT).show()

        val questionsArray: ArrayList<LearnQns?> = ArrayList()

        val qnDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val listData = it.getValue<LearnQns>()
                    questionsArray.add(listData)
                    Log.e("List data", listData.toString())
                }

                updateUI(questionsArray[0])
                continueTimer = true
                startTimer(10000.0)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LearnQuizActivity, "Error fetching question",
                        Toast.LENGTH_SHORT).show()
                onBackPressed()
                finish()
            }
        }

        Firebase.database.reference.child("learnQns")
                .child(intent.extras?.getString("quizTopic").toString())
                .addListenerForSingleValueEvent(qnDataListener)
    }
}