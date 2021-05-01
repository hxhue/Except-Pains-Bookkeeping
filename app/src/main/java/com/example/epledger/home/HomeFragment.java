package com.example.epledger.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epledger.R;
import com.example.epledger.model.Section;
import com.example.epledger.model.SectionLab;

import java.util.List;

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
        SectionLab sectionLab = SectionLab.get(getContext());
        List<Section> sections = sectionLab.getSections();
        mSectionAdapter = new SectionAdapter(sections);
        mRecyclerView.setAdapter(mSectionAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
