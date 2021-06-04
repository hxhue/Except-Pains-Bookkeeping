package com.example.epledger.filter;

import androidx.lifecycle.ViewModel;

import com.example.epledger.model.Source;
import com.example.epledger.util.tag.BaseTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FilterSourcesViewModel extends ViewModel {
    private HashMap<Integer, BaseTag> baseTagHashMap;

    public FilterSourcesViewModel() {
        this.baseTagHashMap = new HashMap<>();
    }

    public void addBaseTag(String name, int id) {
        if (!baseTagHashMap.containsKey(id)) {
            baseTagHashMap.put(id, new BaseTag(id, name, true));
        }
    }

    public void deleteBaseTag(int id) {
        baseTagHashMap.remove(id);
    }

    public void update(List<Source> sourceList) {
        HashMap<Integer, Boolean> isExist = new HashMap<>();
        for (Integer key: baseTagHashMap.keySet()) {
            isExist.put(key, false);
        }
        for (Source category: sourceList) {
            isExist.replace(category.getID(), true);
            addBaseTag(category.getName(), category.getID());
        }
        for (Integer id: isExist.keySet()) {
            if (!isExist.get(id)) {
                deleteBaseTag(id);
            }
        }
    }

    public List<BaseTag> getBaseTagList() {
        List<BaseTag> baseTagList = new ArrayList<>();
        for (Integer key: baseTagHashMap.keySet()) {
            baseTagList.add(baseTagHashMap.get(key));
        }
        return baseTagList;
    }

    public void reset() {
        for (Integer key: baseTagHashMap.keySet()) {
            baseTagHashMap.get(key).setChecked(true);
        }
    }

    // 设置id的tag是否被选中
    public void setChecked(int id, boolean checked) {
        baseTagHashMap.get(id).setChecked(checked);
    }

    public void select(int id) {
        baseTagHashMap.get(id).setChecked(true);
    }

    public void unSelect(int id) {
        baseTagHashMap.get(id).setChecked(false);
    }
}
