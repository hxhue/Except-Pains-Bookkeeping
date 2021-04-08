package com.example.epledger.qaction

import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Intent
import android.service.quicksettings.TileService
import android.util.Log
import java.util.*


class NewRecordTileService : TileService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("qaction.NewRecordTileService", "onStartCommand() called at ${System.currentTimeMillis()}")
        return super.onStartCommand(intent, flags, startId)
    }

    // 点击启动该卡片
    override fun onClick() {
        super.onClick()
        Log.d("qaction.NewRecordTileService", "onClicked() called at ${System.currentTimeMillis()}")

        val recCardIntent = Intent(this, PopupActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    .setAction(Intent.ACTION_MAIN)

        // -TODO: fix: take too long!
        // MainActivity is also slow to start; Nothing to do with my start-up
        // Slow after pressing HOME from any app(even not from mine!)
        startActivityAndCollapse(recCardIntent)
    }
}