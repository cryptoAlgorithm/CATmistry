package com.catmistry.android

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_ph_game.*
import kotlinx.android.synthetic.main.activity_ptable_game.*
import kotlinx.android.synthetic.main.activity_ptable_game.endGame
import kotlinx.android.synthetic.main.activity_ptable_game.progressBar

class PTableGameActivity : AppCompatActivity() {
    val qnsArray: ArrayList<PTableGameArray?> = ArrayList()
    private var ansCorrect = 0

    // Timer vars
    private var continueTimer = false
    private var timeLeft: Double? = null
    private var currentTimerThread: Thread? = null

    // Timer functions
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
                        checkAns(100)
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

    private fun checkButtonState() {
        if (grpToggles.checkedButtonIds.size != 0) {
            subTypeMetal.isEnabled = true
            subTypeNMetal.isEnabled = true
            subTypeTMetal.isEnabled = true
        }
        else {
            subTypeMetal.isEnabled = false
            subTypeNMetal.isEnabled = false
            subTypeTMetal.isEnabled = false
        }
    }

    private fun disableUI() {
        // Reset groups toggle
        grpToggles.clearChecked()

        subGrpOne.isEnabled = false
        subGrpTwo.isEnabled = false
        subGrpThree.isEnabled = false
        subGrpFour.isEnabled = false
        checkButtonState()
    }

    private fun updateQn() {
        subDesc.text = qnsArray[0]?.desc

        // Enable all buttons
        subGrpOne.isEnabled = true
        subGrpTwo.isEnabled = true
        subGrpThree.isEnabled = true
        subGrpFour.isEnabled = true
        checkButtonState()

        // Start timer
        continueTimer = true
        startTimer((5.0 - intent.extras?.getDouble("difficulty")!!) * 5000)
    }

    private fun checkAns(type: Int) {
        // Pause timer
        continueTimer = false
        currentTimerThread?.interrupt()
        timeLeft = null

        val qn = qnsArray[0]

        val fQnsRemaining = if (qnsArray.size == 1) getString(R.string.quiz_remaining_singular)
        else getString(R.string.quiz_remaining_plural, qnsArray.size.toString())


        val selectedGrp = when (grpToggles.checkedButtonId) {
            R.id.subGrpOne -> 0
            R.id.subGrpTwo -> 1
            R.id.subGrpThree -> 2
            R.id.subGrpFour -> 3
            else -> 4
        }
        // Toast.makeText(this, selectedGrp.toString() + qn?.grp.toString() + qn?.type.toString() + type.toString(), Toast.LENGTH_SHORT).show()

        disableUI()

        if (qn?.grp == selectedGrp && qn.type == type) {
            ansCorrect++
            Snackbar.make(grpToggles, getString(R.string.game_correct, fQnsRemaining), Snackbar.LENGTH_SHORT).show()
        }
        else Snackbar.make(grpToggles, getString(R.string.game_wrong, fQnsRemaining), Snackbar.LENGTH_SHORT).show()

        qnsArray.removeFirstOrNull() // Remove current question from questions array
        if (qnsArray.size == 0) {
            startActivity(
                Intent(this, GameEndActivity::class.java)
                    .putExtra("won", ansCorrect >= 3))
            return // Don't call updateQn
        }

        updateQn() // Show next question
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ptable_game)

        endGame.setOnClickListener {
            onBackPressed()
        }

        // Get questions
        val gameTopicListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0
                dataSnapshot.children.forEach {
                    val listData = it.getValue<PTableGameArray>()
                    when {
                        qnsArray.isEmpty() -> {
                            qnsArray.add(listData)
                        }
                        i < qnsArray.size -> {
                            qnsArray[i] = listData
                        }
                        else -> {
                            qnsArray.add(listData)
                        }
                    }

                    i++
                }

                // Initial UI update
                updateQn()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        Firebase.database.reference.child("pTableGameArray").child((intent.extras?.getDouble("difficulty")?.toInt()
            ?.minus(1)).toString())
            .addValueEventListener(gameTopicListener)

        // Button onclick listeners
        subTypeMetal.setOnClickListener {
            checkAns(0)
        }
        subTypeTMetal.setOnClickListener {
            checkAns(1)
        }
        subTypeNMetal.setOnClickListener {
            checkAns(2)
        }

        // Toggle group change listener
        grpToggles.addOnButtonCheckedListener { _, _, _ ->
            checkButtonState()
        }
    }
}