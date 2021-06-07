package com.example.epledger.model

import android.content.Context
import com.example.epledger.R

data class Source(
    var name: String,
    var ID: Int? = null
) {
    fun copyAllExceptID(from: Source) {
        this.name = from.name
    }

    companion object {
        const val UNKNOWN_ID = 1

        fun getDefaultSources(context: Context): MutableList<Source> {
            return arrayListOf(
                Source(context.getString(R.string.default_source), UNKNOWN_ID),
                Source(context.getString(R.string.alipay), 2),
                Source(context.getString(R.string.wechat), 3),
                Source(context.getString(R.string.cash), 4)
            )
        }
    }
}