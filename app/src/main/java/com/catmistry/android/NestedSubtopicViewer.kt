package com.catmistry.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_nested_subtopic_viewer.*
import kotlin.math.min

class NestedSubtopicViewer : AppCompatActivity() {
    private fun updatePhUI(ph: Float) {
        val roundedText = ph.round(2).toString().padEnd(4, '0')
        pHSliderDisc.text = getString(R.string.pH_currentDesc, roundedText.substring(0, min(4, roundedText.length)), "Not implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nested_subtopic_viewer)

        topAppBar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        // Update UI from intent data (to increase loading speed)
        appBarHolder.title = intent.extras?.getString("title")
        topImg.setImageResource(resources.getIdentifier(
            intent.extras?.getString("mainImg"),
            "drawable", packageName
        ))

        val nestedDataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nestedData = dataSnapshot.getValue<NestedSubTopicContent>()

                firstContent.text = nestedData?.firstContent ?: ""
                secondContent.text = nestedData?.secondContent ?: ""
                thirdContent.text = nestedData?.thirdContent ?: ""
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