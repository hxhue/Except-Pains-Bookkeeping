package com.example.epledger.model

import android.content.Context
import com.example.epledger.R

const val CATEGORY_SUGGESTED_MAX_SIZE = 36

data class Category(
    var name: String = "",
    var iconResID: Int = R.drawable.ic_far_bookmark,
    var ID: Int? = null
) {
    fun copyAllExceptID(from: Category) {
        this.name = from.name
        this.iconResID = from.iconResID
    }

    companion object {
        const val UNKNOWN_ID = 1
        const val IMPORTED_ID = 11

        fun getDefaultCategories(context: Context): MutableList<Category> {
            return arrayListOf(
                Category(context.getString(R.string.default_category), R.drawable.ic_far_bookmark, UNKNOWN_ID),
                Category(context.getString(R.string.emergency), R.drawable.ic_fas_asterisk, 2),
                Category(context.getString(R.string.study), R.drawable.ic_fas_pencil_alt, 3),
                Category(context.getString(R.string.food), R.drawable.ic_fas_utensils, 4),
                Category(context.getString(R.string.shopping), R.drawable.ic_fas_shopping_cart, 5),
                Category(context.getString(R.string.transportation), R.drawable.ic_fas_bus, 6),
                Category(context.getString(R.string.digital), R.drawable.ic_fas_mobile_alt, 7),
                Category(context.getString(R.string.coffee), R.drawable.ic_fas_coffee, 8),
                Category(context.getString(R.string.present), R.drawable.ic_fas_gift, 9),
                Category(context.getString(R.string.sports), R.drawable.ic_far_futbol, 10),
                Category(context.getString(R.string.category_imported), R.drawable.u_account_balance_wallet, IMPORTED_ID)
            )
        }
    }
}