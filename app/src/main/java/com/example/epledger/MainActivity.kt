package com.example.epledger

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager.widget.ViewPager
import com.example.epledger.db.*
import com.example.epledger.home.SectionAdapter
import com.example.epledger.inbox.event.item.EventItemFragment
import com.example.epledger.inbox.event.list.EventFragment
import com.example.epledger.nav.MainPagerAdapter
import com.example.epledger.nav.MainScreen
import com.example.epledger.nav.getMainScreenForMenuItem
import com.example.epledger.qaction.loadQuickActionModule
import com.example.epledger.util.loadNotificationModule
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val dbModel by viewModels<DatabaseModel>()
    private lateinit var viewPager: ViewPager
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var mainPagerAdapter: MainPagerAdapter

    // 用于帮助设置延迟，和nav.NavigationFragment一起使用
    val viewCachePolicy = hashMapOf<String, Boolean>(
        Pair(EventItemFragment::class.java.name, true),
        Pair(EventFragment::class.java.name, true)
    )

    /**
     * 由于EventItem页面打开很卡，所以需要事先缓存。
     */
    private var cachedEventItemFragment: EventItemFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 加载模块
        loadModules()
        // 禁用黑暗模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // 界面初始化
        setupViews()
    }

    private fun setupViews() {
        setContentView(R.layout.navigation)
        viewPager = findViewById(R.id.view_pager)
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        mainPagerAdapter = MainPagerAdapter(supportFragmentManager)

        // 设置图标的可见度
        bottomNavigationView.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_AUTO

        // Set items to be displayed
        mainPagerAdapter.setItems(arrayListOf(MainScreen.MAIN, MainScreen.CHARTS,
                MainScreen.INBOX, MainScreen.SETTINGS))

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

        // Set offscreen limit to pages of APP to avoid lag of reloading
        // https://stackoverflow.com/a/16781845/13785815
        viewPager.offscreenPageLimit = 4
    }

    override fun onResume() {
        super.onResume()
        dbModel.reloadDatabase()
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
        createViewCache()
        loadDatabaseModule(ctx)
    }

    private fun loadDatabaseModule(ctx: Context) {
        // Init AppDatabase
        AppDatabase = SqliteDatabase(this.applicationContext)
//        AppDatabase = MemoryDatabase(this.applicationContext)
    }

    private fun createViewCache() {
        cachedEventItemFragment = EventItemFragment()
    }

    private fun invalidateCachedViews() {
        cachedEventItemFragment = null
    }

    fun requireCachedEventItemFragment(): EventItemFragment {
        return cachedEventItemFragment!!
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        invalidateCachedViews()
//        dbModel.clearDatabase()
        Log.d("MainActivity", "onDestroy")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        getMainScreenForMenuItem(item.itemId)?.let {
            scrollToScreen(it)
            supportActionBar?.setTitle(it.titleStringId)
            return true
        }
        return false
    }

    /**
     * Set badge of InboxFragment in BottomNavigation.
     * Any number less or equal than 0 will make badge disappear.
     */
    fun setInboxBadge(num: Int) {
        if (num > 0) {
            bottomNavigationView.getOrCreateBadge(R.id.nav_inbox).number = num
        } else {
            bottomNavigationView.removeBadge(R.id.nav_inbox)
        }
    }

    var homeSectionAdapter: SectionAdapter? = null
}

fun Activity.asMainActivity(): MainActivity {
    if (this is MainActivity) {
        return (this as MainActivity)
    }
    throw RuntimeException("This activity is not MainActivity so the cast failed.")
}