package com.example.epledger.inbox.content;

import android.view.View;

import androidx.annotation.NonNull;

import com.example.epledger.model.Record;
import com.example.epledger.model.RecordGroup;
import com.xwray.groupie.ExpandableGroup;
import com.xwray.groupie.ExpandableItem;
import com.xwray.groupie.GroupieViewHolder;

import java.util.List;

public class InboxExpandableHeaderItem extends InboxHeaderItem implements ExpandableItem {
    private ExpandableGroup expandableGroup;
    private View.OnClickListener clickListener;

    public InboxExpandableHeaderItem(RecordGroup.SectionType title, List<Record> list) {
        super(title, list);
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
