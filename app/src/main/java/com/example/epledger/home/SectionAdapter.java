package com.example.epledger.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epledger.R;
import com.example.epledger.db.DatabaseModel;
import com.example.epledger.model.Entry;
import com.example.epledger.model.Section;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textDate;
        private RecyclerView rv;
        private SimpleDateFormat ft;
        private DatabaseModel dbModel;

        public ViewHolder(@NonNull View itemView, DatabaseModel dbModel) {
            super(itemView);
            this.dbModel = dbModel;

            textDate = itemView.findViewById(R.id.textDate);
            rv = itemView.findViewById(R.id.section_rv);
            rv.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rv.setItemAnimator(new DefaultItemAnimator());

            ft = new SimpleDateFormat ("yyyy-MM-dd");
        }

        public void bind(Date date, List<Entry> entryList, final int sectionPosition) {
            textDate.setText(ft.format(date));

            EntryAdapter entryAdapter = new EntryAdapter(entryList, dbModel);
            entryAdapter.setOnItemClickListener(new EntryAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Toast.makeText(view.getContext(), "click " + sectionPosition + " section " + position + " item", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    Toast.makeText(view.getContext(), "long click " + sectionPosition + " section " + position + " item", Toast.LENGTH_SHORT).show();
                }
            });
            rv.setAdapter(entryAdapter);
        }
    }

    private List<Section> mSections;
    private DatabaseModel dbModel;

    public SectionAdapter(List<Section> mSections, DatabaseModel dbModel) {
        this.mSections = mSections;
        this.dbModel = dbModel;
    }

    public void setSections(List<Section> mSections) {
        this.mSections = mSections;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.section_item, parent, false), dbModel);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Section section = mSections.get(position);
        holder.bind(section.getDate(), section.getEntryList(), position);
    }

    @Override
    public int getItemCount() {
        return mSections.size();
    }
}
