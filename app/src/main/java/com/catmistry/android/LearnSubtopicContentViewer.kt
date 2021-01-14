package com.catmistry.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_learn_subtopic_content_viewer.*

class LearnSubtopicContentViewer : AppCompatActivity(), RecyclerViewClickListener {

    val nestedTopics: ArrayList<LearnTopics?> = ArrayList()

    private fun updatePHImg(ph: Float) {
        bottomImageView.setImageResource(resources.getIdentifier(
                "ph_${ph.toInt()}",
                "drawable", packageName
        ))

        pHSliderDisc.text = getString(R.string.pH_currentDesc, ph.toInt().toString(), "Not implemented yet")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_subtopic_content_viewer)

        val database = Firebase.database.reference

        topAppBar.setNavigationOnClickListener {
            onBackPressed() // Instead of using android's default navigation which restarts the previous activity
            finish()
        }

        // Update nested subtopics
        val nestedSubtopicListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0
                dataSnapshot.children.forEach {
                    val listData = it.getValue<LearnTopics>()
                    when {
                        nestedTopics.isEmpty() -> {
                            nestedTopics.add(listData)
                        }
                        i < nestedTopics.size -> {
                            nestedTopics[i] = listData
                        }
                        else -> {
                            nestedTopics.add(listData)
                        }
                    }

                    i++
                }

                // Update app bar header and content
                val learnTopicListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val learnData = dataSnapshot.getValue<SubTopicContent>()

                        // Set header title
                        appBarHolder.title = learnData?.appBarTitle
                        subTopicContent.text = learnData?.content

                        // Check if nestedTopics is empty
                        if (nestedTopics.isEmpty()) {
                            val resourceId = resources.getIdentifier(
                                    learnData?.bottomImage,
                                    "drawable", packageName
                            )

                            try {
                                bottomImageView.setImageResource(resourceId) // Might throw error if resID is a number
                                bottomImageView.visibility = View.VISIBLE
                            } catch (e: Exception) {}
                        }
                        else bottomImageView.visibility = View.GONE

                        if (learnData?.showPHSlider == true) {
                            pHSliderHolder.visibility = View.VISIBLE
                            updatePHImg(1f) // 1.0
                        }
                        else pHSliderHolder.visibility = View.GONE
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LearnSubtopicContentViewer,
                                "Failed to get subtopic content from database. Please try again later.",
                                Toast.LENGTH_LONG).show()
                    }
                }
                database.child("subTopicsContent").child(intent.extras?.getString("learnTopic").toString())
                        .child(intent.extras?.getString("learnItem").toString())
                        .addValueEventListener(learnTopicListener)

                runOnUiThread {
                    recyclerView.adapter?.notifyDataSetChanged()
                    recyclerView.scheduleLayoutAnimation()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LearnSubtopicContentViewer,
                        "Failed to get nested subtopics from database. Please try again later.",
                        Toast.LENGTH_LONG).show()
            }
        }
        database.child("nestedSubTopics")
                .child(intent.extras?.getString("learnTopic").toString())
                .child(intent.extras?.getString("learnItem").toString())
                .addValueEventListener(nestedSubtopicListener)

        recyclerView.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = HomescreenTopicsAdapter(nestedTopics, this@LearnSubtopicContentViewer, context)
            this.addItemDecoration(
                    DividerItemDecoration(
                            context,
                            LinearLayoutManager.VERTICAL
                    )
            )
        }

        // Handle pH slider
        phSlider.addOnChangeListener { _, value, fromUser ->
            // Responds to when slider's value is changed
            if (fromUser) updatePHImg(value)
        }
    }

    override fun itemClicked(itemID: Int) {
        // Not implemented yet, adding a TO-DO causes crash
    }
}