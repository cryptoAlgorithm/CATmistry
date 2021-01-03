package com.catmistry.android

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

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
        setContentView(R.layout.activity_main)

        viewpager.adapter = BottomTabAdapter(this, 3)
        viewpager.offscreenPageLimit = 2

        // Change callback to update bottom navigation's selected item
        viewpager.registerOnPageChangeCallback(contentChangeCallback)

        // Listen for bottom navigation presses
        (findViewById<View>(R.id.bottom_navigation) as BottomNavigationView).setOnNavigationItemSelectedListener { item ->
            handleTab(item.itemId)
        }
    }
}