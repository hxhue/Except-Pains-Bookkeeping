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
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.epledger.R
import com.example.epledger.db.DatabaseModel
import com.example.epledger.nav.NavigationFragment
import com.example.epledger.qaction.loadQuickActionModule
import com.example.epledger.settings.datamgr.CategoryManagerFragment
import com.example.epledger.settings.datamgr.SourceManagerFragment


class SettingsFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    var alertDialog: AlertDialog? = null

    private val dbModel by activityViewModels<DatabaseModel>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setHasOptionsMenu(true)
        setUpOnClickListeners()
    }

    private fun setUpOnClickListeners() {
        // Management
        val sourceManagement = preferenceScreen.findPreference<Preference>("manage_sources")
        sourceManagement?.setOnPreferenceClickListener {
            val newFragment = SourceManagerFragment()
            NavigationFragment.pushToStack(this.requireActivity().supportFragmentManager, newFragment)
            true
        }

        val categoryManagement = preferenceScreen.findPreference<Preference>("manage_tags")
        categoryManagement?.setOnPreferenceClickListener {
            val newFragment = CategoryManagerFragment()
            NavigationFragment.pushToStack(this.requireActivity().supportFragmentManager, newFragment)
            true
        }

        // Data
        val eJSON = preferenceScreen.findPreference<Preference>("data_export_json")
        eJSON?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Toast.makeText(requireContext(), "data_export_json", Toast.LENGTH_SHORT).show()
            true
        }

        val eCSV = preferenceScreen.findPreference<Preference>("data_export_csv")
        eCSV?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Toast.makeText(requireContext(), "data_export_csv", Toast.LENGTH_SHORT).show()
            true
        }

        val iJSON = preferenceScreen.findPreference<Preference>("data_import_json")
        iJSON?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Toast.makeText(requireContext(), "data_import_json", Toast.LENGTH_SHORT).show()
            true
        }


        val iAlipay = preferenceScreen.findPreference<Preference>("data_import_alipay")
        iAlipay?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Toast.makeText(requireContext(), "data_import_alipay", Toast.LENGTH_SHORT).show()
            true
        }

        val iWechat = preferenceScreen.findPreference<Preference>("data_import_wechat")
        iWechat?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Toast.makeText(requireContext(), "data_import_wechat", Toast.LENGTH_SHORT).show()
            true
        }

        // Debug
        val reloadDB = preferenceScreen.findPreference<Preference>("reload_db")
        reloadDB?.setOnPreferenceClickListener {
            dbModel.reloadDatabase()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_settings_question -> {
                // 显示说明页面
                val newFragment = SpecificationFragment()
                NavigationFragment.pushToStack(requireActivity().supportFragmentManager, newFragment, true)
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
                        context, getString(R.string.stop_ck_prompt),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}