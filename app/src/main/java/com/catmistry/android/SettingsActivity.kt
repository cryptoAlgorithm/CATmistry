package com.catmistry.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.lambdaworks.crypto.SCryptUtil
import kotlinx.android.synthetic.main.password_dialog_layout.*
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

            var devToolsClickedTimes = 0

            findPreference<Preference>("devMode")?.setOnPreferenceClickListener {
                // Must click 5 times in 1 second
                devToolsClickedTimes++
                // Check if the button has been clicked 5 times
                if (devToolsClickedTimes >= 5) {
                    devToolsClickedTimes = 0 // Reset counter

                    val container = layoutInflater.inflate(R.layout.password_dialog_layout, null)

                    // Build password dialog
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(resources.getString(R.string.devtools_pwd_title))
                        .setMessage(R.string.devtools_content)
                        .setView(container)
                        .setNeutralButton(resources.getString(android.R.string.cancel)) { _, _ ->
                            // Respond to neutral button press
                        }
                        .setPositiveButton(resources.getString(android.R.string.ok)) { _, _ ->
                            // Respond to positive button press
                            runOnUiThread {
                                Thread {
                                    val enteredPwd = container.findViewById<TextInputLayout>(R.id.devtoolsPwdField).editText?.text.toString()
                                    runOnUiThread { Toast.makeText(requireActivity(), R.string.calc_hash, Toast.LENGTH_LONG).show() }
                                    if (SCryptUtil.check(enteredPwd, requireActivity().getString(R.string.devtools_pwd_hash))) {
                                            runOnUiThread {
                                                startActivity(Intent(requireActivity(), DevToolsActivity::class.java))
                                                Toast.makeText(requireActivity(), R.string.devtools_pwd_yes, Toast.LENGTH_SHORT).show() }
                                    }
                                    else runOnUiThread { Toast.makeText(requireActivity(), R.string.devtools_pwd_no, Toast.LENGTH_SHORT).show() }
                                }.start()
                            }
                        }
                        .show()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    devToolsClickedTimes--
                }, 2200)
                true
            }

            findPreference<Preference>("shareApp")?.setOnPreferenceClickListener {
                // Share app text

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text))
                    type = "text/plain"
                }

                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_app)))
                true
            }

            findPreference<Preference>("appVer")?.summary = getString(R.string.settings_app_ver, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

            findPreference<Preference>("updateApp")?.setOnPreferenceClickListener {
                // Open CATmistry website in a Chrome custom tab
                val url = "https://www.catmistry.cf/"
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
                true
            }

            // Listen for changes to latest app version
            val appVerListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        findPreference<Preference>("updateApp")?.title =
                                if (BuildConfig.VERSION_NAME == dataSnapshot.value.toString()) getString(R.string.no_app_update)
                                else getString(R.string.app_update_new, dataSnapshot.value.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching latest app version",
                            Toast.LENGTH_SHORT).show()
                }
            }

            Firebase.database.reference.child("latestVer").addValueEventListener(appVerListener)
        }

        private fun Fragment?.runOnUiThread(action: () -> Unit) {
            this ?: return
            if (!isAdded) return // Fragment not attached to an Activity
            activity?.runOnUiThread(action)
        }
    }
}