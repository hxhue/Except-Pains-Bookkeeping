package com.example.epledger.util.tag;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.core.util.Pair;

import com.example.epledger.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class TagGroup {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ChipGroup mChipGroup;
    private List<BaseTag> mTagList;

    private void addChip(BaseTag tag) {
        Chip chip = (Chip) mLayoutInflater.inflate(R.layout.row_tag_view, mChipGroup, false);
        chip.setText(tag.getName());
        chip.setCheckable(true);
        chip.setChecked(tag.isChecked());
        chip.setId(tag.getId());
        mChipGroup.addView(chip);
    }

    public TagGroup(Context context, ChipGroup chipGroup, List<BaseTag> baseTagList) {
        mContext = context;
        mChipGroup = chipGroup;
        mLayoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        mTagList = baseTagList;
        for (BaseTag tag: mTagList) {
            addChip(tag);
        }
    }

    public void setTags(List<BaseTag> tags) {
        if (tags == null) {
            return;
        }
        mTagList.addAll(tags);
        for (BaseTag tag: mTagList) {
            addChip(tag);
        }
    }

    public List<Integer> getAllSelectedTags() {
        return mChipGroup.getCheckedChipIds();
    }

    public List<Integer> getAllUnSelectedTags() {
        List<Integer> unSelected = new ArrayList<>();
        for (int i = 0; i < mChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) mChipGroup.getChildAt(i);
            if (!chip.isChecked()) {
                unSelected.add(chip.getId());
            }
        }
        return unSelected;
    }

    // 得到所有的tags，first是id，second是是否选中
    public List<Pair<Integer, Boolean>> getAllTags() {
        List<Pair<Integer, Boolean>> tags = new ArrayList<>();
        for (int i = 0; i < mChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) mChipGroup.getChildAt(i);
            tags.add(new Pair<>(chip.getId(), chip.isChecked()));
        }
        return tags;
    }

    public void clearChecked() {
        mChipGroup.clearCheck();
    }
}
