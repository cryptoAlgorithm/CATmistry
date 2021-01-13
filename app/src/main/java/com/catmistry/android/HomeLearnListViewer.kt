package com.catmistry.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_learn_list_viewer.*
import kotlinx.android.synthetic.main.activity_home_learn_list_viewer.recyclerView
import kotlinx.android.synthetic.main.activity_home_learn_list_viewer.topAppBar
import kotlinx.android.synthetic.main.fragment_learn_list.*

class HomeLearnListViewer : AppCompatActivity(), RecyclerViewClickListener {

    private val listTopics: ArrayList<LearnTopics?> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_learn_list_viewer)

        appBarHolder.title = intent.extras?.getString("quizHeader")

        topAppBar.setNavigationOnClickListener {
            onBackPressed() // Instead of using android's default navigation which restarts the activity
            finish()
        }

        // Update list topics with data from database
        val database = Firebase.database.reference

        val learnTopicListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0
                dataSnapshot.children.forEach {
                    val listData = it.getValue<LearnTopics>()
                    when {
                        listTopics.isEmpty() -> {
                            listTopics.add(listData)
                        }
                        i < listTopics.size -> {
                            listTopics[i] = listData
                        }
                        else -> {
                            listTopics.add(listData)
                        }
                    }

                    i++
                }

                runOnUiThread {
                    recyclerView.adapter?.notifyDataSetChanged()
                    recyclerView.scheduleLayoutAnimation()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        database.child("learnSubTopics").child(intent.extras?.getString("quizIndex").toString())
                .addValueEventListener(learnTopicListener)

        recyclerView.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = HomescreenTopicsAdapter(listTopics, this@HomeLearnListViewer, context)
            this.addItemDecoration(
                    DividerItemDecoration(
                            context,
                            LinearLayoutManager.VERTICAL
                    )
            )
        }
    }

    override fun itemClicked(itemID: Int) {
        if (itemID == listTopics.size - 1) {
            val quizIntent = Intent(this, LearnQuizActivity::class.java)
            quizIntent.putExtra("quizTopic", intent.extras?.getString("quizIndex"))
            startActivity(quizIntent)
        }
    }
}