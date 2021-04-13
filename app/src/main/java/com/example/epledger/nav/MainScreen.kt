package com.example.epledger.nav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.epledger.R
import com.example.epledger.home.HomeFragment
import com.example.epledger.settings.SettingsFragment

/**
 * Screens available for display in the main screen, with their respective titles,
 * icons, and menu item IDs and fragments.
 */
enum class MainScreen(@IdRes val menuItemId: Int,
                      @DrawableRes val menuItemIconId: Int,
                      @StringRes val titleStringId: Int,
                      val fragment: Fragment) {
    MAIN(R.id.nav_home, android.R.drawable.ic_menu_manage, R.string.nav_home, HomeFragment()),
    CHARTS(R.id.nav_charts, android.R.drawable.ic_menu_manage, R.string.nav_charts, FakeChartsFragment()),
    OTHERS(R.id.nav_others, android.R.drawable.ic_menu_manage, R.string.nav_others, FakeOthersFragment()),
    SETTINGS(R.id.nav_settings, android.R.drawable.ic_menu_manage, R.string.nav_settings, SettingsFragment()),
}

fun getMainScreenForMenuItem(menuItemId: Int): MainScreen? {
    for (mainScreen in MainScreen.values()) {
        if (mainScreen.menuItemId == menuItemId) {
            return mainScreen
        }
    }
    return null
}


class FakeChartsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

class FakeOthersFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}