package com.example.epledger.home;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epledger.R;
import com.example.epledger.model.Entry;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView labelImage;
        private final TextView labelText;
        private final TextView amountText;
        private final TextView infoText;
        private final TextView sourceText;

        private Entry mEntry;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            labelImage = itemView.findViewById(R.id.label_image);
            labelText = itemView.findViewById(R.id.label_text);
            amountText = itemView.findViewById(R.id.amount);
            infoText = itemView.findViewById(R.id.info);
            sourceText = itemView.findViewById(R.id.pay_source);
        }

        public void bind(Entry entry) {
            mEntry = entry;
            
            labelText.setText(mEntry.getLabel());
            if (mEntry.getAmount() >= 0) {
                amountText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.amount_income_color));
                amountText.setText("+￥" + String.format("%.2f", Math.abs(mEntry.getAmount())));
            } else  {
                amountText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.amount_expand_color));
                amountText.setText("-￥" + String.format("%.2f", Math.abs(mEntry.getAmount())));
            }
            infoText.setText(mEntry.getInfo());
            sourceText.setText(mEntry.getSource());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    private List<Entry> mEntryList;
    // 事件回调监听
    private EntryAdapter.OnItemClickListener onItemClickListener;

    public EntryAdapter(List<Entry> entryList) {
        this.mEntryList = entryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entry entry = mEntryList.get(position);
        holder.bind(entry);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(onItemClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView, pos);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onItemClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, pos);
                }
                //表示此事件已经消费，不会触发单击事件
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEntryList.size();
    }

    public void setOnItemClickListener(EntryAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
