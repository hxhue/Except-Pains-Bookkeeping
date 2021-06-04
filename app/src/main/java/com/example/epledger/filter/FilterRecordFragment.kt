package com.example.epledger.filter

import com.example.epledger.nav.NavigationFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.home.SectionAdapter
import com.example.epledger.db.DatabaseModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.epledger.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.epledger.model.Filter
import com.example.epledger.model.RecordGroup
import kotlinx.android.synthetic.main.filter_record.view.*
import kotlinx.android.synthetic.main.page_home.view.*
import kotlinx.coroutines.*
import java.lang.Runnable

class FilterRecordFragment : NavigationFragment() {
    private var mRecyclerView: RecyclerView? = null
    private val databaseModel by activityViewModels<DatabaseModel>()
    private var bindingFilter: Filter? = null
    private var mSectionAdapter: SectionAdapter? = null

    /**
     * 在创建后必须被调用一次
     */
    fun bindFilter(filter: Filter?) {
        bindingFilter = filter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.filter_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view.findViewById(R.id.filter_record_recyclerView)
        mRecyclerView!!.layoutManager = LinearLayoutManager(view.context)

        setUpView(view)

        // Register observers
        databaseModel.filterGroupedRecords.observeForever {
            mSectionAdapter!!.sections = it
            mSectionAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setUpView(view: View) {
        val sections: List<RecordGroup> = databaseModel!!.requireGroupedRecordsByFilter(bindingFilter!!)
        val sectionAdapter = SectionAdapter(sections, databaseModel)

        val checkEmptyListRunnable = Runnable {
            if (!databaseModel.databaseHasLoaded) {
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
                        view.filter_fragment_no_record_image.apply {
                            alpha = 1.0f
                            visibility = View.VISIBLE
                        }
                    } else {
                        view.filter_fragment_no_record_image.apply {
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
        })

        mSectionAdapter = sectionAdapter
        mRecyclerView!!.adapter = sectionAdapter
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()
    }

    override fun onResume() {
        val view = requireView()
        val newTitle = requireContext().getString(R.string.filter_result)

        setNavigation(view, newTitle)

        super.onResume()
    }
}