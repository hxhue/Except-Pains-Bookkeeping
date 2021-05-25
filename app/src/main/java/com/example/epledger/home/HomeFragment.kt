package com.example.epledger.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.R
import com.example.epledger.model.Record
import com.example.epledger.detail.RecordDetailFragment
import com.example.epledger.db.DatabaseModel
//import com.example.epledger.model.Record
import com.example.epledger.nav.NavigationFragment.Companion.pushToStack
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.RuntimeException

class HomeFragment : Fragment() {
    private var mRecyclerView: RecyclerView? = null
    private var mSectionAdapter: SectionAdapter? = null
    private val dbModel by activityViewModels<DatabaseModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.page_home, container, false)
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view.findViewById<View>(R.id.recyclerView) as RecyclerView
        mRecyclerView!!.layoutManager = LinearLayoutManager(view.context)

        // 点击按钮，打开新建界面
        val btn: FloatingActionButton = view.findViewById(R.id.addEntryButton)
        btn.setOnClickListener {
            val frag = RecordDetailFragment()
            frag.bindRecord(Record())
            frag.setDetailRecordMsgReceiver(object : RecordDetailFragment.DetailRecordMsgReceiver {
                override fun onDetailRecordSubmit(record: Record) {
                    dbModel.insertRecord(record)
                }

                override fun onDetailRecordDelete(record: Record) {
                    throw RuntimeException("This page is used for creation so deletion is not allowed")
                }
            })
            pushToStack(requireActivity().supportFragmentManager, frag, true)
        }
        updateUI()

        // Register observers
        dbModel.groupedRecords.observeForever {
            mSectionAdapter!!.sections = it
            mSectionAdapter!!.notifyDataSetChanged()
        }
    }

    private fun updateUI() {
        val sections = dbModel.requireGroupedRecords()
        mSectionAdapter = SectionAdapter(sections, dbModel)
        mRecyclerView!!.adapter = mSectionAdapter
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.top_app_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}