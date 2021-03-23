package com.example.ExceptPains

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ExceptPains.ScreenCap.ScreenCap
import com.example.ExceptPains.Notification.getNotifier
import com.example.ExceptPains.Notification.loadNotificationModule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


private lateinit var mainContext: Context
private var scHeight: Int = -1;
private var scWidth: Int = -1;

fun getMainContext(): Context {
    return mainContext
}

fun getScreenHeight(): Int {
    return scHeight
}
fun getScreenWidth(): Int {
    return scWidth
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainContext = this.applicationContext
        loadModules();
        setContentView(R.layout.activity_main)
    }

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus && (scHeight < 0 || scWidth < 0)) {
//            scHeight = this.window.decorView.rootView.measuredHeight
//            scWidth = this.window.decorView.rootView.measuredWidth
//            Log.d("MainActivity.onWindowFocusChanged", "scHeight=${scHeight}, scWidth=${scWidth}")
//            // -TODO: DEBUG
//            ScreenCap.shotScreen(this@MainActivity)
//        }
//    }

    private fun loadModules() {
        loadNotificationModule()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ScreenCap.shared.processActivityResult(requestCode, resultCode, data)
    }

    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
        getNotifier().standardAlert("这是标题", "这是文本👀")
    }

}