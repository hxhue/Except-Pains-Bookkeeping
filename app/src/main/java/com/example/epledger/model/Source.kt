package com.example.epledger.model

import android.content.Context
import com.example.epledger.R

data class Source(
    var name: String,
    var ID: Int? = null
) {
    companion object {
        fun getDefaultSources(context: Context): MutableList<Source> {
            return arrayListOf(
                Source(context.getString(R.string.alipay), 1),
                Source(context.getString(R.string.wechat), 2),
                Source(context.getString(R.string.cash), 3)
            )
        }
    }
}