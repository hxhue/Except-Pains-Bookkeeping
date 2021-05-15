package com.example.epledger.inbox;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.epledger.R;
import com.example.epledger.model.Entry;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

public class InboxEntryItem extends Item {
    private Entry mEntry;

    public InboxEntryItem(Entry mEntry) {
        this.mEntry = mEntry;
    }

    @Override
    public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
        ImageView imageView = viewHolder.itemView.findViewById(R.id.label_image);
        TextView labelText = viewHolder.itemView.findViewById(R.id.label_text);
        TextView amountText = viewHolder.itemView.findViewById(R.id.amount);
        TextView infoText = viewHolder.itemView.findViewById(R.id.info);
        TextView paySourceText = viewHolder.itemView.findViewById(R.id.pay_source);

        imageView.setImageResource(R.drawable.ic_baseline_close);
        labelText.setText(mEntry.getLabel());
        if (mEntry.getAmount() >= 0) {
            amountText.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.amount_income_color));
            amountText.setText("+￥" + String.format("%.2f", Math.abs(mEntry.getAmount())));
        } else  {
            amountText.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.amount_expand_color));
            amountText.setText("-￥" + String.format("%.2f", Math.abs(mEntry.getAmount())));
        }
        infoText.setText(mEntry.getInfo());
        paySourceText.setText(mEntry.getSource());
    }

    @Override
    public int getLayout() {
        return R.layout.entry_item;
    }
}
