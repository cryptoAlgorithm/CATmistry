package com.catmistry.android

import android.content.Intent
import android.os.Bundle
import android.preference.SwitchPreference
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {
    // Scam android function
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        setSupportActionBar(topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<ListPreference>("theme")?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    // Immediately apply new theme
                    when (newValue) {
                        "dark" -> { // Dark theme
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        }
                        "light" -> { // Light theme
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }
                        else -> { // Follow system theme/unset
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        }
                    }
                    true
                }

            findPreference<SwitchPreferenceCompat>("dyslexiaFont")?.setOnPreferenceClickListener {
                requireActivity().recreate()
                true
            }

            findPreference<Preference>("shareApp")?.setOnPreferenceClickListener {
                // Share app text

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text))
                    type = "text/plain"
                }

                startActivity(Intent.createChooser(sendIntent, null))
                true
            }
        }
    }
}