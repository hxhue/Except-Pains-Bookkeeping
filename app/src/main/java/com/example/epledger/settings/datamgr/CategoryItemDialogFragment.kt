package com.example.epledger.settings.datamgr

import android.app.Dialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import com.example.epledger.R
import com.example.epledger.inbox.event.item.IconItemAdapter
import com.example.epledger.inbox.event.item.SpaceItemDecoration
import com.example.epledger.db.DatabaseModel
import com.example.epledger.util.IconAsset
import com.example.epledger.util.ScreenMetrics
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_category_edit.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CategoryItemDialogFragment: DialogFragment(), IconItemAdapter.OnPositionClickListener {
    private val dbModel: DatabaseModel by activityViewModels()
    private val mModel: CategoryDialogViewModel by viewModels()

    class CategoryDialogViewModel: ViewModel() {
        val categoryName = MutableLiveData<String>(null)
        val categoryIconResID = MutableLiveData<Int>(null)
        val categoryID = MutableLiveData<Int>(null)

        fun assembleCategory(): Category {
            return Category(categoryName.value!!, categoryIconResID.value!!, categoryID.value)
        }
    }

    /**
     * To modify an existing category in database, you need to pass a category with ID != null.
     * To create one, you shouldn't call this method.
     */
    fun bindExistingCategory(category: Category) {
        mCategoryToBind = category
    }

    private var mCategoryToBind: Category? = null

    /**
     * You can (optionally) call this method before showing this Fragment
     */
    fun setCategorySubmitListener(listener: CategorySubmitListener) {
        mCategorySubmitListener = listener
    }

    interface CategorySubmitListener {
        fun onCategorySubmit(category: Category)
    }

    private var mCategorySubmitListener: CategorySubmitListener? = null

    private var mDialogView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Check initialization
        if (mCategorySubmitListener == null) {
            throw RuntimeException("You must call setCategorySubmitListener() before showing CategoryItemDialogFragment")
        }
        if (mCategoryToBind == null) {
            mCategoryToBind = Category()
        } else if (mCategoryToBind!!.ID == null) {
            throw RuntimeException("An existing category item cannot have a null ID. Check your parameter " +
                    "on call of bindExistingCategory()")
        }
        mCategoryToBind!!.let {
            mModel.categoryName.value = it.name
            mModel.categoryID.value = it.ID
            mModel.categoryIconResID.value = it.iconResID
        }

        val ctx = requireContext()
        val inflater = LayoutInflater.from(ctx)
        val dialogContent  = inflater.inflate(R.layout.dialog_category_edit, null)
        dialogContent.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        val dialog = MaterialAlertDialogBuilder(ctx)
                .setView(dialogContent)
                .setNegativeButton(R.string.no) { _, _ -> /**/ }
                .setPositiveButton(R.string.ok) { _, _ ->
                    // Change data in viewModel
                    val categoryItem = mModel.assembleCategory()
                    categoryItem.name = dialogContent.category_name_edittext.text.trim().toString()

                    // When name is invalid, we refuse to create this category
                    if (!checkCategoryName(categoryItem)) {
                        val anotherDialog = MaterialAlertDialogBuilder(ctx)
                                .setPositiveButton(R.string.ok) { _, _ -> }
                                .setTitle(getString(R.string.changes_not_saved))
                                .setMessage(getString(R.string.category_not_saved_prompt))
                        anotherDialog.show()
                    } else {
                        // When category number is too large, we give a warning
                        if (!checkMaxCategoryNumber()) {
                            Toast.makeText(ctx, getString(R.string.category_number_too_large_prompt),
                                    Toast.LENGTH_SHORT).show()
                        }
                        mCategorySubmitListener!!.onCategorySubmit(categoryItem)
                    }
                }
                .setTitle(if (mCategoryToBind!!.ID != null) {
                    getString(R.string.modify_category)
                } else {
                    getString(R.string.new_category)
                })
                .create()

        // 1. Set up RecyclerView
        val iconRecyclerView = dialogContent.category_edit_icon_recyclerview

        // Count of span
        val display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val w = ScreenMetrics.pxToDp(display.width) * 0.8f

        // CAUTION: Hard code!
        val spanCount = ((w - 16.0f * 2 + 8.0f) / (44.0f + 8.0f)).toInt()
        val layoutMgr = GridLayoutManager(requireContext(), spanCount)
        iconRecyclerView.layoutManager = layoutMgr

        // Adjust space
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.icon_recycler_view_space)
        iconRecyclerView.addItemDecoration(SpaceItemDecoration(spacingInPixels, spanCount))
        iconRecyclerView.adapter = IconItemAdapter(IconAsset.assets, this)

        // 2. Set up iconImageView
        val iconView = dialogContent.category_icon_imageview
        iconView.isClickable = true
        iconView.setOnClickListener {
            // Hide keyboard
            val imm: InputMethodManager? =
                requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(dialogContent.category_name_edittext.windowToken, 0)
            // Move focus off input field
            dialogContent.category_name_edittext.clearFocus()

            // Wait some time and set set recyclerView visible
            // 通过Post运行能够延缓一段时间
            Handler(Looper.getMainLooper()).post {
                setIconRecyclerViewVisibility(true)
            }
        }

        // 3. Set up input field
        val editText = dialogContent.category_name_edittext
        editText.apply {
            // When view is already focused, onClickListener works
            setOnClickListener { setIconRecyclerViewVisibility(false) }
            // When view is not focused, onFocusChangeListener works
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) setIconRecyclerViewVisibility(false) }
        }

        dialog.setOnShowListener {
            // Make input field able to show keyboard when focused
            dialog.window!!.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
            editText.requestFocus()
        }

        mDialogView = dialogContent
        return dialog
    }

    // Override this can make sure onViewCreated() is called
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mDialogView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add observer
        mModel.categoryIconResID.observe(viewLifecycleOwner, {
            view.category_icon_imageview.setImageDrawable(ContextCompat.getDrawable(view.context, it))
        })

        mModel.categoryName.observe(viewLifecycleOwner, {
            view.category_name_edittext.apply {
                setText(it)
                // 将光标位置移动到末尾
                setSelection(it.length)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Prevent creation of this dialog if changes are applied
        if (savedInstanceState != null) {
            this.dismiss()
        }
        super.onCreate(savedInstanceState)
    }

    override fun onPositionClick(position: Int) {
        mModel.categoryIconResID.value = IconAsset.assets[position]
    }

    private fun checkCategoryName(category: Category): Boolean {
        if (category.name.isBlank()) {
            return false
        }

        // Check if we already have that name
        val categories = dbModel.requireCategories()
        // Bad time complex but good enough in practice
        categories.forEach {
            if ((it.name == category.name) && !(category.ID != null && category.ID == it.ID)) {
                return false
            }
        }
        return true
    }

    private fun checkMaxCategoryNumber(): Boolean {
        val categories = dbModel.requireCategories()
        return categories.size < CATEGORY_MAX_SIZE
    }

    // Some how the keyboard will cause a few events in a row
    // So I block the listener for some time (400ms)
    // But it still didn't work, the algorithm cheats us sometimes!!
    // https://stackoverflow.com/a/36259261/13785815

//    private fun setKeyboardVisibilityListener(rootView: View,
//                                              onKeyboardVisibilityListener: OnKeyboardVisibilityListener) {
//        val parentView = (rootView as ViewGroup).getChildAt(0)
//        parentView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//            private var alreadyOpen = false
//            private val defaultKeyboardHeightDP = 100
//            private val EstimatedKeyboardDP = defaultKeyboardHeightDP + 48
//            private val rect: Rect = Rect()
//            private var blockingConsequentEvent = false
//            override fun onGlobalLayout() {
//                if (blockingConsequentEvent) {
//                    return
//                }
//                val estimatedKeyboardHeight = TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    EstimatedKeyboardDP.toFloat(),
//                    parentView.resources.displayMetrics
//                ).toInt()
//                parentView.getWindowVisibleDisplayFrame(rect)
//                val heightDiff: Int = parentView.rootView.height - (rect.bottom - rect.top)
//                val isShown = heightDiff >= estimatedKeyboardHeight
//                if (isShown == alreadyOpen) {
//                    Log.i("Keyboard state", "Ignoring global layout change...")
//                    return
//                }
//                alreadyOpen = isShown
//                onKeyboardVisibilityListener.onVisibilityChanged(isShown)
//
//                // Blocking consequent events
//                blockingConsequentEvent = true
//                viewLifecycleOwner.lifecycleScope.launch {
//                    delay(400)
//                    blockingConsequentEvent = false
//                }
//            }
//        })
//    }

    private fun setIconRecyclerViewVisibility(visible: Boolean) {
        val recyclerView = requireView().category_edit_icon_recyclerview
        if (visible) {
            recyclerView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.GONE
        }
    }
}