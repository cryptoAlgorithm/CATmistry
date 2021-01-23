package com.catmistry.android

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_gas_game.*

class GasGameActivity : AppCompatActivity() {
    var gameData: ArrayList<GasGameArray?> = ArrayList()
    var questionsSeq: ArrayList<Int> = ArrayList()
    private var continueTimer = false
    private var timeLeft: Double? = null
    private var currentTimerThread: Thread? = null
    private var reallyExit: Boolean = false
    private var totalTime: Double = 0.0

    private fun resumeTimer() {
        continueTimer = true // Resume timer
        startTimer(totalTime)
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
                .setTitle(getString(R.string.end_game_header))
                .setMessage(getString(R.string.end_game_content))
                .setNegativeButton(getString(R.string.end_game)) { _, _ ->
                    // Respond to negative button press
                    super.onBackPressed()
                    finish()
                }
                .setPositiveButton(getString(R.string.cont_game)) { _, _ ->
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

    private fun startTimer(totalTime: Double) {
        if (timeLeft == null) timeLeft = totalTime

        val runnable = Runnable {
            while (continueTimer && !Thread.interrupted()) {
                val newProg = ((timeLeft!! / totalTime) * 100.0).toInt()

                runOnUiThread {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.setProgress(newProg, true)
                    }
                    else progressBar.progress = newProg
                }

                if (timeLeft!! <= 0.0) {
                    runOnUiThread {
                        // Stuff that updates the UI
                        // Android how I hate you
                        checkAns()
                    }

                    timeLeft = null
                    break
                }

                timeLeft = timeLeft!! - 50

                try {
                    Thread.sleep(50)
                }
                catch(e: Exception) {}
            }

            continueTimer = false
        }
        currentTimerThread = Thread(runnable)
        currentTimerThread?.start()
    }

    private fun clearGasTestChecks() {
        gasTestOne.isChecked = false
        gasTestTwo.isChecked = false
        gasTestThree.isChecked = false
        gasTestFour.isChecked = false
    }

    private fun clearGasTankChecks() {
        gasOne.isChecked = false
        gasTwo.isChecked = false
        gasThree.isChecked = false
        gasFour.isChecked = false
    }

    private fun checkSubmitEnabled() {
        submitGasAns.isEnabled = (gasOne.isChecked || gasTwo.isChecked || gasThree.isChecked || gasFour.isChecked)
    }

    private fun refreshUI(gasTest: Int) {
        val qnData = gameData[questionsSeq[0]]

        gasOneOverlay.visibility = View.VISIBLE
        gasTwoOverlay.visibility = View.VISIBLE
        gasThreeOverlay.visibility = View.VISIBLE
        gasFourOverlay.visibility = View.VISIBLE

        if (qnData?.firstGasIsPositive?.get(gasTest) == true) gasOneOverlay.setImageResource(R.drawable.check)
        else gasOneOverlay.setImageResource(R.drawable.close)
        if (qnData?.secondGasIsPositive?.get(gasTest) == true) gasTwoOverlay.setImageResource(R.drawable.check)
        else gasTwoOverlay.setImageResource(R.drawable.close)
        if (qnData?.thirdGasIsPositive?.get(gasTest) == true) gasThreeOverlay.setImageResource(R.drawable.check)
        else gasThreeOverlay.setImageResource(R.drawable.close)
        if (qnData?.forthGasIsPositive?.get(gasTest) == true) gasFourOverlay.setImageResource(R.drawable.check)
        else gasFourOverlay.setImageResource(R.drawable.close)
    }

    private fun ansCorrect() {
        val leftQns = if (questionsSeq.size - 1 == 1) getString(R.string.quiz_remaining_singular)
        else getString(R.string.quiz_remaining_plural, (questionsSeq.size-1).toString())
        Snackbar.make(submitGasAns, getString(R.string.gas_game_correct, leftQns), Snackbar.LENGTH_LONG).show()
    }

    private fun checkAns() {
        val question = gameData[questionsSeq[0]]
        val leftQns = if (questionsSeq.size - 1 == 1) getString(R.string.quiz_remaining_singular)
        else getString(R.string.quiz_remaining_plural, (questionsSeq.size-1).toString())
        if (gasOne.isChecked && question?.correctGasTank.equals(question?.firstGasTank)) ansCorrect()
        else if (gasTwo.isChecked && question?.correctGasTank.equals(question?.secondGasTank)) ansCorrect()
        else if (gasThree.isChecked && question?.correctGasTank.equals(question?.thirdGasTank)) ansCorrect()
        else if (gasFour.isChecked && question?.correctGasTank.equals(question?.fourthGasTank)) ansCorrect()
        else Snackbar.make(submitGasAns, getString(R.string.gas_game_wrong, leftQns), Snackbar.LENGTH_LONG).show()

        questionsSeq.removeFirstOrNull()

        continueTimer = false
        currentTimerThread?.interrupt()
        timeLeft = null

        if (questionsSeq.isEmpty()) {
            reallyExit = true
            onBackPressed()
        }
        else initUiElem()
    }

    private fun initUiElem() {
        targetGas.text = gameData[questionsSeq[0]]?.correctGasTank
        clearGasTestChecks()
        clearGasTankChecks()
        checkSubmitEnabled()

        gasOneOverlay.visibility = View.INVISIBLE
        gasTwoOverlay.visibility = View.INVISIBLE
        gasThreeOverlay.visibility = View.INVISIBLE
        gasFourOverlay.visibility = View.INVISIBLE

        continueTimer = true
        startTimer(totalTime)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gas_game)

        // Set difficulty globally
        totalTime = (5.0 - intent.extras?.getDouble("difficulty")!!) * 5000.0
        // Toast.makeText(this, intent.extras!!.getDouble("difficulty").toString(), Toast.LENGTH_LONG).show()

        // Get data from database
        val gameTopicListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0
                dataSnapshot.children.forEach {
                    val listData = it.getValue<GasGameArray>()
                    when {
                        gameData.isEmpty() -> {
                            gameData.add(listData)
                        }
                        i < gameData.size -> {
                            gameData[i] = listData
                        }
                        else -> {
                            gameData.add(listData)
                        }
                    }

                    i++
                }

                repeat(10) { // Generate random sequence of questions
                    var selectedIndex: Int
                    do {
                        selectedIndex = (0 until gameData.size).random()
                    } while (questionsSeq.contains(selectedIndex))

                    questionsSeq.add(selectedIndex)
                }
                initUiElem()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        Firebase.database.reference.child("gasGameArray").addValueEventListener(gameTopicListener)

        gameBottomImg.setImageResource(resources.getIdentifier(
            intent.extras?.getString("bottomImg"),
            "drawable", packageName
        ))

        // End game button
        endGame.setOnClickListener {
            onBackPressed()
        }

        // Gas tests onclick listeners
        gasTestOne.setOnClickListener {
            clearGasTestChecks()
            gasTestOne.isChecked = true
            refreshUI(0)
        }
        gasTestTwo.setOnClickListener {
            clearGasTestChecks()
            gasTestTwo.isChecked = true
            refreshUI(1)
        }
        gasTestThree.setOnClickListener {
            clearGasTestChecks()
            gasTestThree.isChecked = true
            refreshUI(2)
        }
        gasTestFour.setOnClickListener {
            clearGasTestChecks()
            gasTestFour.isChecked = true
            refreshUI(3)
        }

        // Gas tanks onclick listeners
        gasOne.setOnClickListener {
            val prevState = gasOne.isChecked // Save prev state
            clearGasTankChecks()
            gasOne.isChecked = !prevState
            checkSubmitEnabled()
        }
        gasTwo.setOnClickListener {
            val prevState = gasTwo.isChecked
            clearGasTankChecks()
            gasTwo.isChecked = !prevState
            checkSubmitEnabled()
        }
        gasThree.setOnClickListener {
            val prevState = gasThree.isChecked
            clearGasTankChecks()
            gasThree.isChecked = !prevState
            checkSubmitEnabled()
        }
        gasFour.setOnClickListener {
            val prevState = gasFour.isChecked
            clearGasTankChecks()
            gasFour.isChecked = !prevState
            checkSubmitEnabled()
        }

        // Submit button
        submitGasAns.setOnClickListener {
            checkAns()
        }
    }
}