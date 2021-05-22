package com.example.epledger.inbox.event.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.MainActivity
import com.example.epledger.R
import com.example.epledger.inbox.event.viewmodel.EventViewModel
import com.example.epledger.inbox.event.item.EventItemFragment
import com.example.epledger.nav.NavigationFragment
import kotlinx.android.synthetic.main.fragment_event_list.view.*

class EventListFragment: PreferenceFragmentCompat(), EventAdapter.OnPositionClickListener {
    private val eventsModel: EventViewModel by activityViewModels()
    private val eventAdapter = run {
        val adapter = EventAdapter()
        adapter.onPositionClickListener = this
        adapter
    }
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_event_list, container, false)
        setUpView(view)
        setUpModel(view)
        return view
    }

    override fun onResume() {
        super.onResume()
//        if (!eventsModel.isNewEvent() && eventsModel.eventIndex >= 0) {
//            eventAdapter.notifyItemChanged(eventsModel.eventIndex)
//        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (savedInstanceState == null) {

        }
    }

    private fun setUpView(view: View) {
        recyclerView = view.frag_event_list
        recyclerView.adapter = eventAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setUpModel(view: View) {
        eventsModel.events.observe(viewLifecycleOwner, {
            eventAdapter.eventItems = it
            eventAdapter.notifyDataSetChanged()
        })
        eventsModel.currentEvent.observe(viewLifecycleOwner, {
            if (!eventsModel.isNewEvent() && eventsModel.eventIndex >= 0)  {
                eventAdapter.notifyItemChanged(eventsModel.eventIndex)
            }
        })
    }

    override fun onClick(position: Int) {
        val newFragment = (this.activity as MainActivity).requireCachedEventItemFragment()
        newFragment.shouldCopyItem = true

        eventsModel.setNewEvent(false)
        val curEvent = eventsModel.events.value!![position]
        eventsModel.eventIndex = position
        eventsModel.setEditing(false)
        eventsModel.setCurrentEvent(curEvent)

        NavigationFragment.pushToStack(requireActivity().supportFragmentManager, newFragment)
    }

}