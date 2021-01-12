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
    private var continueTimer = false
    private var timeLeft: Double? = null
    private var currentTimerThread: Thread? = null
    private var numCorrectAns: Int = 0
    private var reallyExit = true

    val questionsArray: ArrayList<LearnQns?> = ArrayList() // Array to store all questions data
    val questionsSeq: ArrayList<Int> = ArrayList() // Sequence of questions

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
                        checkAns(5, questionsArray[questionsSeq[0]]?.correctAnswer!!) // This will never be right
                    }

                    timeLeft = null
                    break
                }

                timeLeft = timeLeft!! - 50

                Thread.sleep(50)
            }

            continueTimer = false
        }
        currentTimerThread = Thread(runnable)
        currentTimerThread?.start()
    }

    private fun showNextQn() {
        reallyExit = false

        // Gracefully stop konfetti
        konfettiView.stopGracefully()

        // Interrupt timer
        currentTimerThread?.interrupt()
        timeLeft = null

        if (questionsSeq.isEmpty()) {
            resultHeader.text = getString(R.string.endOfQuiz, numCorrectAns.toString())
            resultSubtitle.text = if (numCorrectAns >= 5) getString(R.string.quiz_pass)
            else getString(R.string.quiz_fail)
            if (numCorrectAns >= 5) resultImg.setImageResource(R.drawable.check)
            else resultImg.setImageResource(R.drawable.close)
            reallyExit = true

            BottomSheetBehavior.from(resultsSheet).state = BottomSheetBehavior.STATE_EXPANDED

            /*BottomSheetBehavior.from(resultsSheet).addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        onBackPressed()
                        Log.e("Exited from bottom shit", "Bottom sheet")
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // Do nothing
                }
            })*/

            sheetEndButton.text = getString(R.string.quiz_exit_btn)
            sheetEndButton.setOnClickListener {
                onBackPressed()
                Log.e("Exited from button", "Press")
            }

            return
        }

        updateUI(questionsArray[questionsSeq[0]])

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

        // Disable the option buttons to prevent clicking again
        optOne.isEnabled   = false
        optTwo.isEnabled   = false
        optThree.isEnabled = false
        optFour.isEnabled  = false

        val questionsRemaining = questionsSeq.size - 1
        val questionsLeftText = if (questionsRemaining == 1) getString(R.string.quiz_remaining_singular)
        else getString(R.string.quiz_remaining_plural, questionsRemaining.toString())

        if (enteredAns == correctAns) {
            resultImg.setImageResource(R.drawable.check)
            resultHeader.text = getString(R.string.quiz_result_correct)
            resultSubtitle.text = getString(R.string.correct_ans_correct, questionsLeftText)
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

            // Add a correct answer
            numCorrectAns++
        }
        else {
            resultImg.setImageResource(R.drawable.close)
            resultHeader.text = getString(R.string.quiz_result_wrong, questionsLeftText)
            resultSubtitle.text = getString(R.string.correct_ans_wrong, questionsArray[questionsSeq[0]]?.options?.get(correctAns - 1))
        }

        BottomSheetBehavior.from(resultsSheet).state = BottomSheetBehavior.STATE_EXPANDED

        // Delete shown question (or fail silently)
        questionsSeq.removeFirstOrNull()
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

        if (reallyExit) {
            super.onBackPressed()
            finish()
            return
        }

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

        val qnDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val listData = it.getValue<LearnQns>()
                    Log.e("ListData", listData.toString())
                    questionsArray.add(listData)
                }
                repeat(10) {
                    var selectedIndex: Int
                    do {
                        selectedIndex = (0 until questionsArray.size).random()
                    } while (questionsSeq.contains(selectedIndex))

                    questionsSeq.add(selectedIndex)
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
        }

        bottomSheet.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (!reallyExit) showNextQn()
                    else onBackPressed()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do nothing
            }
        })
    }
}