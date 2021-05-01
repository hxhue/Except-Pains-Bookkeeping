package com.example.epledger.qaction

import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Intent
import android.service.quicksettings.TileService
import android.util.Log
import java.util.*

/**
 * A TileService will display a toggle in the quick-settings window.
 * This is a shortcutting to open a window and record your expenses.
 */
class NewRecordTileService : TileService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NewRecordTileService", "onStartCommand() called at ${System.currentTimeMillis()}")
        return super.onStartCommand(intent, flags, startId)
    }

    // 点击启动该卡片
    override fun onClick() {
        super.onClick()
        Log.d("NewRecordTileService", "onClicked() called at ${System.currentTimeMillis()}")

        val recCardIntent = Intent(this, PopupActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    .setAction(Intent.ACTION_MAIN)

        // Slow after pressing HOME from any app(even not from mine!)
        // This is because of Google's concern about app privilege
        // Nothing to do with my code...
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            startActivityAndCollapse(recCardIntent)
        }
    }
}