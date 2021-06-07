package com.example.epledger.settings

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.epledger.R
import com.example.epledger.db.DatabaseModel
import com.example.epledger.db.ImportDataFromExcel
import com.example.epledger.nav.NavigationFragment
import com.example.epledger.qaction.loadQuickActionModule
import com.example.epledger.settings.datamgr.CategoryManagerFragment
import com.example.epledger.settings.datamgr.SourceManagerFragment
import com.example.epledger.db.FileManager
import java.io.File

class SettingsFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    var alertDialog: AlertDialog? = null
    var mode=0
    private val dbModel by activityViewModels<DatabaseModel>()
    private val myActivityLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { activityResult ->
        var path=""
        if(activityResult.size>=1)
        {
            val get=GetAbsolutePath()
            path= get.getFilePathFromURI(this.context,activityResult.get(0))
            val file = File(path)
        }
        System.out.println(activityResult.size)
        System.out.println(path)
        val tmp=ImportDataFromExcel(this.context)
        if(mode==1&&path!="")  tmp.alia_base(path)
        else if(mode==2&&path!="") tmp.weixin_base(path)
        else if(mode==3&&path!="")
        {
            val f=FileManager(this.context)
            f.reloadDb(path)

        }

    }

    /*rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { activityResult ->
            /*if(activityResult.path == Activity.RESULT_OK){
                val result = activityResult.data?.getStringExtra("result")
                if (result != null) {
                    val path=result
                }
            }*/
          val path=activityResult.path
        }*/
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
            NavigationFragment.pushToStack(this.requireActivity().supportFragmentManager, newFragment, true)
            true
        }

        val categoryManagement = preferenceScreen.findPreference<Preference>("manage_tags")
        categoryManagement?.setOnPreferenceClickListener {
            val newFragment = CategoryManagerFragment()
            NavigationFragment.pushToStack(this.requireActivity().supportFragmentManager, newFragment, true)
            true
        }

        // Data
        val eJSON = preferenceScreen.findPreference<Preference>("data_export_json")
        eJSON?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            //Toast.makeText(requireContext(), "数据库文件已保存在/storage/emulated/0/Database中", Toast.LENGTH_SHORT).show()
            val file=FileManager(this.context)
            file.writeDb()
            val info = this.context?.let { it1 -> AlertDialog.Builder(it1) }
            //info.setIcon(R.drawable.dnn_rnd)
            if (info != null) {
                info.setTitle("提示")
                info.setMessage("数据库文件已保存在/storage/emulated/0/Database")
                info.setPositiveButton(getString(R.string.ok)) {_, _->}
                info.setNegativeButton(getString(R.string.cancel)) {_, _->}
                //info.setNeutralButton(getString(R.string.more)) {_, _->}
                info.show()
            }


            true
        }

        val eCSV = preferenceScreen.findPreference<Preference>("data_export_csv")
        eCSV?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val tmp=ImportDataFromExcel(this.context)
            tmp.exportToCSV()
            val info = this.context?.let { it1 -> AlertDialog.Builder(it1) }
            //info.setIcon(R.drawable.dnn_rnd)
            if (info != null) {
                info.setTitle("提示")
                info.setMessage("数据已保存在/storage/emulated/0/excel.csv")
                info.setPositiveButton(getString(R.string.ok)) {_, _->}
                info.setNegativeButton(getString(R.string.cancel)) {_, _->}
                //info.setNeutralButton(getString(R.string.more)) {_, _->}
                info.show()
            }
            //Toast.makeText(requireContext(), "数据已保存在/storage/emulated/0/excel.csv", Toast.LENGTH_SHORT).show()
            true
        }

        val iJSON = preferenceScreen.findPreference<Preference>("data_import_json")
        iJSON?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            //Toast.makeText(requireContext(), "data_import_json", Toast.LENGTH_SHORT).show()
            mode=3
            var string_array:Array<String> = arrayOf("*/*")
            myActivityLauncher.launch(string_array)
            true
        }


        val iAlipay = preferenceScreen.findPreference<Preference>("data_import_alipay")
        iAlipay?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            //Toast.makeText(requireContext(), "data_import_alipay", Toast.LENGTH_SHORT).show()
            //true
            /*val  intent = Intent().apply {
                putExtra("name","Hello,技术最TOP")
            }*/
            mode=1
            var string_array:Array<String> = arrayOf("*/*")
            myActivityLauncher.launch(string_array)

            true
            //val intent=Intent(Intent.ACTION_GET_CONTENT)
            /*myActivityLauncher.launch(

                )
            )*/


        }

        val iWechat = preferenceScreen.findPreference<Preference>("data_import_wechat")
        iWechat?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            //Toast.makeText(requireContext(), "data_import_wechat", Toast.LENGTH_SHORT).show()
            var string_array:Array<String> = arrayOf("*/*")
            mode=2
            myActivityLauncher.launch(string_array)
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

