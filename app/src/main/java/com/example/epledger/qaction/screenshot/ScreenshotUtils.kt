package com.example.epledger.qaction.screenshot

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.Image
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.epledger.qaction.tool.Store
import java.io.*

const val SCREENSHOT_REQ_CODE = 1029;

class ScreenshotUtils {
    companion object {
        /**
         * 处理权限申请结果。
         * 在申请者的onActivityResult中调用。
         */
        fun processPermissionAskingResult(ctx: Context, requestCode: Int, resultCode: Int, data: Intent?) : Boolean {
            if (requestCode != SCREENSHOT_REQ_CODE) {
                Log.d("ScreenCap.ScreenCap.onActivityResult", "requestCode (${requestCode}) is not processable by ScreenCap.ScreenCap")
                return false
            }
            // 检查是否是截图相关的结果
            if (data == null) {
                Log.d("ScreenCap.ScreenCap.onActivityResult", "data is null")
                return false
            }
            if (resultCode != Activity.RESULT_OK) {
                Log.d("ScreenCap.ScreenCap.onActivityResult", "resultCode (${resultCode}) is wrong")
                return false
            }

            Store.mediaProjectionIntent = data
            return true
        }

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
         * 创建前台服务来获取截图。
         * 在使用之前必须有截屏的权限。
         */
        fun shotScreen(identity: AppCompatActivity, eid: Int) {

            // 检查权限，当权限通过时才启动前台服务
            var data = Store.mediaProjectionIntent
            if (data == null) {
                Log.d("ScreenCap.shotScreen", "Have no permission for screen capture!")
                askForScreenshotPermission(identity)
                return
            }

            // 创建前台服务
            val fgService = Intent(identity, ScreenshotService::class.java)
            fgService.putExtra("callback", eid)
            ContextCompat.startForegroundService(identity, fgService)
        }

        /**
         * 请求截图的权限。
         */
        fun askForScreenshotPermission(identity: AppCompatActivity) {
            val ctx = identity.applicationContext
            val intent = (ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                    as MediaProjectionManager).createScreenCaptureIntent()
            identity.startActivityForResult(intent, SCREENSHOT_REQ_CODE)
        }

        /**
         * 存储图片到私有目录。存储后返回图片的全路径。
         */
        fun saveToSandbox(ctx: Context, bmp: Bitmap): String {
            val cw = ContextWrapper(ctx)
            val directory: File? = cw.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            // Generate a unique name and bind it to a file
            var file = File(directory, "${System.currentTimeMillis()}.jpg")
            while (file.exists()) {
                file = File(directory, "${System.currentTimeMillis()}.jpg")
            }

            Log.d("==> Save Screenshot", "trying to save ${file.absolutePath}")

            // Establish a stream to the file and write it
            val fos = FileOutputStream(file)
            fos.use { f ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, f)
            }

            Log.d("==> Save Screenshot", "${file.absolutePath} should've been saved.")

            // Return file node
            return file.absolutePath
        }

        /**
         * 根据文件全路径名加载Bitmap图片并返回，找不到图片时引发异常。
         */
        fun loadBitmap(ctx: Context, path: String): Bitmap {
            return BitmapFactory.decodeFile(path)
        }

        /**
         * 存储Bitmap到相册。nameWithoutExt表示文件名，但不包含末尾的文件类型扩展名。
         * TODO: 在版本比Q更低的设备上测试。
         */
        fun saveToGallery(ctx: Context, bmp: Bitmap, nameWithoutExt: String) {
            val filename = "$nameWithoutExt.jpg"
            var fos: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ctx.contentResolver?.also { resolver ->
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

        /**
         * 把argb8888格式转换成Bitmap格式返回。
         * 注意：调用者应该在使用结束后自行关闭Image。
         */
        fun argb8888ToBitmap(img: Image): Bitmap {
            // 因为Image不是用Bitmap编码的，所以需要用特殊的方式去转换成Bitmap
            val width = img.width
            val height = img.height
            val plane = img.planes[0]
            val buffer = plane.buffer
            // 每个像素的间距
            val pixelStride: Int = plane.pixelStride
            // 总的间距
            val rowStride: Int = plane.rowStride
            val rowPadding: Int = rowStride - pixelStride * width
            val bitmapImage = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                    Bitmap.Config.ARGB_8888)
            bitmapImage.copyPixelsFromBuffer(buffer)
            return bitmapImage
        }
    }
}