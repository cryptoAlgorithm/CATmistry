package com.catmistry.android

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_retrieve_game.*
import kotlinx.android.synthetic.main.table_thin_row.view.*

class SeparationGameAdapter(
    private val data: ArrayList<String>,
    private val clickListener: TableRowClickListener
): RecyclerView.Adapter<SeparationGameAdapter.ViewHolder>() {
    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T { // Adapter for onClickListener
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.table_thin_row, parent, false)

        return ViewHolder(view)
            .listen { pos, _ ->
                clickListener.rowClicked(pos)
            }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.content.text = data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: MaterialTextView = itemView.rowContent
    }
}

class RetrieveGameActivity : AppCompatActivity(), RecyclerViewClickListener, TableRowClickListener {
    private val separationMethods: ArrayList<HomeTopics?> = ArrayList()
    private val separationSubstances: ArrayList<String> = ArrayList()
    private val sepAns: ArrayList<Int> = ArrayList()
    private var selectedSubstance = -1
    private var waveProg = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrieve_game)

        // Load list of separation methods
        val learnTopicListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0
                dataSnapshot.children.forEach {
                    val listData = it.getValue<HomeTopics>()
                    // Separation methods
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

        val separationSubstancesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0
                dataSnapshot.children.forEach {
                    when {
                        separationSubstances.isEmpty() -> {
                            separationSubstances.add(it.child("title").value.toString())
                        }
                        i < separationSubstances.size -> {
                            separationSubstances[i] = it.child("title").value.toString()
                        }
                        else -> {
                            separationSubstances.add(it.child("title").value.toString())
                        }
                    }


                    // Separation answers
                    when {
                        sepAns.isEmpty() -> {
                            it.child("correctAns").getValue<Int>()?.let { ans -> sepAns.add(ans) }
                        }
                        i < sepAns.size -> {
                            it.child("correctAns").getValue<Int>()?.let { ans -> sepAns[i] = ans }
                        }
                        else -> {
                            it.child("correctAns").getValue<Int>()?.let { ans -> sepAns.add(ans) }
                        }
                    }

                    i++
                }

                runOnUiThread {
                    sepSubstances.adapter?.notifyDataSetChanged()
                    sepSubstances.scheduleLayoutAnimation()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        Firebase.database.reference.child("sepGameArray").addValueEventListener(
            separationSubstancesListener
        )

        // Set up seperation methods recyclerview
        sepMethods.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = HomescreenTopicsAdapter(
                separationMethods,
                this@RetrieveGameActivity,
                context,
                false
            )
            this.addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }

        sepSubstances.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = SeparationGameAdapter(separationSubstances, this@RetrieveGameActivity)
            this.addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }


    private fun changeCupBg(newColor: Int) {
        var oldColor = Color.TRANSPARENT
        val background: Drawable = wave_view.background
        if (background is ColorDrawable) oldColor = background.color

        if (oldColor == newColor) return

        val anim = ValueAnimator.ofObject(ArgbEvaluator(), oldColor, newColor)
        anim.duration = 250 // Animation time in MS

        anim.addUpdateListener { animator -> wave_view.setBackgroundColor(animator.animatedValue as Int) }
        anim.start()
    }

    override fun itemClicked(itemID: Int) {
        if (selectedSubstance < 0) {
            Snackbar.make(sepMethods, R.string.choose_sep_substance, Snackbar.LENGTH_SHORT).show()
            return
        }
        // Check if ans is correct
        if (sepAns[selectedSubstance] == itemID) waveProg -= 10
        else waveProg += 10
        // Check if the cup is empty or full
        if (waveProg < 0) {
            Snackbar.make(sepMethods, "Won!", Snackbar.LENGTH_SHORT).show()
            return
        }
        else if (waveProg > 100) {
            Snackbar.make(sepMethods, "Lost!", Snackbar.LENGTH_SHORT).show()
            return
        }
        separationSubstances.removeAt(selectedSubstance)
        sepAns.removeAt(selectedSubstance)
        selectedSubstance = -1 // Reset selected variables
        sepSubstance.text = getString(R.string.choose_sep_substance)
        runOnUiThread {
            sepSubstances.adapter?.notifyDataSetChanged()
        }
        // Set cup progress
        wave_view.setProgress(waveProg)
        // Change wave background
        when {
            waveProg <= 20 -> changeCupBg(Color.rgb(67, 160, 71)) // Green - good
            waveProg >= 80 -> changeCupBg(Color.rgb(229, 57, 53)) // Red   - bad
            else -> changeCupBg(Color.rgb(3, 155, 229)) // Blue - normal
        }
    }

    override fun rowClicked(itemID: Int) {
        sepSubstance.text = getString(
            R.string.sep_substance_template, separationSubstances[itemID].split(
                " - "
            )[0]
        )
        selectedSubstance = itemID
    }
}