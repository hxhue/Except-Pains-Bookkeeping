package com.example.epledger.qaction

import android.content.Intent
import android.service.quicksettings.TileService

class NewRecordTileService : TileService() {
    // 点击启动该卡片
    override fun onClick() {
        super.onClick()
        val recCardIntent = Intent(this, PopupActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                .setAction(Intent.ACTION_MAIN)
        startActivityAndCollapse(recCardIntent)
    }
}