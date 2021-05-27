package com.example.epledger.util

import java.text.DateFormat
import java.util.*

object Fmt {
    val date: DateFormat
        get() = DateFormat.getDateInstance(DateFormat.LONG)
}