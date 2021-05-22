package com.example.epledger.inbox.content;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.epledger.R;
import com.example.epledger.model.Record;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

public class InboxEntryItem extends Item {
    private Record mEntry;

    public InboxEntryItem(Record mEntry) {
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
        labelText.setText(mEntry.getCategory());
        if (mEntry.getMoneyAmount() >= 0) {
            amountText.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.amount_income_color));
            amountText.setText("+￥" + String.format("%.2f", Math.abs(mEntry.getMoneyAmount())));
        } else  {
            amountText.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.amount_expand_color));
            amountText.setText("-￥" + String.format("%.2f", Math.abs(mEntry.getMoneyAmount())));
        }
        infoText.setText(mEntry.getNote());
        paySourceText.setText(mEntry.getSource());
    }

    @Override
    public int getLayout() {
        return R.layout.entry_item;
    }
}
