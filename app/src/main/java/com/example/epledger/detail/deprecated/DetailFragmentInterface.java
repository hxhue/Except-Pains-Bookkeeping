package com.example.epledger.detail.deprecated;

public interface DetailFragmentInterface {
    /**
     * 根据数据更新UI。
     */
    void updateUI();

    /**
     * 将控件中的所有数据记录到record当中。
     * 这个场景下，record两者都能够取到，因此没有必要使用返回值。
     */
    void prepareRecordToCommit();
}
