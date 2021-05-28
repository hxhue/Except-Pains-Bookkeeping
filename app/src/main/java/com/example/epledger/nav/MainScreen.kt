package com.example.epledger.nav

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.epledger.R
import com.example.epledger.chart.ChartsFragment
import com.example.epledger.home.HomeFragment
import com.example.epledger.inbox.content.InboxFragment
import com.example.epledger.settings.SettingsFragment

/**
 * Screens available for display in the main screen, with their respective titles,
 * icons, and menu item IDs and fragments.
 */
enum class MainScreen(@IdRes val menuItemId: Int,
                      @StringRes val titleStringId: Int,
                      val fragment: Fragment) {
    MAIN(R.id.nav_home, R.string.nav_home, HomeFragment()),
    CHARTS(R.id.nav_charts, R.string.nav_charts, ChartsFragment()),
    OTHERS(R.id.nav_inbox, R.string.nav_inbox, InboxFragment()),
    SETTINGS(R.id.nav_settings, R.string.nav_settings, SettingsFragment()),
}

fun getMainScreenForMenuItem(menuItemId: Int): MainScreen? {
    for (mainScreen in MainScreen.values()) {
        if (mainScreen.menuItemId == menuItemId) {
            return mainScreen
        }
    }
    return null
}

class EmptyFragment : Fragment() {}