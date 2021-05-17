package com.example.epledger.nav

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.epledger.R
import java.lang.RuntimeException


open class NavigationFragment: Fragment() {
    var nfPreviousTitle: CharSequence? = null
    var nfCurrentTitle: CharSequence? = null
    var nfPreviousHasBackArrow = false
    var nfNavigationSet = false

    /**
     * 如果要重写onResume，则把super.onResume()放在最后调用
     */
    override fun onResume() {
        val actionBar = (activity as AppCompatActivity).supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        nfCurrentTitle?.let {
            (this.activity as AppCompatActivity).supportActionBar?.title = it
        }
        if (!nfNavigationSet) {
            throw RuntimeException("setNavigation() must be called before super.onResume() in " +
                    "a subclass of NavigationFragment")
        }
        super.onResume()
    }

    /**
     * 设置和导航相关的重要属性，在创建view时调用。最后一个参数现已废弃。
     */
    fun setNavigation(rootView: View, title: CharSequence) {
        val activity = this.activity as AppCompatActivity
        val actionBar = activity.supportActionBar!!

        // 设置标题
        nfPreviousTitle = actionBar.title
        actionBar.title = title
        nfCurrentTitle = title

        // 返回箭头的状态只记录一次
        if (!nfNavigationSet) {
            this.nfPreviousHasBackArrow = (actionBar.displayOptions and ActionBar.DISPLAY_HOME_AS_UP) != 0
        }
        nfNavigationSet = true

        // 设置返回箭头
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        // 设置背景
        rootView.setBackgroundColor(requireActivity().getColor(R.color.lightColorOnPrimary))

        // 设置菜单
        setHasOptionsMenu(true)
        activity.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onStop() {
        resumePreviousActionBar()
        super.onStop()
    }

    /**
     * 恢复ActionBar。具有幂等特性。
     */
    private fun resumePreviousActionBar() {
        // 重设返回箭头
        val activity = this.activity as AppCompatActivity
        val actionBar = activity.supportActionBar!!
        val mgr = activity.supportFragmentManager
        actionBar.setDisplayHomeAsUpEnabled(nfPreviousHasBackArrow)

        // 重设标题
        nfPreviousTitle.let {
            actionBar.title = nfPreviousTitle
        }
    }

    /**
     * 返回值表示在退出前调用了收起键盘的功能。
     */
    private fun hideKeyboard(): Boolean {
        // 退出键盘
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(requireView().windowToken, 0)
        return imm != null
    }

    fun exitNavigationFragment() {
        val activity = this.activity as AppCompatActivity
        if (hideKeyboard()) {
            // 如果有键盘存在，则需要稍微延时一段时间，否则键盘收起时切换页面可能出现视图跳跃的视觉感受
            Handler(Looper.myLooper()!!).postDelayed({
                activity.supportFragmentManager.popBackStack()
            }, 50)
        } else {
            activity.supportFragmentManager.popBackStack()
        }
    }

    /**
     * 对Fragment的onOptionsItemSelected的重写。增加了点击返回按钮返回的功能。
     * 在子类中使用时应该利用或短路特性：return super.onOptionsItemSelected(...) or ...
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                exitNavigationFragment()
                true
            }
            else -> false
        }
    }

    companion object {
        fun pushToStack(fragmentManager: FragmentManager, fragment: NavigationFragment,
                        fromMainPage: Boolean = false) {
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()
            // Set transition animations
//            if (fromMainPage) {
//                transaction.setCustomAnimations(
//                        R.anim.fade_in,
//                        R.anim.fade_out,
//                        R.anim.fade_in,
//                        R.anim.fade_out
//                )
//            } else {
//                // Somewhat ugly...
////                transaction.setCustomAnimations(
////                        R.anim.slide_in,
////                        R.anim.slide_out_below,
////                        R.anim.slide_in_below,
////                        R.anim.slide_out
////                )
//                transaction.setCustomAnimations(
//                        R.anim.fade_in,
//                        R.anim.fade_out,
//                        R.anim.fade_in,
//                        R.anim.fade_out
//                )
//            }
            // Use replace to get animation to work
//            transaction.replace(android.R.id.content, fragment)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            // Use add to ignore bottom-layer animation
            transaction.add(android.R.id.content, fragment)
            // Commit
            transaction.addToBackStack(null).commit()
        }
    }
}