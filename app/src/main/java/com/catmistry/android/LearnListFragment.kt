package com.catmistry.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_learn_list.*

class LearnListFragment : Fragment(), RecyclerViewClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_learn_list, container, false)
    }

    val learningTopics: ArrayList<HomeTopics?> = ArrayList()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Put the logik here
        val database = Firebase.database.reference

        val learnTopicListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0
                dataSnapshot.children.forEach {
                    val listData = it.getValue<HomeTopics>()
                    when {
                        learningTopics.isEmpty() -> {
                            learningTopics.add(listData)
                        }
                        i < learningTopics.size -> {
                            learningTopics[i] = listData
                        }
                        else -> {
                            learningTopics.add(listData)
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
        database.child("learnTopics").addValueEventListener(learnTopicListener)

        // Set up recyclerView
        recyclerView.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = HomescreenTopicsAdapter(learningTopics, this@LearnListFragment, context)
            this.addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }

        // Appbar onclick listeners
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.appbar_settings -> {
                    startActivity(Intent(requireActivity().applicationContext, SettingsActivity::class.java)) // Open settings
                    true
                }
                else -> false
            }
        }
    }

    override fun itemClicked(itemID: Int) {
        Log.e("Quiz topic", itemID.toString())
        /*val quizIntent = Intent(requireActivity(), LearnQuizActivity::class.java)
        quizIntent.putExtra("quizTopic", itemID.toString())
        startActivity(quizIntent)*/
        val quizIntent = Intent(requireActivity(), HomeLearnListViewer::class.java)
        quizIntent.putExtra("quizHeader", learningTopics[itemID]?.title)
        quizIntent.putExtra("quizIndex", itemID.toString())
        startActivity(quizIntent)
    }

    // Extension functions
    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }
}