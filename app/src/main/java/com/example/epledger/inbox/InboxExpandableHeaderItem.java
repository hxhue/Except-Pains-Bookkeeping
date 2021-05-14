package com.example.epledger.inbox;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.example.epledger.R;
import com.xwray.groupie.ExpandableGroup;
import com.xwray.groupie.ExpandableItem;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

public class InboxExpandableHeaderItem extends InboxHeaderItem implements ExpandableItem {
    private String name;
    private ExpandableGroup expandableGroup;
    private View.OnClickListener clickListener;

    public InboxExpandableHeaderItem(String name) {
        super(name);
        clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableGroup.onToggleExpanded();
            }
        };
    }

    @Override
    public void setExpandableGroup(@NonNull ExpandableGroup onToggleListener) {
        expandableGroup = onToggleListener;
    }

    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
        super.bind(viewHolder, position);
        viewHolder.itemView.setOnClickListener(clickListener);
    }
}
