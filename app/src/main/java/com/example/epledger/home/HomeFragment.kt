package com.example.epledger.home

import android.animation.LayoutTransition
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.epledger.filter.FilterCategoriesViewModel
import com.example.epledger.filter.FilterDialogFragment
import com.example.epledger.filter.FilterSourcesViewModel
import com.example.epledger.model.Filter
//import com.example.epledger.model.Record
import com.example.epledger.nav.NavigationFragment.Companion.pushToStack
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.page_home.view.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.lang.RuntimeException

class HomeFragment : Fragment() {
    private var mRecyclerView: RecyclerView? = null
    private var mSectionAdapter: SectionAdapter? = null
    private val dbModel by activityViewModels<DatabaseModel>()
    private val filterCategoriesViewModel by activityViewModels<FilterCategoriesViewModel>()
    private val filterSourcesViewModel by activityViewModels<FilterSourcesViewModel>()

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
            mSectionAdapter!!.notifyDataSetChanged()
        }
    }

    private fun finishCreatingUI() {
        val sections = dbModel.requireGroupedRecords()
        val sectionAdapter = SectionAdapter(sections, dbModel)

        val checkEmptyListRunnable = Runnable {
            if (!dbModel.databaseHasLoaded) {
                return@Runnable
            }

            GlobalScope.launch(Dispatchers.IO) {
                // 等待view初始化完成
                while (view == null) {
                    delay(50)
                }

                // 在IO线程中等待好了再回到主线程
                withContext(Dispatchers.Main) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sift -> {
                val dialog = FilterDialogFragment()
                dialog.setFilterSubmitListener(object : FilterDialogFragment.FilterSubmitListener {
                    override fun onFilterSubmit(filter: Filter?) {
                        // 筛选然后显示
                        TODO("Not yet implemented")
                    }
                })
                dialog.show(requireActivity().supportFragmentManager, null)
                true
            }
            R.id.reset -> {
                filterCategoriesViewModel.reset()
                filterSourcesViewModel.reset()
                // TODO
                true
            }
            R.id.more -> {
                true
            }
            else -> false
        }
    }
}