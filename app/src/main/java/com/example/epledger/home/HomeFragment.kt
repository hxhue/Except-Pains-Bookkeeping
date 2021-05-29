package com.example.epledger.home

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.R
import com.example.epledger.asMainActivity
import com.example.epledger.model.Record
import com.example.epledger.detail.RecordDetailFragment
import com.example.epledger.db.DatabaseModel
//import com.example.epledger.model.Record
import com.example.epledger.nav.NavigationFragment.Companion.pushToStack
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.page_home.view.*
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

        finishCreatingUI()

        // Register observers
        dbModel.groupedRecords.observeForever {
            mSectionAdapter!!.sections = it
            updateLoadTimes()
            mSectionAdapter!!.notifyDataSetChanged()
        }
    }

    private var loadTimes = 0

    /**
     * 返回是否已经完成初次加载。因为首次加载是加载空数据，所以首次加载应该是第二次加载。
     */
    private fun updateLoadTimes() {
        loadTimes = if (loadTimes >= 2) loadTimes else (loadTimes + 1)
    }

    private fun firstLoadIsFinished(): Boolean {
        return (loadTimes >= 2)
    }

    private fun finishCreatingUI() {
        val sections = dbModel.requireGroupedRecords()
        val sectionAdapter = SectionAdapter(sections, dbModel)

        val checkEmptyListRunnable = Runnable {
            if (!firstLoadIsFinished()) {
                return@Runnable
            }
            if (sectionAdapter.sections.isNullOrEmpty()) {
                requireView().home_page_no_record_image.apply {
                    alpha = 1.0f
                    visibility = View.VISIBLE
                }
            } else {
                requireView().home_page_no_record_image.apply {
                    alpha = 0.0f
                    visibility = View.INVISIBLE
                }
            }
        }

        sectionAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkEmptyListRunnable.run()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmptyListRunnable.run()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmptyListRunnable.run()
            }
        })

        mSectionAdapter = sectionAdapter
        mRecyclerView!!.adapter = sectionAdapter
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()

        // Save reference in Activity
        requireActivity().asMainActivity().homeSectionAdapter = mSectionAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.top_app_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}