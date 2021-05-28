package com.example.epledger.model.tag;

import android.content.Context;
import android.view.LayoutInflater;

import com.example.epledger.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.List;

public class TagGroup {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ChipGroup mChipGroup;
    public static List<BaseTag> sTagList;

    private void initTagList() {
        List<String> tagString = Arrays.asList("餐饮", "教育", "交通", "娱乐", "书籍", "服装", "数码");
        for (String tag: tagString) {
            sTagList.add(new BaseTag(tag));
        }
    }

    private void addChip(BaseTag tag) {
        Chip chip = (Chip) mLayoutInflater.inflate(R.layout.row_tag_view, mChipGroup, false);
        chip.setText(tag.getName());
        mChipGroup.addView(chip);
    }

    public TagGroup(Context context, ChipGroup chipGroup) {
        mContext = context;
        mChipGroup = chipGroup;
        mLayoutInflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        if (sTagList == null) {
            initTagList();
        }
        for (BaseTag tag: sTagList) {
            addChip(tag);
        }
    }

    public void setTags(List<BaseTag> tags) {
        if (tags == null) {
            return;
        }
        sTagList.addAll(tags);
        for (BaseTag tag: sTagList) {
            addChip(tag);
        }
    }

    public List<Integer> getAllSelectedTags() {
        return mChipGroup.getCheckedChipIds();
    }
}
