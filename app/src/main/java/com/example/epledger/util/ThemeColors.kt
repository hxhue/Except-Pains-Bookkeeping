package com.example.epledger.util

import android.R

import android.util.TypedValue




object ThemeColors {
    fun getColorPrimary(theme: android.content.res.Resources.Theme): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        return typedValue.data
    }

    fun getColorSecondary(theme: android.content.res.Resources.Theme): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorSecondary, typedValue, true)
        return typedValue.data
    }

    fun getDarkColorPrimary(theme: android.content.res.Resources.Theme): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true)
        return typedValue.data
    }

    fun getColorAccent(theme: android.content.res.Resources.Theme): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
        return typedValue.data
    }
}