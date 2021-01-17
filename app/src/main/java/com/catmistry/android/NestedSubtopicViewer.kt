package com.catmistry.android

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_nested_subtopic_viewer.*
import kotlin.math.min
import kotlin.math.roundToInt


class NestedSubtopicViewer : AppCompatActivity() {
    var nestedData: NestedSubTopicContent? = null
    private var latestColor: Int? = null

    private fun pHTransitionColor(oldColor: Int, newColor: Int) {
        if (latestColor == newColor) return
        latestColor = newColor

        val anim = ValueAnimator.ofObject(ArgbEvaluator(), oldColor, newColor)
        anim.duration = 200 // Animation time in MS

        anim.addUpdateListener { animator -> pHIndicatorColor.setCardBackgroundColor(animator.animatedValue as Int) }
        anim.start()
    }

    private fun updatePhUI(ph: Float) {
        val roundedText = ph.round(2).toString().padEnd(4, '0')
        val oldColor = pHIndicatorColor.cardBackgroundColor.defaultColor

        if (intent.extras?.getString("title").equals("Universal Indicator")) { // Hardcode this
            val pHColors = listOf("#EF1D2C", "#F6643A", "#F98F30", "#FFC33D", "#FFF23A", "#83C34D", "#49B751", "#3B9C57", "#00B8B6", "#4390CA", "#3753A1", "#5A519F", "#6D227E", "#49176C")
            val pHColorsDesc = listOf("Red", "Dark Orange", "Orange", "Light Orange/Dark Yellow", "Yellow", "Light Green", "Green", "Dark Green", "Light Blue", "Blue", "Dark Blue", "Light Purple", "Purple", "Dark Purple")

            repeat(14) {
                if (ph >= it + 1 && ph < it + 2) {
                    pHTransitionColor(oldColor, Color.parseColor(pHColors[it]))
                    pHSliderDisc.text = getString(R.string.pH_currentDesc, roundedText.substring(0, min(4, roundedText.length)), pHColorsDesc[it])
                }
            }

            return
        }
        // Get color description
        val phColorDesc = when {
            ph >= nestedData?.highPH!! -> nestedData?.highPHDesc
            ph <= nestedData?.lowPH!!  -> nestedData?.lowPHDesc
            else -> nestedData?.midPHDesc
        }
        // Set color
        try { // Catch color parse errors
            when {
                ph >= nestedData?.highPH!! -> pHTransitionColor(oldColor, Color.parseColor(nestedData?.highPHColor))
                ph <= nestedData?.lowPH!!  -> pHTransitionColor(oldColor, Color.parseColor(nestedData?.lowPHColor))
                else -> pHTransitionColor(oldColor, Color.parseColor(nestedData?.midPHColor))
            }
        } catch (e: Exception) {}

        pHSliderDisc.text = getString(R.string.pH_currentDesc, roundedText.substring(0, min(4, roundedText.length)), phColorDesc)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nested_subtopic_viewer)

        topAppBar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        // Update UI from intent data (to increase initial loading speed)
        appBarHolder.title = intent.extras?.getString("title")

        val nestedDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                nestedData = dataSnapshot.getValue<NestedSubTopicContent>()

                firstContent.text = nestedData?.firstContent ?: ""
                secondContent.text = nestedData?.secondContent ?: ""
                thirdContent.text = nestedData?.thirdContent ?: ""

                topImg.setImageResource(resources.getIdentifier(
                        nestedData?.mainImg,
                        "drawable", packageName
                ))

                if (nestedData?.lowPH != null) {
                    phSection.visibility = View.VISIBLE
                    updatePhUI(7f)
                }
                else phSection.visibility = View.GONE

                if (nestedData?.emphasisText != null) {
                    warningCard.visibility = View.VISIBLE
                    warningText.text = nestedData?.emphasisText
                }
                else warningCard.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                firstContent.setText(R.string.error_load)
            }
        }

        // Get text data from database
        Firebase.database.reference.child("nestedSubTopicsContent")
            .child(intent.extras?.getString("rootTopic").toString())
            .child(intent.extras?.getString("subTopic").toString())
            .child(intent.extras?.getString("nestedTopic").toString())
            .addValueEventListener(nestedDataListener)

        // Handle pH slider
        phSlider.addOnChangeListener { _, value, fromUser ->
            // Responds to when slider's value is changed
            if (fromUser) updatePhUI(value)
        }
    }
}