package com.catmistry.android

import android.content.Intent
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
    val pHData: ArrayList<PhArray?> = ArrayList()

    private fun updatePHImg(ph: Float) {
        val pHIndex = ph.toInt()

        if (pHIndex > pHData.size) return // pH has more elements than the array (causes crashes)

        // Update image
        bottomImageView.setImageResource(resources.getIdentifier(
                pHData[pHIndex-1]?.pHImg,
                "drawable", packageName
        ))

        // Update text
        pHSliderDisc.text = getString(R.string.pH_currentDesc, pHIndex.toString(), pHData[pHIndex-1]?.pHDesc)
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

                        // Check if pH slider should be enabled
                        if (learnData?.showPHSlider == true) {
                            // Then get pH data
                            val pHArrayListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    // Add data to array
                                    var n = 0
                                    dataSnapshot.children.forEach {
                                        val singlePHData = it.getValue<PhArray>()
                                        when {
                                            pHData.isEmpty() -> {
                                                pHData.add(singlePHData)
                                            }
                                            n < pHData.size -> {
                                                pHData[n] = singlePHData
                                            }
                                            else -> {
                                                pHData.add(singlePHData)
                                            }
                                        }

                                        n++
                                    }

                                    // Finally update UI
                                    pHSliderHolder.visibility = View.VISIBLE  // Make slider linearlayout visible
                                    bottomImageView.visibility = View.VISIBLE // Make image visible
                                    updatePHImg(7f) // 1.0

                                    // Handle pH slider
                                    phSlider.addOnChangeListener { _, value, fromUser ->
                                        // Responds to when slider's value is changed
                                        if (fromUser) updatePHImg(value)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@LearnSubtopicContentViewer,
                                        "Could not get pH data array from database.",
                                        Toast.LENGTH_LONG).show()
                                }
                            }

                            database.child("pHArray").addValueEventListener(pHArrayListener)
                        }
                        // Check if nestedTopics is empty
                        else if (nestedTopics.isEmpty()) {
                            val resourceId = resources.getIdentifier(
                                learnData?.bottomImage,
                                "drawable", packageName
                            )

                            try {
                                bottomImageView.setImageResource(resourceId) // Might throw error if resID is a number
                                bottomImageView.visibility = View.VISIBLE
                            } catch (e: Exception) {}

                            pHSliderHolder.visibility = View.GONE
                        }
                        else bottomImageView.visibility = View.GONE
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
    }

    override fun itemClicked(itemID: Int) {
        // Start nested item viewer
        val nestedViewer = Intent(this, NestedSubtopicViewer::class.java)
        nestedViewer.putExtra("rootTopic", intent.extras?.getString("learnTopic").toString())
        nestedViewer.putExtra("subTopic", intent.extras?.getString("learnItem").toString())
        nestedViewer.putExtra("nestedTopic", itemID.toString())
        nestedViewer.putExtra("title", nestedTopics[itemID]?.title)
        nestedViewer.putExtra("mainImg", nestedTopics[itemID]?.icon)
        startActivity(nestedViewer)
    }
}