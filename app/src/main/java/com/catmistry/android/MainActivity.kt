package com.catmistry.android

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.ktx.analytics
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
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set app theme based on preferences
        when (PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(
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

        setContentView(R.layout.activity_main)

        // Init Firebase, first things first
        FirebaseApp.initializeApp(applicationContext)

        try {
            Firebase.database.setPersistenceEnabled(true) // Enable offline capabilities
        }
        catch (e: Exception) {}

        // Init Analytics
        Firebase.analytics

        viewpager.adapter = BottomTabAdapter(this, 2)
        viewpager.offscreenPageLimit = 2

        // Change callback to update bottom navigation's selected item
        viewpager.registerOnPageChangeCallback(contentChangeCallback)

        // Listen for bottom navigation presses
        (findViewById<View>(R.id.bottom_navigation) as BottomNavigationView).setOnNavigationItemSelectedListener { item ->
            handleTab(item.itemId)
        }
    }
}