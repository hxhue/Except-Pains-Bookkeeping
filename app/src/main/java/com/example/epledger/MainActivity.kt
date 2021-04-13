package com.example.epledger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.example.epledger.chart.ShowChartActivity
import com.example.epledger.nav.MainPagerAdapter
import com.example.epledger.nav.MainScreen
import com.example.epledger.nav.getMainScreenForMenuItem
import com.example.epledger.qaction.CKForeground
import com.example.epledger.qaction.loadQuickActionModule
import com.example.epledger.util.Store
import com.example.epledger.qaction.screenshot.ScreenshotUtils
import com.example.epledger.util.NotificationUtils
import com.example.epledger.util.loadNotificationModule
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewPager: ViewPager
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var mainPagerAdapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 禁用黑暗模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // 界面初始化
        setupViews()
        // 其他初始化
        Store.loadFromActivity(this)
        loadModules()
    }

    private fun setupViews() {
        setContentView(R.layout.navigation)
        viewPager = findViewById(R.id.view_pager)
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        mainPagerAdapter = MainPagerAdapter(supportFragmentManager)

        // 设置图标的可见度
        bottomNavigationView.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED

        // Set items to be displayed
        mainPagerAdapter.setItems(arrayListOf(MainScreen.MAIN, MainScreen.CHARTS,
                MainScreen.OTHERS, MainScreen.SETTINGS))

        // Default page
        val defaultPage = MainScreen.MAIN
        scrollToScreen(defaultPage)
        selectBottomNavigationViewMenuItem(defaultPage.menuItemId)
        supportActionBar?.setTitle(defaultPage.titleStringId)

        // Set the listener for item selection in the bottom navigation view.
        bottomNavigationView.setOnNavigationItemSelectedListener(this)

        // Attach an adapter to the view pager and make it select the bottom navigation
        // menu item and change the title to proper values when selected.
        viewPager.adapter = mainPagerAdapter
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                val selectedScreen = mainPagerAdapter.getItems()[position]
                selectBottomNavigationViewMenuItem(selectedScreen.menuItemId)
                supportActionBar?.setTitle(selectedScreen.titleStringId)
            }
        })
    }

    /**
     * Selects the specified item in the bottom navigation view.
     */
    private fun selectBottomNavigationViewMenuItem(@IdRes menuItemId: Int) {
        bottomNavigationView.setOnNavigationItemSelectedListener(null)
        bottomNavigationView.selectedItemId = menuItemId
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
    }

    /**
     * Scrolls `ViewPager` to show the provided screen.
     */
    private fun scrollToScreen(mainScreen: MainScreen) {
        val screenPosition = mainPagerAdapter.getItems().indexOf(mainScreen)
        if (screenPosition != viewPager.currentItem) {
            viewPager.currentItem = screenPosition
        }
    }

    private fun loadModules() {
        val ctx = this.applicationContext
        loadNotificationModule(ctx)
        loadQuickActionModule(ctx)
    }

    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
        Log.d("MainActivity", "sendMessage() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
    }

    //打开ShowChartActivity
    fun sendShowChartMessage(view:View){
        val intent = Intent(this, ShowChartActivity::class.java).apply {
        }
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        getMainScreenForMenuItem(item.itemId)?.let {
            scrollToScreen(it)
            supportActionBar?.setTitle(it.titleStringId)
            return true
        }
        return false
    }
}

