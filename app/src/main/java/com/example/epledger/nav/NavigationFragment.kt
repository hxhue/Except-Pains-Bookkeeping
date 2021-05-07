package com.example.epledger.nav

import android.content.Context
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.epledger.R


open class NavigationFragment: Fragment() {
    var bTitle: CharSequence? = null

    /**
     * 对返回按键进行处理。所有能够聚焦的视图都应该应用此Listener。
     */
    val onBackKeyListener = View.OnKeyListener { _, keyCode, _ ->
        if ((keyCode == KeyEvent.KEYCODE_BACK) or (keyCode == KeyEvent.KEYCODE_HOME)) {
            onBackPressed()
            true
        } else {
            false
        }
    }

    fun setNavigation(rootView: View, title: CharSequence?, vararg focusableViews: View) {
        val activity = this.activity as AppCompatActivity

        // 设置返回箭头
        val actionBar = activity.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        // 设置标题
        if (title != null) {
            bTitle = actionBar.title
            actionBar.title = title
        }

        // 设置背景
        rootView.setBackgroundColor(requireActivity().getColor(R.color.lightColorOnPrimary))

        // 对返回按键处理
        rootView.isFocusableInTouchMode = true
        rootView.setOnKeyListener(onBackKeyListener)

        // 对子View做返回按键处理
        for (v in focusableViews) {
            v.setOnKeyListener(onBackKeyListener)
        }

        // 设置菜单
        setHasOptionsMenu(true)
        activity.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    fun onBackPressed() {
        // 重设返回箭头
        val activity = this.activity as AppCompatActivity
        val actionBar = activity.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(false)

        // 重设标题
        bTitle.let {
            actionBar.title = bTitle
        }

        // 退出键盘（如果有）
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(requireView().windowToken, 0)

        // 退出这个fragment
        activity.supportFragmentManager.popBackStack()
    }

    /**
     * 对Fragment的onOptionsItemSelected的重写。增加了点击返回按钮返回的功能。
     * 在子类中使用时应该利用或短路特性：return super.onOptionsItemSelected(...) or ...
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> false
        }
    }

    companion object {
        fun pushToStack(activity: AppCompatActivity, fragment: NavigationFragment) {
            val fragmentManager = activity.supportFragmentManager
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.add(android.R.id.content, fragment).addToBackStack(null).commit()
        }
    }
}