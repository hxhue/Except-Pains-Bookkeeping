package com.example.epledger.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epledger.R;
import com.example.epledger.model.Entry;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private EntryAdapter mEntryAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.page_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mEntryAdapter = new EntryAdapter(initData());
        mEntryAdapter.setOnItemClickListener(new EntryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(view.getContext(), "click " + position + " item", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(view.getContext(), "long click " + position + " item", Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(mEntryAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private List<Entry> initData() {
        List<Entry> datas = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            datas.add(new Entry(i, i * 4.3, "food", "eat", "支付宝"));
        }
        return datas;
    }
}
