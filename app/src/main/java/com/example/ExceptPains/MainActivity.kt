package com.example.ExceptPains

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ExceptPains.Utils.Store
import com.example.ExceptPains.Notification.NotificationUtils
import com.example.ExceptPains.ScreenCap.ScreenCap
import com.example.ExceptPains.Notification.loadNotificationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 界面初始化
        setContentView(R.layout.activity_main)
        setTitle(R.string.act_home_title)
        // 其它初始化
        Store.shared.loadFromActivity(this)
        // 载入其它模块
        CoroutineScope(Dispatchers.Main).launch {
            loadModules()
        }
    }

    private suspend fun loadModules() {
        loadNotificationModule()
        // 获取截屏权限
        if (Store.shared.mediaProjectionIntent == null) {
//            ScreenCap.askForScreenshotPermission(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 对于申请截屏权限结果的处理
        ScreenCap.processPermissionAskingResult(requestCode, resultCode, data)
    }

    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
        NotificationUtils.standardAlert("这是标题", "这是文本👀")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
    }
}