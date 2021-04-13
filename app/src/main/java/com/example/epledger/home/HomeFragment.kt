package com.example.epledger.home

import com.example.epledger.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.epledger.nav.MainPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {
//    private lateinit var viewPager: ViewPager
//    private lateinit var bottomNavigationView: BottomNavigationView
//    private lateinit var mainPagerAdapter: MainPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.page_home, container, false)
    }
}