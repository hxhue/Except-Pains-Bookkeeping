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
import com.example.epledger.model.EntryLab;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class HomeFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SectionAdapter mSectionAdapter;

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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        updateUI();
    }

    private void updateUI() {
        EntryLab entryLab = EntryLab.get(getContext());
        List<Entry> entries = entryLab.getEntries();
        TreeMap<Date, List<Entry>> entryMap = new TreeMap<>();
        for (Entry entry: entries) {
            if (!entryMap.containsKey(entry.getDate())) {
                entryMap.put(entry.getDate(), new ArrayList<>());
            }
            entryMap.get(entry.getDate()).add(entry);
        }
        mSectionAdapter = new SectionAdapter(entryMap);
        mRecyclerView.setAdapter(mSectionAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
