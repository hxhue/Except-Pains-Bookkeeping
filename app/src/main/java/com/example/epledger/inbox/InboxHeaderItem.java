package com.example.epledger.inbox;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.epledger.R;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.xwray.groupie.Section;

public class InboxHeaderItem extends Item {
    private String name;

    public InboxHeaderItem(String name) {
        this.name = name;
    }

    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
        TextView textView = viewHolder.itemView.findViewById(R.id.inbox_header_label);
        ImageView imageView = viewHolder.itemView.findViewById(R.id.inbox_header_icon);
        textView.setText(name);
        switch (name) {
            case "Starred":  // TODO
                imageView.setImageResource(R.drawable.ic_baseline_star);
                break;
            case "Incomplete":  // TODO
                imageView.setImageResource(R.drawable.ic_baseline_not_interested);
                break;
            case "Screenshot Included":  // TODO
                imageView.setImageResource(R.drawable.ic_baseline_photo);
                break;
            case "Events":  // TODO
                imageView.setImageResource(R.drawable.ic_baseline_close);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_baseline_delete);
                break;
        }
    }

    @Override
    public int getLayout() {
        return R.layout.inbox_header_item;
    }
}
