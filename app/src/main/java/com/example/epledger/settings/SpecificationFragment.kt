package com.example.epledger.settings

import android.os.Bundle
import android.view.*
import com.example.epledger.R
import com.example.epledger.nav.NavigationFragment


class SpecificationFragment: NavigationFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 控制菜单栏
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_specification, container, false)
        setNavigation(rootView, requireContext().getString(R.string.specification_title))
        return rootView
    }

}