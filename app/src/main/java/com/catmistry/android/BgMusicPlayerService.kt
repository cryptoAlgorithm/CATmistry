package com.catmistry.android

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class BgMusicPlayerService : Service() {
    private val PLAY = "com.catmistry.android.action.PLAY"
    private val PAUSE = "com.catmistry.android.action.PAUSE"
    private val CHANGEMUSIC = "com.catmistry.android.action.CHANGEMUSIC"
    private val STOP = "com.catmistry.android.action.STOP"

    private fun getMyActivityNotification(songName: String): NotificationCompat.Builder{

        // The PendingIntent to launch our activity if the user selects
        // this notification
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, 0
        )
        // Pause/play action
        val pauseIntent = Intent(this, BgMusicPlayerService::class.java)
        if (isPaused) {
            pauseIntent.apply {
                action = PLAY
            }
        }
        else {
            pauseIntent.apply {
                action = PAUSE
            }
        }
        val pendingPauseIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0,
                    pauseIntent, 0
            )
        } else {
            PendingIntent.getService(this, 0,
                    pauseIntent, 0
            )
        }
        val text = if (isPaused) {
            "Play"
        }
        else {
            "Pause"
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(songName)
            .setContentText("CATmistry Music Player")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_round_pause_24, text, pendingPauseIntent)
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()

        when (intent.action) {
            PLAY -> {
                if (isPaused) { // Safety check
                    isPaused = false
                    mediaPlayer.start()
                    isBuffering = false
                    with(NotificationManagerCompat.from(this)) {
                        // notificationId is a unique int for each notification that you must define
                        notify(1, getMyActivityNotification(songName).build())
                    }
                }
            }
            PAUSE -> {
                if (isPlaying && !isPaused) { // Check if the player was actually playing
                    mediaPlayer.pause()
                    isPaused = true
                    with(NotificationManagerCompat.from(this)) {
                        // notificationId is a unique int for each notification that you must define
                        notify(1, getMyActivityNotification(songName).build())
                    }
                }
            }
            STOP -> {
                mediaPlayer.release()
                isPlaying = false // Must reset vars to default state
                isPaused = false
                repeat = false
                stopForeground(true) // Kill itself
                stopSelf() // Close immediately
            }
            CHANGEMUSIC -> {
                startForeground(1, getMyActivityNotification(songName).build())
                isBuffering = true
                if (isPlaying) {
                    mediaPlayer.release() // Destroy the MediaPlayer instance
                }
                val url = intent.getStringExtra("url").toString()
                mediaPlayer = MediaPlayer().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setAudioAttributes(
                                AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                        )
                    } else setAudioStreamType(AudioManager.STREAM_MUSIC) // For phones below Lollipop
                    setDataSource(url)
                    setOnCompletionListener {
                        if (repeat) {
                            isPaused = true
                            it.start()
                            isPaused = false
                        } else {
                            // Return everything to default
                            it.release() // Don't hog up system resources
                            returnDefault()
                            stopForeground(true) // Stop service
                            stopSelf() // Close
                        }
                    }
                    prepare() // might take long! (for buffering, etc)
                }
                isPlaying = true
                isPaused = true
                songName = intent.getStringExtra("songName").toString() // Store song name
            }
        }
        return START_STICKY // Restart service if killed
    }

    private fun returnDefault() {
        isPlaying = false
        isPaused = false
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Music Player Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                    NotificationManager::class.java
            )
            serviceChannel.setSound(null, null)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        var isPlaying: Boolean = false
        var isPaused: Boolean = false
        var songName: String = "No song playing"
        var repeat: Boolean = false
        var isBuffering: Boolean = false
        lateinit var mediaPlayer: MediaPlayer
        var progress: Float
            get() = currentProgress()
            set(value) = if (this::mediaPlayer.isInitialized) { mediaPlayer.seekTo((value / 100.0 * mediaPlayer.duration).toInt()) } else { /* no-op */ }
        var getElapsed: Long
            get() = mediaPlayer.currentPosition.toLong()
            set(_) = sinkHole()
        var getRemaining                                  : Long
            get() = (mediaPlayer.duration - mediaPlayer.currentPosition).toLong()
            set(_) = sinkHole()

        private fun currentProgress(): Float {
            return if (isPlaying) { // MediaPlayer has been init
                ((mediaPlayer.currentPosition / mediaPlayer.duration.toDouble()) * 100.0).toFloat()
            } else {
                0f // Playing has not even started
            }
        }

        private fun sinkHole() = Unit

        const val CHANNEL_ID = "BackgroundMusicPlayer"
    }
}