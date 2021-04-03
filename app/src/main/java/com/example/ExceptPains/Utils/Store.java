package com.example.ExceptPains.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ExceptPains.ScreenCap.ScreenCap;

/**
 * 用来存储启动时保存的环境信息。
 * 被本身没有Context和其它属性的工具类来使用。
 * 用bundle之类的手段做存储会更好一点。这个只要离线就没了。
 */
public class Store {
    private int height = -1;    // 屏幕的高度
    private int width = -1;     // 屏幕的宽度
    private Context appContext = null;
    private Intent mediaProjectionIntent = null; // 带有截图权限的intent

    private Store() {}

    public static Store shared = new Store();

    private boolean screenOnceFlag = false;

    // 设置宽高
    public synchronized void saveWidthAndHeight(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Width and height must be > 0.");
        }
        if (!screenOnceFlag) {
            screenOnceFlag = true;
            width = w;
            height = h;
            Log.d("Context.RuntimeContext.saveWidthAndHeight",
                    "Width and height stored.");
        }

    }

    // 设置应用上下文
    public synchronized void setAppContext(Context ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("Application Context can't be null.");
        }
        if (this.appContext != null) {
            Log.d("Context.RuntimeContext.setAppContext",
                    "Application Context Already stored. No need to store twice.");
            return;
        }
        appContext = ctx;
    }

    public synchronized int getHeight() {
        return height;
    }

    public synchronized int getWidth() {
        return width;
    }

    public synchronized Context getAppContext() {
        return appContext;
    }

    public synchronized Intent getMediaProjectionIntent() {
        return mediaProjectionIntent;
    }

    public synchronized void setMediaProjectionIntent(Intent intent) {
        this.mediaProjectionIntent = intent;
    }

    /**
     * @param activity
     * 从活动中加载部分重要内容。
     */
    public synchronized void loadFromActivity(AppCompatActivity activity) {
        // 存储上下文
        Store.shared.appContext = activity.getApplicationContext();
        // 存储宽高
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int width = size.x;
        int height = size.y;
        Store.shared.saveWidthAndHeight(width, height);
    }
}
