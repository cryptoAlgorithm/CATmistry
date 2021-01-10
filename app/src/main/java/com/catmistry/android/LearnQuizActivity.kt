package com.catmistry.android

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
    var timeLeft: Double? = null
    var currentTimerThread: Thread? = null

    val questionsArray: ArrayList<LearnQns?> = ArrayList() // Array to store all questions data


    private fun startTimer(totalTime: Double) {
        if (timeLeft == null) timeLeft = totalTime

        val runnable = Runnable {
            while (continueTimer && !Thread.interrupted()) {
                val newProg = ((timeLeft!! / totalTime) * 100.0).toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(newProg, true)
                }
                else progressBar.progress = newProg

                if (timeLeft!! <= 0.0) {
                    runOnUiThread {
                        // Stuff that updates the UI
                        // Android how I hate you
                        checkAns(5, questionsArray[0]?.correctAnswer!!) // This will never be right
                    }
                    break
                }

                timeLeft = timeLeft!! - 50

                Thread.sleep(50)
            }

            timeLeft = null
            continueTimer = false
        }
        currentTimerThread = Thread(runnable)
        currentTimerThread?.start()
    }

    private fun showNextQn() {
        // Gracefully stop konfetti
        konfettiView.stopGracefully()

        // Interrupt timer
        currentTimerThread?.interrupt()
        timeLeft = null

        // Delete shown question (or fail silently)
        questionsArray.removeFirstOrNull()

        if (questionsArray.isEmpty()) {
            Toast.makeText(this@LearnQuizActivity, "Quiz complete", Toast.LENGTH_SHORT).show()
            return
        }
        updateUI(questionsArray[0])

        // Enable buttons once again
        optOne.isEnabled   = true
        optTwo.isEnabled   = true
        optThree.isEnabled = true
        optFour.isEnabled  = true

        continueTimer = true
        startTimer(10000.0)
    }

    private fun checkAns(enteredAns: Int, correctAns: Int) {
        // Pause the timer
        continueTimer = false

        if (enteredAns == correctAns) {
            resultImg.setImageResource(R.drawable.check)
            resultHeader.text = getString(R.string.quiz_result_correct)
            resultSubtitle.text = getString(R.string.correct_ans_correct, (questionsArray.size - 1).toString())
            konfettiView.build()
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
                    .streamFor(200, StreamEmitter.INDEFINITE)
        }
        else {
            resultImg.setImageResource(R.drawable.close)
            resultHeader.text = getString(R.string.quiz_result_wrong, (questionsArray.size - 1).toString())
            resultSubtitle.text = getString(R.string.correct_ans_wrong, questionsArray[0]?.options?.get(correctAns - 1))
        }

        BottomSheetBehavior.from(resultsSheet).state = BottomSheetBehavior.STATE_EXPANDED

        // Disable the option buttons to prevent clicking again
        optOne.isEnabled   = false
        optTwo.isEnabled   = false
        optThree.isEnabled = false
        optFour.isEnabled  = false
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

    private fun resumeTimer() {
        continueTimer = true // Resume timer
        startTimer(10000.0)
    }

    override fun onBackPressed() {
        val prevState = continueTimer

        continueTimer = false // Pause timer if running

        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.end_quiz_title))
            .setMessage(resources.getString(R.string.end_quiz_body))
            .setNegativeButton(resources.getString(R.string.end_quiz_ok)) { _, _ ->
                // Respond to negative button press
                super.onBackPressed()
                finish()
            }
            .setPositiveButton(resources.getString(R.string.end_quiz_no)) { _, _ ->
                // Respond to positive button press
                if (prevState) {
                    resumeTimer()
                }
            }
            .setOnCancelListener {
                if (prevState) {
                    resumeTimer()
                }
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_quiz)

        val bottomSheet = BottomSheetBehavior.from(resultsSheet)

        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        // Start konfetti
        /*  */

        Toast.makeText(this, intent.extras?.getString("quizTopic"), Toast.LENGTH_SHORT).show()

        val qnDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val listData = it.getValue<LearnQns>()
                    questionsArray.add(listData)
                }
                showNextQn()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LearnQuizActivity, "Error fetching question",
                        Toast.LENGTH_SHORT).show()
                onBackPressed()
                finish()
            }
        }

        val qnRef = Firebase.database.reference.child("learnQns")
                .child(intent.extras?.getString("quizTopic").toString())
        qnRef.keepSynced(true)
        qnRef.addListenerForSingleValueEvent(qnDataListener)

        endQuizButton.setOnClickListener {
            onBackPressed()
        }

        sheetEndButton.setOnClickListener {
            bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            showNextQn()
        }

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    showNextQn()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do nothing
            }
        })
    }
}