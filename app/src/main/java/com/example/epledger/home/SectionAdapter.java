package com.example.epledger.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.epledger.R;
import com.example.epledger.db.DatabaseModel;
import com.example.epledger.detail.RecordDetailFragment;
import com.example.epledger.model.Record;
import com.example.epledger.model.RecordGroup;
import com.example.epledger.nav.NavigationFragment;
import com.example.epledger.util.Fmt;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.jetbrains.annotations.NotNull;
import static com.example.epledger.util.ViewsKt.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textDate;
        private RecyclerView innerRecyclerView;
        private DateFormat ft;
        private DatabaseModel dbModel;

        public ViewHolder(@NonNull View itemView, DatabaseModel dbModel) {
            super(itemView);
            this.dbModel = dbModel;

            textDate = itemView.findViewById(R.id.textDate);
            innerRecyclerView = itemView.findViewById(R.id.section_rv);
            innerRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            innerRecyclerView.setItemAnimator(new DefaultItemAnimator());

            ft = Fmt.INSTANCE.getDate();
        }

        public void bind(Date date, List<Record> records, final int sectionPosition) {
            textDate.setText(ft.format(date));

            EntryAdapter entryAdapter = new EntryAdapter(records, dbModel);
            entryAdapter.setOnItemClickListener(new EntryAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    // Set up fragment
                    RecordDetailFragment fragment = new RecordDetailFragment();
                    fragment.bindRecord(records.get(position));
                    fragment.setDetailRecordMsgReceiver(new RecordDetailFragment.DetailRecordMsgReceiver() {
                        @Override
                        public void onDetailRecordSubmit(@NotNull Record record) {
                            dbModel.updateRecord(sectionPosition, position, SectionAdapter.this, null);
                        }

                        @Override
                        public void onDetailRecordDelete(@NotNull Record record) {
                            dbModel.deleteRecord(sectionPosition, position, SectionAdapter.this);
                        }
                    });
                    // Open fragment
                    NavigationFragment.Companion.pushToStack(
                            ((AppCompatActivity) Objects.requireNonNull(getActivity(view))).getSupportFragmentManager(),
                            fragment, true, null
                    );
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
            innerRecyclerView.setAdapter(entryAdapter);
        }
    }

    private List<RecordGroup> mRecordGroups;
    private final DatabaseModel dbModel;

    public SectionAdapter(List<RecordGroup> mRecordGroups, DatabaseModel dbModel) {
        this.mRecordGroups = mRecordGroups;
        this.dbModel = dbModel;
    }

    public void setSections(List<RecordGroup> mRecordGroups) {
        this.mRecordGroups = mRecordGroups;
    }

    public List<RecordGroup> getSections() {
        return mRecordGroups;
    }

    /**
     * 补充方法。用来实现内部二级RecyclerView中一个项目的改变通知。
     * @param section 在主列表中的位置
     * @param position 在二级列表中的位置
     */
    public void notifySingleItemChanged(int section, int position) {
        if (recyclerView == null) {
            throw new IllegalStateException();
        }
        SectionAdapter.ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(section);
        if (viewHolder != null) {
            EntryAdapter adapter = (EntryAdapter) Objects.requireNonNull(viewHolder.innerRecyclerView.getAdapter());
            adapter.notifyItemChanged(position);
        }
    }

    /**
     * 补充方法。用来实现内部二级RecyclerView中一个项目的删除通知。
     * 注意：当二级列表仅有一个元素时不得调用此函数，因为此函数不会进行异常检查。
     * @param section 在主列表中的位置
     * @param position 在二级列表中的位置
     */
    public void notifySingleItemRemoved(int section, int position, int sectionSizeAfterRemoval) {
        if (recyclerView == null) {
            throw new IllegalStateException();
        }
        SectionAdapter.ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(section);
        if (viewHolder != null) {
            EntryAdapter adapter = (EntryAdapter) Objects.requireNonNull(viewHolder.innerRecyclerView.getAdapter());
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, sectionSizeAfterRemoval);
        }
        // viewHolder为空时没有必要在视图上删除，因为还没有创建这个视图
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_item, parent, false);

        // 设置嵌套在内部的recyclerView以防止奇怪的动画出现
        // https://stackoverflow.com/a/45579654/13785815
        RecyclerView innerRecyclerView = view.findViewById(R.id.section_rv);
        innerRecyclerView.setHasFixedSize(true);

        return new ViewHolder(view, dbModel);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecordGroup recordGroup = mRecordGroups.get(position);
        holder.bind(recordGroup.getDate(), recordGroup.getRecords(), position);
//        holder.innerRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mRecordGroups.size();
    }

    private RecyclerView recyclerView = null;

    @Override
    public void onAttachedToRecyclerView(@NonNull @NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull @NotNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

}
