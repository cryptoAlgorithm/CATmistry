package com.catmistry.android

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class BottomTabAdapter(activity: AppCompatActivity, private val itemsCount: Int) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return itemsCount
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> LearnListFragment()
            1 -> GameListFragment()
            2 -> MusicFragment()
            // 2 -> LearnListFragment() // For debug purposes
            else -> LearnListFragment()
        }
    }
}