package com.example.epledger.inbox.event.item

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class SpaceItemDecoration(private val space: Int, private val spanCount: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        val pos = parent.getChildLayoutPosition(view)
        outRect.left =  // (pos % spanCount == 0) ? space * 2 :
            space
        outRect.right =  // ((pos + 1) % spanCount == 0) ? space * 2 :
            space
        outRect.bottom = space
        outRect.top = if (pos < spanCount) 0 else space
    }

}