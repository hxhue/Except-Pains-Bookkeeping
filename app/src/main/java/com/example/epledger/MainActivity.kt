package com.example.epledger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.epledger.chart.ShowChartActivity
import com.example.epledger.qaction.CKForeground
import com.example.epledger.util.Store
import com.example.epledger.qaction.screenshot.ScreenshotUtils
import com.example.epledger.util.NotificationUtils
import com.example.epledger.util.loadNotificationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 禁用黑暗模式（配色困难）
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // 界面初始化
        setContentView(R.layout.activity_main)
        setTitle(R.string.act_home_title)
        // 其它初始化
        Store.loadFromActivity(this)
        // 载入其它模块
        CoroutineScope(Dispatchers.Main).launch {
            loadModules()
        }
    }

    private suspend fun loadModules() {
        val ctx = this.applicationContext
        loadNotificationModule(ctx)
        // 获取截屏权限
        if (Store.mediaProjectionIntent == null) {
//            ScreenCap.askForScreenshotPermission(this)
        }
        // 启动快捷操作
        CKForeground.launch(ctx)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 对于申请截屏权限结果的处理
        ScreenshotUtils.processPermissionAskingResult(this, requestCode, resultCode, data)
    }

    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
        Log.d("MainActivity", "sendMessage() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
    }

    //打开ShowChartActivity
    fun sendShowChartMessage(view:View){
        val intent = Intent(this, ShowChartActivity::class.java).apply {
        }
        startActivity(intent)
    }
}