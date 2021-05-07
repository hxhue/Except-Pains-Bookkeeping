package com.example.epledger.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.epledger.R
import com.example.epledger.nav.NavigationFragment
import com.example.epledger.qaction.loadQuickActionModule


class SettingsFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    var alertDialog: AlertDialog? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setHasOptionsMenu(true)

        val eJSON = preferenceScreen.findPreference<Preference>("data_export_json")
        eJSON?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Toast.makeText(requireContext(), "clicked", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_settings_question -> {
                // 显示说明页面
                val newFragment = SpecificationFragment()
                NavigationFragment.pushToStack(activity as AppCompatActivity, newFragment)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroyView() {
        // Prevent leak of view
        alertDialog?.let {
            it.dismiss()
            alertDialog = null
        }
        super.onDestroyView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences == null) {
            Log.d("settings", "argument sharedPreferences is null.")
            return
        }

        // 状态栏功能开关变更
        if (key == "qa_notification") {
            loadQuickActionModule(requireContext())
            // If the preference is changed to false
            if (!sharedPreferences.getBoolean("qa_notification", true)) {
                Toast.makeText(
                        context, "Stopping foreground service. Please be patient.",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}