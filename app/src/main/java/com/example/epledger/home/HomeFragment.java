package com.example.epledger.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.epledger.R;
import com.example.epledger.detail.DetailRecord;
import com.example.epledger.detail.RecordDetailFragment;
import com.example.epledger.model.Section;
import com.example.epledger.model.SectionLab;
import com.example.epledger.nav.NavigationFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
        View view = inflater.inflate(R.layout.page_home, container, false);
        setHasOptionsMenu(true);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        // 点击FloatingActionButton的功能
        FloatingActionButton btn = view.findViewById(R.id.addEntryButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordDetailFragment frag = new RecordDetailFragment();
                frag.bindRecord(new DetailRecord());
                NavigationFragment.Companion.pushToStack(
                        HomeFragment.this.requireActivity().getSupportFragmentManager(),
                        frag, true);
            }
        });

        updateUI();
    }

    private void updateUI() {
        SectionLab sectionLab = SectionLab.get(getContext());
        List<Section> sections = sectionLab.getSections();
        mSectionAdapter = new SectionAdapter(sections);
        mRecyclerView.setAdapter(mSectionAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.top_app_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
