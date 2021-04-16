package com.example.epledger.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.epledger.R
import com.example.epledger.qaction.loadQuickActionModule

class SettingsFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val eJSON = preferenceScreen.findPreference<Preference>("data_export_json")
        eJSON?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Toast.makeText(requireContext(), "clicked", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences == null) {
            Log.d("settings.SettingsFragment", "argument sharedPreferences is null.")
            return
        }

        // 状态栏功能开关变更
        if (key == "qa_notification") {
            loadQuickActionModule(requireContext())
            // If the preference is changed to false
            if (!sharedPreferences.getBoolean("qa_notification", true)) {
                Toast.makeText(context, "Stopping foreground service. Please be patient.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

}