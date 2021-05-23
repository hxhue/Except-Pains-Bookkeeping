package com.example.epledger.inbox.event.list

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.epledger.MainActivity
import com.example.epledger.R
import com.example.epledger.asMainActivity
import com.example.epledger.inbox.event.viewmodel.EventViewModel
import com.example.epledger.inbox.event.item.EventItem
import com.example.epledger.inbox.event.item.EventItemFragment
import com.example.epledger.nav.NavigationFragment
import kotlinx.android.synthetic.main.fragment_event.view.*
import kotlinx.android.synthetic.main.fragment_event_list.view.*
import java.util.Date

class EventFragment: NavigationFragment() {
    val model: EventViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_event, container, false)
        setNavigation(view, requireContext().getString(R.string.notifications))
        setUpView(view)
        return view
    }

    private fun setUpView(view: View) {
        view.btn_add_event_to_list.setOnClickListener {
            val newFragment = (this.activity as MainActivity).requireCachedEventItemFragment()
            newFragment.shouldCopyItem = true

            model.setNewEvent(true)
            model.setEditing(true)
            val newEvent = EventItem("", Date(), 1, EventItem.CycleUnit.DAY)
            model.setCurrentEvent(newEvent)

            // 尝试增加延迟来减缓动画卡顿
            pushToStack(
                requireActivity().supportFragmentManager,
                newFragment,
                fromMainPage = false,
                this.requireActivity().asMainActivity().viewCachePolicy
            )
        }
    }
}