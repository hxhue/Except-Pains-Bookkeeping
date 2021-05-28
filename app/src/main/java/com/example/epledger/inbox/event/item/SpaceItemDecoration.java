package com.example.epledger.inbox.event.item;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    final private int space;
    final private int spanCount;

    public SpaceItemDecoration(int space, int spanCount) {
        this.space = space;
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        int pos = parent.getChildLayoutPosition(view);
        outRect.left = // (pos % spanCount == 0) ? space * 2 :
                space;
        outRect.right = // ((pos + 1) % spanCount == 0) ? space * 2 :
                space;
        outRect.bottom = space;
        outRect.top = (pos < spanCount) ? 0: space;
    }
}