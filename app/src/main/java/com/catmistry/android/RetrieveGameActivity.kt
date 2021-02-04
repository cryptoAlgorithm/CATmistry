package com.catmistry.android

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
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
import kotlinx.android.synthetic.main.activity_retrieve_game.progressBar
import kotlinx.android.synthetic.main.activity_start_game.*
import kotlinx.android.synthetic.main.table_thin_row.view.*
import java.lang.Math.abs

class SeparationGameAdapter(
    private val data: ArrayList<String>,
    private val clickListener: TableRowClickListener,
    private val dataSeq: ArrayList<Int>
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
        holder.content.text = data[dataSeq[position]]
    }

    override fun getItemCount(): Int {
        return dataSeq.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: MaterialTextView = itemView.rowContent
    }
}

class RetrieveGameActivity : AppCompatActivity(), RecyclerViewClickListener, TableRowClickListener {
    private val separationMethods: ArrayList<HomeTopics?> = ArrayList()
    private val separationSubstances: ArrayList<String> = ArrayList()
    private val sepAns: ArrayList<Int> = ArrayList()
    private val subSeq: ArrayList<Int> = ArrayList()
    private var selectedSubstance = -1
    private var waveProg = 50
    private var totalTime = 0.0
    private var timeLeft: Double? = null
    private var currentTimerThread: Thread? = null
    private var continueTimer = false

    // Timer code
    private fun startTimer(totalTime: Double) {
        if (totalTime == 0.0) return // Don't start timer if time is 0
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
                        selectedSubstance = 0
                        itemClicked(separationMethods.size + 1) // Will always be wrong
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set app theme based on preferences
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        when (pref.getString(
                "theme",
                "auto"
        )) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // Dark theme
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // Light theme
            else -> { // Follow system theme/unset
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        .edit()
                        .putString("theme", "auto")
                        .apply() // Prevent unset state
            }
        }

        when (pref.getBoolean("dyslexiaFont", false)) {
            true -> setTheme(R.style.DyslexicFont)
            false -> setTheme(R.style.Theme_CATmistry)
        }

        setContentView(R.layout.activity_retrieve_game)

        if (intent.extras?.getDouble("difficulty") == 1.0) progressBar.visibility = View.INVISIBLE // Inf time at lowest diff
        else totalTime = (5 - intent.extras?.getDouble("difficulty")!!) * 4000 // Diff 2: 16 secs, diff 3: 8 secs, diff 4: 4 secs

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

                checkSeqLen()
                continueTimer = true
                startTimer(totalTime)
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
            this.adapter = SeparationGameAdapter(separationSubstances, this@RetrieveGameActivity, subSeq)
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

    private fun checkSeqLen() {
        if ((waveProg / 10) - subSeq.size <= separationSubstances.size) {
            repeat((waveProg / 10) - subSeq.size) {
                var selectedIndex: Int
                do {
                    selectedIndex = (0 until separationSubstances.size).random()
                } while (subSeq.contains(selectedIndex))

                subSeq.add(selectedIndex)
            }
        }

        runOnUiThread {
            sepSubstances.adapter?.notifyDataSetChanged()
            sepSubstances.scheduleLayoutAnimation()
        }
    }

    private fun stopGame() {
        subSeq.clear()
        selectedSubstance = -1
        runOnUiThread {
            sepSubstances.adapter?.notifyDataSetChanged()
            sepSubstances.scheduleLayoutAnimation()
        }
    }

    override fun itemClicked(itemID: Int) {
        if (selectedSubstance < 0) {
            Snackbar.make(sepMethods, R.string.choose_sep_substance, Snackbar.LENGTH_SHORT).show()
            return
        }

        continueTimer = false
        currentTimerThread?.interrupt()
        timeLeft = null

        // Check if ans is correct
        if (sepAns[subSeq[selectedSubstance]] == itemID) waveProg -= 10
        else {
            waveProg += 10
            Snackbar.make(sepMethods, getString(R.string.correct_ans_wrong, separationMethods[sepAns[subSeq[selectedSubstance]]]?.title), Snackbar.LENGTH_LONG)
                    .show()
        }

        subSeq.removeAt(selectedSubstance)
        selectedSubstance = -1 // Reset selected variables
        sepSubstance.text = getString(R.string.choose_sep_substance)

        // Set cup progress
        wave_view.setProgress(waveProg)

        // Check if the cup is empty or full
        if (waveProg <= 0) {
            Snackbar.make(sepMethods, "Won!", Snackbar.LENGTH_SHORT).show()
            startActivity(
                Intent(this, GameEndActivity::class.java)
                .putExtra("won", true))
            stopGame()
            return
        }
        else if (waveProg >= 100) {
            Snackbar.make(sepMethods, "Lost!", Snackbar.LENGTH_SHORT).show()
            startActivity(
                Intent(this, GameEndActivity::class.java)
                    .putExtra("won", false))
            stopGame()
            return
        }

        checkSeqLen() // Update length of substances to be separated

        // Change wave background
        when {
            waveProg <= 20 -> changeCupBg(Color.rgb(67, 160, 71)) // Green - good
            waveProg >= 80 -> changeCupBg(Color.rgb(229, 57, 53)) // Red   - bad
            else -> changeCupBg(Color.rgb(3, 155, 229)) // Blue - normal
        }

        // Resume timer
        continueTimer = true
        startTimer(totalTime)
    }

    override fun rowClicked(itemID: Int) {
        sepSubstance.text = getString(
            R.string.sep_substance_template, separationSubstances[subSeq[kotlin.math.abs(itemID)]].split(
                " - "
            )[0]
        )
        selectedSubstance = itemID
    }
}