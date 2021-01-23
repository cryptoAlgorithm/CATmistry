package com.catmistry.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_retrieve_game.*

class RetrieveGameActivity : AppCompatActivity(), RecyclerViewClickListener {
    private val separationMethods: ArrayList<HomeTopics?> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrieve_game)

        // Load list of separation methods
        val learnTopicListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0
                dataSnapshot.children.forEach {
                    val listData = it.getValue<HomeTopics>()
                    when {
                        separationMethods.isEmpty() -> {
                            separationMethods.add(listData)
                        }
                        i < separationMethods.size -> {
                            separationMethods[i] = listData
                        }
                        else -> {
                            separationMethods.add(listData)
                        }
                    }

                    i++
                }

                runOnUiThread {
                    sepMethods.adapter?.notifyDataSetChanged()
                    sepMethods.scheduleLayoutAnimation()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        Firebase.database.reference.child("nestedSubTopics").child("1").child("0")
            .addValueEventListener(learnTopicListener)

        // Set up seperation methods recyclerview
        sepMethods.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = HomescreenTopicsAdapter(separationMethods, this@RetrieveGameActivity, context, false)
            this.addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    override fun itemClicked(itemID: Int) {
        sepSubstance.text = separationMethods[itemID]?.title
    }
}