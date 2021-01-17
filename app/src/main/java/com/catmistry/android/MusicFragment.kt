package com.catmistry.android

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_music.*
import kotlinx.android.synthetic.main.song_row.view.*
import java.util.*

class MusicFragment : Fragment() {

    private fun String.capitalizeWords(): String = // Addon function for String class
        split(" ").joinToString(" ") { it.toLowerCase(Locale.ROOT).capitalize(Locale.ROOT) }

    private lateinit var ref: StorageReference

    private fun listRaw() {
        val inflater = layoutInflater
        val songTable = songsList

        ref = FirebaseStorage.getInstance("gs://catmistry-android.appspot.com").reference.child("music")

        ref.listAll()
            .addOnSuccessListener { listResult ->
                listResult.prefixes.forEach { _ ->
                    /* NOTES:
                    _ = prefix
                    All the prefixes under listRef.
                    You may call listAll() recursively on them.
                    I won't actually need this function
                    */
                }

                listResult.items.forEach { item ->
                    // All the items under listRef.
                    val row = inflater.inflate(R.layout.song_row, songTable, false)
                    val songName = item.name.replace("_", " ").capitalizeWords().substringBeforeLast(
                        '.',
                        ""
                    )
                    row.country.text = songName
                    val param = row.country.layoutParams as ViewGroup.MarginLayoutParams
                    val margin = (5 *
                            Resources.getSystem().displayMetrics.density).toInt()
                    param.setMargins(margin * 3, margin, margin, margin) // Left margin should be 15dp
                    row.country.layoutParams = param
                    // Replace underscores with spaces and capitalise (Somehow resources cannot have any of those)
                    row.setOnClickListener { it ->
                        BottomSheetBehavior.from(musicSheet).state = BottomSheetBehavior.STATE_COLLAPSED
                        if (buffering.visibility == View.VISIBLE) {
                            Snackbar.make(
                                it,
                                "Another song is currently buffering. Please wait",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                        else {
                            buffering.visibility = View.VISIBLE
                            item.downloadUrl.addOnSuccessListener {
                                // Got the download URL for the music
                                val intent = Intent(requireContext(), BgMusicPlayerService::class.java)
                                    .apply {
                                        action = "com.catmistry.android.action.CHANGEMUSIC"
                                    }
                                    .putExtra(
                                        "url",
                                        it.toString()
                                    )
                                    .putExtra("songName", songName)
                                // .putExtra("srcResource", field.getInt(field))

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    requireActivity().startForegroundService(intent)
                                } else {
                                    requireActivity().startService(intent)
                                }
                                // Music will stop when source is changed. So start it again
                                val playIntent =
                                    Intent(requireContext(), BgMusicPlayerService::class.java)
                                        .apply {
                                            action = "com.catmistry.android.action.PLAY"
                                        }
                                        .putExtra("songName", songName)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    requireActivity().startForegroundService(playIntent)
                                } else {
                                    requireActivity().startService(playIntent)
                                }
                                // PausePlay won't work as it thinks it was not playing
                                pausePlay.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.ic_round_pause_24
                                    )
                                )

                                songTitle.text = songName
                            }.addOnFailureListener {
                                // Handle any errors
                                Snackbar.make(
                                    pausePlay,
                                    "Failed to decode URL",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    runOnUiThread {
                        songTable.addView(row)
                        seekBar.value = 0f // Reset it
                    }
                }
            }
            .addOnFailureListener {
                // Uh-oh, an error occurred!
                // Smol problem lol
                Snackbar.make(
                    pausePlay,
                    "No internet connectivity",
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    private fun playPause() {
        if (!BgMusicPlayerService.isPaused && BgMusicPlayerService.isPlaying) {
            val intent = Intent(requireContext(), BgMusicPlayerService::class.java)
                .apply {
                    action = "com.catmistry.android.action.PAUSE"
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().startForegroundService(intent)
            }
            else {
                requireActivity().startService(intent)
            }
            pausePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_round_play_arrow_24
                )
            )
        }
        else if (!BgMusicPlayerService.isPlaying) {
            // No song is queued
            Snackbar.make(
                pausePlay,
                "Please choose a song from the list",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        else {
            val intent = Intent(requireContext(), BgMusicPlayerService::class.java)
                .apply {
                    action = "com.catmistry.android.action.PLAY"
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().startForegroundService(intent)
            }
            else {
                requireActivity().startService(intent)
            }
            pausePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_round_pause_24
                )
            )
        }
    }

    private fun updateElements() {
        // Updates progress bar and text
        if (BgMusicPlayerService.isPlaying) {
            // Enable buttons
            runOnUiThread {
                seekBar.isEnabled = true
                stopFAB.isEnabled = true
                pausePlay.isEnabled = true
                seekBar.value = BgMusicPlayerService.progress
            }
            val millisecondsElapsed = BgMusicPlayerService.getElapsed
            val millisecondsRemaining = BgMusicPlayerService.getRemaining
            runOnUiThread {
                elapsed.text = requireActivity().getString(R.string.duration_template,
                        (millisecondsElapsed/1000/60).toString().padStart(2, '0'),
                        (millisecondsElapsed/1000%60).toString().padStart(
                                2,
                                '0'
                        )) // Minutes:Seconds
                remaining.text = requireActivity().getString(R.string.duration_template,
                        (millisecondsRemaining/1000/60).toString().padStart(2, '0'),
                        (millisecondsRemaining/1000%60).toString().padStart(
                                2,
                                '0'
                        )) // Minutes:Seconds

                // Change icon
                if (BgMusicPlayerService.isPaused) {
                    pausePlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_round_play_arrow_24
                            )
                    )
                }
                else {
                    pausePlay.setImageDrawable(
                            ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_round_pause_24
                            )
                    )
                }
            }
        }
        else {
            runOnUiThread {
                seekBar.value = 0f
                seekBar.isEnabled = false
                stopFAB.isEnabled = false
                pausePlay.isEnabled = false
                pausePlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_round_play_arrow_24
                    )
                )
            }
        }

        if (!BgMusicPlayerService.isBuffering) {
            runOnUiThread {
                buffering.visibility = View.INVISIBLE
            }
        }
        else {
            runOnUiThread {
                buffering.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listRaw()

        updateElements() // Need to update once first
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed(object : Runnable {
            override fun run() {
                updateElements()
                handler.postDelayed(this, 500)
            }
        }, 1000)

        seekBar.addOnChangeListener { _, pos, fromUser ->
            if (fromUser) {
                BgMusicPlayerService.progress = pos
                updateElements()
            }
        }
        seekBar.setLabelFormatter { _ ->
            try {
                val millisecondsElapsed = BgMusicPlayerService.getElapsed
                requireActivity().getString(R.string.duration_template,
                        (millisecondsElapsed/1000/60).toString().padStart(2, '0'),
                        (millisecondsElapsed/1000%60).toString().padStart(2, '0')) // Minutes:Seconds
            } catch (e: Exception) { "--:--" }
        }

        if (!BgMusicPlayerService.isPaused && BgMusicPlayerService.isPlaying) {
            // The music service was playing
            pausePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_round_pause_24
                )
            )
        }
        songTitle.text = BgMusicPlayerService.songName

        pausePlay.setOnClickListener {
            playPause()
        }
        stopFAB.setOnClickListener {
            seekBar.value = 0f
            elapsed.text = getString(R.string.duration_placeholder)
            remaining.text = getString(R.string.duration_placeholder) // Clear both TextViews
            songTitle.text = getString(R.string.no_playing_song) // Reset to default song
            val intent = Intent(requireContext(), BgMusicPlayerService::class.java)
                .apply {
                    action = "com.zerui.hackathonthing.action.STOP"
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().startForegroundService(intent)
            }
            else {
                requireActivity().startService(intent)
            }
            pausePlay.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_round_play_arrow_24
                )
            )
        }
        repeatToggle.setOnCheckedChangeListener { _, isChecked ->
            BgMusicPlayerService.repeat = isChecked
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music, container, false)
    }

    private fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }
}