package com.example.epledger.inbox

import android.os.Bundle
import android.view.*
import com.example.epledger.R
import com.example.epledger.nav.NavigationFragment

class NotificationFragment: NavigationFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        setNavigation(view, requireContext().getString(R.string.notifications))
        setupView(view)
        return view
    }

    private fun setupView(view: View) {

    }
}