package com.example.epledger.settings.datamgr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.epledger.R
import com.example.epledger.db.AppDatabase
import com.example.epledger.db.DatabaseModel
import com.example.epledger.model.Category
import com.example.epledger.nav.NavigationFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.mgr_rec_cate.view.*
import kotlinx.android.synthetic.main.mgr_rec_cate_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryManagerFragment: NavigationFragment() {
    private val dbModel: DatabaseModel by activityViewModels()
    private val recyclerViewAdapter by lazy {
        val categories = dbModel.requireCategories()
        CategoryAdapter(categories, requireActivity().supportFragmentManager, dbModel)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.mgr_rec_cate, container, false)
        setUpView(view)
        setUpModel(view)
        return view
    }

    private fun setUpView(view: View) {
        setNavigation(view, getString(R.string.categories))
        val ctx = view.context
        
        // RecyclerView
        val recyclerView = view.mgr_rec_cate_recyclerview
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(ctx)

        // Floating button
        val button = view.floating_add
        button.setOnClickListener {
            val dialog = CategoryItemDialogFragment()
            dialog.setCategorySubmitListener(object : CategoryItemDialogFragment.CategorySubmitListener {
                override fun onCategorySubmit(category: Category) {
                    // TODO: insert this new category into database
                    dbModel.insertCategory(category) {
                        recyclerViewAdapter.notifyItemInserted(dbModel.requireCategories().size)
                    }
//                    GlobalScope.launch(Dispatchers.IO) {
//                        category.ID = AppDatabase.insertCategory(category)
//                        val categories = dbModel.categories.value!!
//                        val size = categories.size
//                        categories.add(category)
//                        withContext(Dispatchers.Main) {
//                            recyclerViewAdapter.notifyItemInserted(size)
//                        }
//                    }
                }
            })
            dialog.show(requireActivity().supportFragmentManager, null)
        }
    }

    private fun setUpModel(view: View) {
        dbModel.categories.observeForever {
            recyclerViewAdapter.categoryList = it
            recyclerViewAdapter.notifyDataSetChanged()
        }
    }

    class CategoryAdapter(
        var categoryList: MutableList<Category>,
        private val fragmentManager: FragmentManager,
        private val dbModel: DatabaseModel
    ):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        class ViewHolder(view: View): RecyclerView.ViewHolder(view) {}

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.mgr_rec_cate_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val categoryItem = categoryList[position]
            val view = holder.itemView
            val ctx = view.context
            val imageView = view.mgr_rec_cate_item_image
            val textView = view.mgr_rec_cate_item_textview

            // Set image
            imageView.setImageDrawable(ContextCompat.getDrawable(ctx, categoryItem.iconResID))

            // Set name
            textView.setText(categoryItem.name)

            // Long click to delete
            view.setOnLongClickListener {
                val dialog = MaterialAlertDialogBuilder(ctx)
                        .setMessage(ctx.getString(R.string.del_item_confirm))
                        .setNegativeButton(R.string.no) { _, _ -> /* nothing */ }
                        .setPositiveButton(R.string.ok) { _, _ ->
                            dbModel.deleteCategoryByID(categoryItem.ID!!) {
                                this@CategoryAdapter.notifyItemRemoved(position)
                                this@CategoryAdapter.notifyItemRangeChanged(position, categoryList.size)
                            }
//                            GlobalScope.launch(Dispatchers.IO) {
//                                AppDatabase.deleteCategoryByID(categoryList[position].ID!!)
//                                categoryList.removeAt(position)
//                                withContext(Dispatchers.Main) {
//                                    this@CategoryAdapter.notifyItemRemoved(position)
//                                    this@CategoryAdapter.notifyItemRangeChanged(position, categoryList.size)
//                                }
//                            }
                        }
                dialog.show()
                true
            }

            // Short click to modify name
            view.setOnClickListener {
                val dialog = CategoryItemDialogFragment()
                dialog.bindExistingCategory(categoryList[position].copy())
                dialog.setCategorySubmitListener(object: CategoryItemDialogFragment.CategorySubmitListener {
                    override fun onCategorySubmit(category: Category) {
                        dbModel.updateCategory(category) {
                            this@CategoryAdapter.notifyItemChanged(position)
                        }
//                        GlobalScope.launch(Dispatchers.IO) {
//                            AppDatabase.updateCategory(category)
//                            categoryList[position] = category
//                            withContext(Dispatchers.Main) {
//                                this@CategoryAdapter.notifyItemChanged(position)
//                            }
//                        }
                    }
                })
                dialog.show(fragmentManager, null)
            }
        }

        override fun getItemCount(): Int {
            return categoryList.size
        }
    }
}