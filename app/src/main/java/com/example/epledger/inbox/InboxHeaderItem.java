package com.example.epledger.inbox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.epledger.R;
import com.example.epledger.model.Entry;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.xwray.groupie.Section;

import java.util.List;

public class InboxHeaderItem extends Item {
    private InboxHeaderTitle headerTitle;
    private List<Entry> list;

    enum InboxHeaderTitle {
        STARRED, INCOMPLETE, SCREENSHOTS, EVENTS
    }

    public InboxHeaderItem(InboxHeaderTitle title, List<Entry> list) {
        this.list = list;
        this.headerTitle = title;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
        Context context = viewHolder.itemView.getContext();
        TextView textView = viewHolder.itemView.findViewById(R.id.inbox_header_label);
        ImageView imageView = viewHolder.itemView.findViewById(R.id.inbox_header_icon);
        final String format = "%s (%d)";
        switch (headerTitle) {
            case STARRED:
                imageView.setImageResource(R.drawable.ic_baseline_star);
                textView.setText(String.format(format, context.getString(R.string.starred), list.size()));
                break;
            case INCOMPLETE:
                imageView.setImageResource(R.drawable.ic_baseline_not_interested);
                textView.setText(String.format(format, context.getString(R.string.incomplete), list.size()));
                break;
            case SCREENSHOTS:
                imageView.setImageResource(R.drawable.ic_baseline_photo);
                textView.setText(String.format(format, context.getString(R.string.screenshots), list.size()));
                break;
            case EVENTS:
                imageView.setImageResource(R.drawable.ic_baseline_close);
                textView.setText(String.format(format, context.getString(R.string.events), list.size()));
                break;
            default:
                throw new RuntimeException("Unexpected header title category.");
        }
    }

    @Override
    public int getLayout() {
        return R.layout.inbox_header_item;
    }
}
