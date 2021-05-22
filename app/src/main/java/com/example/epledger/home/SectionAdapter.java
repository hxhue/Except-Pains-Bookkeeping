package com.example.epledger.home;

import android.content.DialogInterface;
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
import com.example.epledger.model.Record;
import com.example.epledger.model.Section;
import com.example.epledger.util.Fmt;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textDate;
        private RecyclerView rv;
        private DateFormat ft;
        private DatabaseModel dbModel;

        public ViewHolder(@NonNull View itemView, DatabaseModel dbModel) {
            super(itemView);
            this.dbModel = dbModel;

            textDate = itemView.findViewById(R.id.textDate);
            rv = itemView.findViewById(R.id.section_rv);
            rv.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rv.setItemAnimator(new DefaultItemAnimator());

            ft = Fmt.INSTANCE.getDate();
        }

        public void bind(Date date, List<Record> entryList, final int sectionPosition) {
            textDate.setText(ft.format(date));

            EntryAdapter entryAdapter = new EntryAdapter(entryList, dbModel);
            entryAdapter.setOnItemClickListener(new EntryAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Toast.makeText(view.getContext(), "click " + sectionPosition + " section " + position + " item", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    new MaterialAlertDialogBuilder(view.getContext())
                            .setMessage(R.string.del_item_confirm)
                            .setNegativeButton(R.string.no, (dialog, which) -> {})
                            .setPositiveButton(R.string.ok, (dialog, which) -> dbModel.deleteRecord(sectionPosition, position, SectionAdapter.this))
                    .show();
                }
            });
            rv.setAdapter(entryAdapter);
        }
    }

    private List<Section> mSections;
    private final DatabaseModel dbModel;

    public SectionAdapter(List<Section> mSections, DatabaseModel dbModel) {
        this.mSections = mSections;
        this.dbModel = dbModel;
    }

    public void setSections(List<Section> mSections) {
        this.mSections = mSections;
    }

    public List<Section> getSections() {
        return mSections;
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
