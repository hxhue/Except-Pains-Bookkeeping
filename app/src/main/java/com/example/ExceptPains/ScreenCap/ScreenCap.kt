package com.example.ExceptPains.ScreenCap

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.ExceptPains.getMainContext
import com.example.ExceptPains.getScreenHeight
import com.example.ExceptPains.getScreenWidth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

const val SCREENSHOT_REQ_CODE = 1029;

class ScreenCap : AppCompatActivity() {
    /**
     * 2020.3.23: 只有满足条件的服务才能启动此功能。全屏截图暂无法使用。
     * **此功能没有完成，不应使用**。
     */
    fun processActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val ctx = getMainContext()
        if (requestCode != SCREENSHOT_REQ_CODE) {
            Log.d("ScreenCap.ScreenCap.onActivityResult", "requestCode (${requestCode}) is not processable by ScreenCap.ScreenCap")
            return
        }
        // 检查是否是截图相关的结果
        if (data == null) {
            Log.d("ScreenCap.ScreenCap.onActivityResult", "data is null")
            return
        }
        if (resultCode != -1) {
            Log.d("ScreenCap.ScreenCap.onActivityResult", "resultCode (${resultCode}) is wrong")
            return
        }

        val proj = (getMainContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager).getMediaProjection(Activity.RESULT_OK, data)
        val mImageReader: ImageReader = ImageReader.newInstance(
            getScreenWidth(),
            getScreenHeight(),
            ImageFormat.FLEX_RGBA_8888, 1
        )

        val vDisplay = proj.createVirtualDisplay("screen-mirror",
            getScreenWidth(),
            getScreenHeight(),
            Resources.getSystem().getDisplayMetrics().densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader.getSurface(), null, null)

        GlobalScope.launch {
            val image: Image = mImageReader.acquireLatestImage()
            //-TODO: 处理image
        }

//        val handler = Handler()
//        handler.
//
//        vDisplay.release()
    }

    companion object {
        val shared = ScreenCap()

        /**
         * 截取传来的View的图片，返回一个Bitmap图像。该图像可以用一些手段显示或存储。
         * 只有当前的view已经完成布局（已经初始化了宽高）之后才能够调用。否则会引发异常。
         */
        fun shotView(v: View): Bitmap {
            val width = v.measuredWidth
            val height = v.measuredHeight
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

//            val backgroundDrawable: Drawable = v.getBackground()
//            backgroundDrawable.draw(canvas)

            v.draw(canvas)
            return bitmap
        }

        /**
         * 以一个activity为基础来启动新的activity，并等待获取结果。
         * 这个activity必须在对应的onActivityResult中调用ScreenCap.shared.processActivityResult来完成剩余步骤。
         * 因为本模块的onActivityResult中暂未解决错误问题，**本功能无法使用**。
         */
        fun shotScreen(act: AppCompatActivity) {
            val intent = (getMainContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                    as MediaProjectionManager).createScreenCaptureIntent()
            act.startActivityForResult(intent, SCREENSHOT_REQ_CODE)
        }

        //-TODO: 逻辑是不对的，目前只是调试阶段使用
        /**
         * 存储图片到私有目录。nameWithoutExt表示文件名，但不包含末尾的文件类型扩展名。
         * **暂无法正常使用**
         */
        private fun savePrivately(bmp: Bitmap, nameWithoutExt: String) {
            val cw = ContextWrapper(getMainContext())
            val directory: File? = cw.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(directory, "$nameWithoutExt.jpg")
            if (file.exists()) {
                Log.d("ScreenCap.ScreenCap.savePrivately", "****Found this unique file.")
                /**
                 * -TODO: 一些疑惑。
                 * 可以确认文件确实存储成功了，但是没有办法方便获取和查看，也不在相册中。
                 * 另外，如果名字已经被使用了，则应该生成一个新名字，并且返回一个新名字。
                 * 也不太清楚这个名字是从外面传过来，还是里面随机生成完传出去。
                 */
                file.delete()
            }
            Log.d("path", file.toString())
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                /**
                 * 约定Log的标签使用子包名.类名.方法名
                 */
                Log.d("ScreenCap.ScreenCap.savePrivately", "Trying to save ${file.toString()}.")
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /**
         * 存储Bitmap到相册。nameWithoutExt表示文件名，但不包含末尾的文件类型扩展名。
         * -TODO: 将约定的最小兼容版本上升到安卓Q，目前暂未在版本更低的设备上测试
         */
        fun saveToGallery(bmp: Bitmap, nameWithoutExt: String) {
            val filename = "$nameWithoutExt.jpg"
            var fos: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getMainContext().contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }
            fos?.use {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

        }
    }
}