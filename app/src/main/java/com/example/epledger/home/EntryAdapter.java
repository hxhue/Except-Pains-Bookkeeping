package com.example.epledger.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epledger.R;
import com.example.epledger.db.DatabaseModel;
import com.example.epledger.model.Record;
import com.example.epledger.model.Category;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.ViewHolder> {
    private DatabaseModel dbModel;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView labelImage;
        private final TextView labelText;
        private final TextView amountText;
        private final TextView infoText;
        private final TextView categoryText;

        private Record mEntry;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            labelImage = itemView.findViewById(R.id.label_image);
            labelText = itemView.findViewById(R.id.label_text);
            amountText = itemView.findViewById(R.id.amount);
            infoText = itemView.findViewById(R.id.info);
            categoryText = itemView.findViewById(R.id.pay_source);
        }

        public void bind(Record entry) {
            mEntry = entry;
            // Get a context
            Context ctx = itemView.getContext();
            // Set image (**Testing**)
//            int choice = Random.Default.nextInt();
//            if (choice % 3 == 0)
//                labelImage.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.u_sports_basketball));
//            else if (choice % 3 == 1)
//                labelImage.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_fas_car));
//            else
//                labelImage.setImageDrawable(null);
            labelImage.setImageDrawable(null);
            for (Category category: dbModel.requireCategories()) {
                if (category.getName().equals(entry.getCategory())) {
                    labelImage.setImageDrawable(ContextCompat.getDrawable(ctx, category.getIconResID()));
                    break;
                }
            }

            labelText.setText(mEntry.getCategory());
            if (mEntry.getMoneyAmount() >= 0) {
                amountText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.amount_income_color));
                amountText.setText("+￥" + String.format("%.2f", Math.abs(mEntry.getMoneyAmount())));
            } else  {
                amountText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.amount_expand_color));
                amountText.setText("-￥" + String.format("%.2f", Math.abs(mEntry.getMoneyAmount())));
            }

            // Set info
            final String infoStr = mEntry.getNote();
            // Check if it's empty
            if (infoStr != null && !infoStr.trim().isEmpty()) {
                infoText.setText(mEntry.getNote());
            } else {
                infoText.setText(R.string.no_info_prompt);
            }

            categoryText.setText(mEntry.getSource());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    private List<Record> mEntryList;
    // 事件回调监听
    private EntryAdapter.OnItemClickListener onItemClickListener;

    public EntryAdapter(List<Record> entryList, DatabaseModel model) {
        this.mEntryList = entryList;
        dbModel = model;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Record entry = mEntryList.get(position);
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
