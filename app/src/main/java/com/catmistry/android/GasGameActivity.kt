package com.catmistry.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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

    private fun checkAns() {
        val question = gameData[questionsSeq[0]]
        if (gasOne.isChecked && question?.correctGasTank.equals(question?.firstGasTank)) Snackbar.make(submitGasAns, "Korrect", Snackbar.LENGTH_SHORT).show()
        else if (gasTwo.isChecked && question?.correctGasTank.equals(question?.secondGasTank)) Snackbar.make(submitGasAns, "Korrect", Snackbar.LENGTH_SHORT).show()
        else if (gasThree.isChecked && question?.correctGasTank.equals(question?.thirdGasTank)) Snackbar.make(submitGasAns, "Korrect", Snackbar.LENGTH_SHORT).show()
        else if (gasFour.isChecked && question?.correctGasTank.equals(question?.fourthGasTank)) Snackbar.make(submitGasAns, "Korrect", Snackbar.LENGTH_SHORT).show()
        else Snackbar.make(submitGasAns, "Wang", Snackbar.LENGTH_SHORT).show()

        questionsSeq.removeFirstOrNull()

        if (questionsSeq.isEmpty()) {
            Snackbar.make(submitGasAns, "Game end", Snackbar.LENGTH_SHORT).show()
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gas_game)

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