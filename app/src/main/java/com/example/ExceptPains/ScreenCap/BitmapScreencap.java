package com.example.ExceptPains.ScreenCap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 不应该使用此方法。
 */
public class BitmapScreencap {
    public final static BitmapScreencap Get = new BitmapScreencap();
    private BitmapScreencap() { }
    @Deprecated
    public Bitmap Screen() {
        // 这种方法没有权限
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStreamWriter outputStream = new OutputStreamWriter(process.getOutputStream());
            outputStream.write("/system/bin/screencap -p\n");
            outputStream.flush();
            Bitmap screen = BitmapFactory.decodeStream(process.getInputStream());
            outputStream.write("exit\n");
            outputStream.flush();
            outputStream.close();
            return screen;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}