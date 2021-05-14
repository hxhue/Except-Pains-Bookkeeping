package com.example.epledger.inbox.event.list

import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.epledger.R
import com.example.epledger.inbox.event.viewmodel.EventViewModel


class EventPreferenceFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    val eventsModel: EventViewModel by activityViewModels()
    val eventAdapter = EventAdapter()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.events_preference, rootKey)
        setUpAlertTimePreference()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.overScrollMode = View.OVER_SCROLL_NEVER

//        val mgr = childFragmentManager
//        val trans = mgr.beginTransaction()
//        val frag = EventListFragment()
//        trans.add(R.id.frame_event_list_wrapper, frag)
//        trans.addToBackStack("event_list")
//        trans.commit()

        // 含有FragmentContainer的xml不能够被PreferenceScreen正确inflate，因此只能够把eventList逻辑内联
//        val eventPreference = preferenceScreen.findPreference<Preference>("event_list_placeholder")
//        val eventList = view.frag_event_list!!
//        eventList.adapter = eventAdapter
//        eventList.layoutManager = LinearLayoutManager(requireContext())
//        eventsModel.events.observe(viewLifecycleOwner, {
//            eventAdapter.eventItems = it
//            eventAdapter.notifyDataSetChanged()
//        })
    }

    private fun setUpAlertTimePreference() {
        val alertTimeKey = "noti_time"
        val perf = PreferenceManager.getDefaultSharedPreferences(context)
        val alertTimePreference = preferenceScreen.findPreference<Preference>(alertTimeKey)
        val alertTime = perf.getInt(alertTimeKey, -1)
        if (alertTime >= 0) {
            val hour = alertTime / 60
            val minute = alertTime % 60
            alertTimePreference?.summary = String.format(
                getString(R.string.noti_time_sum_fmt), "%02d:%02d".format(hour, minute)
            )
        } else {
            alertTimePreference?.summary = getString(R.string.have_not_set_time_prompt)
        }
        alertTimePreference?.setOnPreferenceClickListener {
            val timePicker = TimePickerDialog(requireContext(), { picker, hr, m ->
                    // 记录数值
                    val timeAlertValue = hr * 60 + m
                    val editor = perf.edit()
                    editor.putInt(alertTimeKey, timeAlertValue)
                    editor.apply()

                    // TODO: 修改已经存在的定时消息的通知时间

                    // 视图调整
                    alertTimePreference.summary = String.format(
                        getString(R.string.noti_time_sum_fmt), "%02d:%02d".format(hr, m)
                    )
                },
                12,
                0,
                true
            )
//            timePicker.setOnShowListener {
//                val color = requireContext().getColor(R.color.lightColorSecondary)
//                timePicker.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(color)
//                timePicker.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(color)
//            }
            timePicker.show()
            true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        //
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}