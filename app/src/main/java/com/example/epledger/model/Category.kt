package com.example.epledger.model

import com.example.epledger.R

const val CATEGORY_MAX_SIZE = 36

data class Category(var name: String = "",
                    var iconResID: Int = R.drawable.ic_far_bookmark,
                    var ID: Int? = null
) {}