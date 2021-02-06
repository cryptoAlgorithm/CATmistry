package com.catmistry.android

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {

    private var contentChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            bottom_navigation.selectedItemId = when(position) {
                0 -> R.id.navigation_learn
                1 -> R.id.navigation_play
                2 -> R.id.navigation_music
                else -> R.id.navigation_learn
            }
        }
    }

    private fun handleTab(id: Int): Boolean {
        return when(id) {
            R.id.navigation_learn -> {
                viewpager.setCurrentItem(0, true)
                true
            }
            R.id.navigation_play -> {
                viewpager.setCurrentItem(1, true)
                true
            }
            R.id.navigation_music -> {
                viewpager.setCurrentItem(2, true)
                true
            }
            else -> false
        }
    }

    lateinit var prefChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CATmistry)

        super.onCreate(savedInstanceState)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true) // Enable vector in old android versions

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

        // Define change listener
        prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "dyslexiaFont") recreate()
        }

        pref.registerOnSharedPreferenceChangeListener(prefChangeListener)

        when (pref.getBoolean("dyslexiaFont", false)) {
            true -> setTheme(R.style.DyslexicFont)
            false -> setTheme(R.style.Theme_CATmistry)
        }

        setContentView(R.layout.activity_main)

        // Init Firebase, first things first
        FirebaseApp.initializeApp(applicationContext)

        try {
            Firebase.database.setPersistenceEnabled(true) // Enable offline capabilities
        }
        catch (e: Exception) {}

        // Init Analytics
        Firebase.analytics

        viewpager.adapter = BottomTabAdapter(this, 3)
        viewpager.offscreenPageLimit = 3

        // Change callback to update bottom navigation's selected item
        viewpager.registerOnPageChangeCallback(contentChangeCallback)

        // Listen for bottom navigation presses
        (findViewById<View>(R.id.bottom_navigation) as BottomNavigationView).setOnNavigationItemSelectedListener { item ->
            handleTab(item.itemId)
        }

        var dialogOpen = false

        // Listen for changes to latest app version
        val appVerListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    if (BuildConfig.VERSION_NAME != dataSnapshot.value.toString()) {
                        if (dialogOpen) return // Don't show another dialog if theres already one open

                        MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle(getString(R.string.app_update_new, dataSnapshot.value.toString()))
                                .setMessage(getString(R.string.update_summary))
                                .setNegativeButton(R.string.action_later) { _, _ ->
                                    // Do nothing
                                }
                                .setPositiveButton(resources.getString(R.string.update_action)) { _, _ ->
                                    // Respond to positive button press
                                    // Open CATmistry website in a Chrome custom tab
                                    val url = "https://www.catmistry.cf/"
                                    val builder = CustomTabsIntent.Builder()
                                    val customTabsIntent = builder.build()
                                    customTabsIntent.launchUrl(this@MainActivity, Uri.parse(url))
                                }
                                .setOnDismissListener {
                                    dialogOpen = false
                                }
                                .show()

                        dialogOpen = true
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error fetching latest app version",
                    Toast.LENGTH_SHORT).show()
            }
        }

        Firebase.database.reference.child("latestVer").addValueEventListener(appVerListener)
    }
}