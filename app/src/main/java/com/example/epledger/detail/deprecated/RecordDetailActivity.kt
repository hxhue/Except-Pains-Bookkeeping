package com.example.epledger.detail.deprecated

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.epledger.R
import com.example.epledger.detail.DetailRecord
import com.example.epledger.qaction.screenshot.ScreenshotUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Exception
import java.lang.RuntimeException

class RecordDetailActivity : AppCompatActivity() {
    /**
     * 注意：必须获取一份拷贝。不能和其它组件共用数据。
     */
    lateinit var bindingRecord: DetailRecord
    lateinit var recordCopy: DetailRecord

    /**
     * 当前的视图是否处于编辑模式。
     */
    var recordBeingEdited = false

    /**
     * 使用这个接口引用来通知Fragment进行更新。
     */
    var updatableFragment: DetailFragmentInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 从intent中获取记录，获取失败则抛出异常
//        bindingRecord = intent.getParcelableExtra("record")
//            ?: throw RuntimeException("You should pass a record to the intent " +
//                    "before you try to launch this activity by it. " +
//                    "Hint: intent.putExtra(\"record\", yourRecord)")

        // 从路径恢复截图信息（因为图片太大不能够传递，但路径可以）
//        bindingRecord.screenshotPath?.let { path ->
//            try {
//                bindingRecord.screenshot = ScreenshotUtils.loadBitmap(this, path)
//            } catch (e: Exception) {
//                throw RuntimeException("Screenshot path is not null but wrong. " +
//                        "Please CHECK database or UI code")
//            }
//        }

        // 如果当前ledgerRecord和一个数据库中已有数据绑定，则开启时处于非修改态，否则处于修改态
        recordBeingEdited = (bindingRecord.ID == null)

        // 复制ledgerRecord以便退出更改时能够完整还原
        recordCopy = bindingRecord.getCopy()

        // 准备界面
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setTitleByRecord(bindingRecord)
        setContentView(R.layout.activity_detail)
    }

    // 创建OptionsMenu时用。如果状态改变，直接调用相关的invalidate方法使Options重新加载即可。
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        if (recordBeingEdited) {
            if (bindingRecord.ID == null) {
                inflater.inflate(R.menu.detail_menu_creating, menu)
            } else {
                inflater.inflate(R.menu.detail_menu_editing, menu)
            }
        } else {
            inflater.inflate(R.menu.detail_menu_non_editing, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
            R.id.menu_item_detail_submit -> {
                Toast.makeText(this, "Submit", Toast.LENGTH_SHORT).show()
                // TODO
            }
            R.id.menu_item_detail_discard -> {
                Toast.makeText(this, "Discard", Toast.LENGTH_SHORT).show()
                recordBeingEdited = false
                // 复原做出的修改
                recordCopy.copyTo(bindingRecord)
                applyChanges()
            }
            R.id.menu_item_detail_edit -> {
                recordBeingEdited = true
                applyChanges()
            }
            R.id.menu_item_detail_delete -> {
                val dialog = MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.rec_del_confirm))
                        .setPositiveButton(getString(R.string.sure)) { _, _ ->
                            // TODO：在此处删除该条记录
                            this.finish()
                        }
                        .setNegativeButton(getString(R.string.no)) { _, _ -> /* Nothing */ }
                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 根据当前状态的更变，更新当前的视图。
     */
    private fun applyChanges() {
        updatableFragment?.updateUI()
        invalidateOptionsMenu()
    }

    /**
     * 根据记录是否存在ID来设置标题。
     */
    private fun setTitleByRecord(record: DetailRecord) {
        if (record.ID == null) {
            setTitle(getString(R.string.detail_page_title_create))
        } else {
            setTitle(getString(R.string.detail_page_title_modify))
        }
    }
}