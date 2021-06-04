package com.example.epledger.filter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.epledger.R;
import com.example.epledger.db.DatabaseModel;
import com.example.epledger.model.Category;
import com.example.epledger.model.Filter;
import com.example.epledger.model.Source;
import com.example.epledger.util.Fmt;
import com.example.epledger.util.tag.TagGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FilterDialogFragment extends DialogFragment {
    private DatabaseModel databaseModel;
    private FilterCategoriesViewModel filterCategoryViewModel;
    private FilterSourcesViewModel filterSourceViewModel;

    public interface FilterSubmitListener {
        public void onFilterSubmit(Filter filter);
    }

    private FilterSubmitListener mFilterSubmitListener;

    public void setFilterSubmitListener(FilterSubmitListener filterSubmitListener) {
        this.mFilterSubmitListener = filterSubmitListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogContent = inflater.inflate(R.layout.filter_info, null);
        dialogContent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        EditText filterDateTextLeft = dialogContent.findViewById(R.id.filter_date_text_left);
        EditText filterDateTextRight = dialogContent.findViewById(R.id.filter_date_text_right);
//        final Long[] filterDateMin = {null};
//        final Long[] filterDateMax = {null};

        filterDateTextLeft.setText(Fmt.INSTANCE.getDate().format(MaterialDatePicker.thisMonthInUtcMilliseconds()));
        filterDateTextRight.setText(Fmt.INSTANCE.getDate().format(MaterialDatePicker.todayInUtcMilliseconds()));

        MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setSelection(
                        new Pair<>(
                                MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                MaterialDatePicker.todayInUtcMilliseconds()
                        )
                )
                .build();
        dateRangePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                filterDateTextLeft.setText(Fmt.INSTANCE.getDate().format(selection.first));
//                filterDateMin[0] = selection.first;
                filterDateTextRight.setText(Fmt.INSTANCE.getDate().format(selection.second));
//                filterDateMax[0] = selection.second;
            }
        });

        Button filterDateButton = dialogContent.findViewById(R.id.filter_date_button);
        filterDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateRangePicker.show(getParentFragmentManager(), null);
            }
        });

        TagGroup sourceTagGroup = new TagGroup(context, dialogContent.findViewById(R.id.filter_src_chipgroup),
                filterSourceViewModel.getBaseTagList());
        TagGroup categoryTagGroup = new TagGroup(context, dialogContent.findViewById(R.id.filter_type_chipgroup),
                filterCategoryViewModel.getBaseTagList());

        EditText filterMoneyMin = dialogContent.findViewById(R.id.filter_money_min);
        EditText filterMoneyMax = dialogContent.findViewById(R.id.filter_money_max);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogContent)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            // 构建筛选条件Filter
                            Filter filter = new Filter(
                                    DateFormat.getDateInstance().parse(filterDateTextLeft.getText().toString()),
                                    addOneDay(DateFormat.getDateInstance().parse(filterDateTextRight.getText().toString())),
                                    Double.parseDouble(filterMoneyMin.getText().toString()),
                                    Double.parseDouble(filterMoneyMax.getText().toString()),
                                    categoryTagGroup.getAllSelectedTags(),
                                    sourceTagGroup.getAllSelectedTags()
                            );

                            // 点击确定之后保存所选的标签，下次打开这次没选择的标签不会被选择
                            List<Pair<Integer, Boolean>> categoryTags = categoryTagGroup.getAllTags();
                            for (Pair<Integer, Boolean> pair: categoryTags) {
                                filterCategoryViewModel.setChecked(pair.first, pair.second);
                            }
                            List<Pair<Integer, Boolean>> sourceTags = sourceTagGroup.getAllTags();
                            for (Pair<Integer, Boolean> pair: sourceTags) {
                                filterSourceViewModel.setChecked(pair.first, pair.second);
                            }

                            // 使用筛选信息进行筛选
                            mFilterSubmitListener.onFilterSubmit(filter);
                        } catch (ParseException | NumberFormatException e) {
                            Dialog errorDialog = new MaterialAlertDialogBuilder(requireContext())
                                    .setMessage(R.string.filter_incomplete_prompt)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .create();
                            errorDialog.show();
                        }

//                        Filter filter = new Filter(
//                                roundToDay(filterDateMin[0]),
//                                roundToDay(filterDateMax[0]),
//                                Double.parseDouble(filterMoneyMin.getText().toString()),
//                                Double.parseDouble(filterMoneyMax.getText().toString()),
//                                categoryTagGroup.getAllSelectedTags(),
//                                sourceTagGroup.getAllSelectedTags()
//                        );
//                        mFilterSubmitListener.onFilterSubmit(filter);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing
                        return;
                    }
                })
                .setTitle(R.string.filter_title)
                .create();

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelProvider viewModelProvider = new ViewModelProvider(requireActivity());
        databaseModel = viewModelProvider.get(DatabaseModel.class);
        filterCategoryViewModel = viewModelProvider.get(FilterCategoriesViewModel.class);
        filterSourceViewModel = viewModelProvider.get(FilterSourcesViewModel.class);
        // 新添加的标签都默认被选择
        List<Category> categoryList = databaseModel.requireCategories();
        updateCategoryViewModel(categoryList);
        List<Source> sourceList = databaseModel.requireSources();
        updateSourceViewModel(sourceList);
    }

    private void updateCategoryViewModel(List<Category> categoryList) {
        filterCategoryViewModel.update(categoryList);
    }

    private void updateSourceViewModel(List<Source> sourceList) {
        filterSourceViewModel.update(sourceList);
    }

    private Date addOneDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return new Date(cal.getTimeInMillis());
    }

    // 把日期保留到天
    // https://stackoverflow.com/a/7930591/13785815
    private Date roundToDay(Long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        // 清除较小位置的时间
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Date(cal.getTimeInMillis());
    }
}
