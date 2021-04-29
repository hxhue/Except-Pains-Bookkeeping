package com.example.epledger.home;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.example.epledger.R;
import com.example.epledger.model.Entry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textDate;
        private RecyclerView rv;
        private SimpleDateFormat ft;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textDate = itemView.findViewById(R.id.textDate);
            rv = itemView.findViewById(R.id.section_rv);
            rv.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rv.setItemAnimator(new DefaultItemAnimator());

            ft = new SimpleDateFormat ("yyyy-MM-dd");
        }

        public void bind(Date date, List<Entry> entryList) {
            textDate.setText(ft.format(date));

            EntryAdapter entryAdapter = new EntryAdapter(entryList);
            entryAdapter.setOnItemClickListener(new EntryAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Toast.makeText(view.getContext(), "click " + position + " item", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    Toast.makeText(view.getContext(), "long click " + position + " item", Toast.LENGTH_SHORT).show();
                }
            });
            rv.setAdapter(entryAdapter);
        }
    }

    private TreeMap<Date, List<Entry>> mEntries;

    public SectionAdapter(TreeMap<Date, List<Entry>> entryList) {
        this.mEntries = entryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.section_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date date = null;
        List<Entry> entries = null;
        int i = 0;
        for (Date o: mEntries.keySet()) {
            if (i == position) {
                date = o;
                entries = mEntries.get(o);
            }
            i++;
        }
        holder.bind(date, entries);
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }
}
