package com.example.epledger.chart

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.epledger.R
import com.example.epledger.db.ImportDataFromExcel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


class ChartsFragment: Fragment() {
    private lateinit var siftLayout:RelativeLayout
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    var dateRangePicker=
            MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .setSelection(
                            androidx.core.util.Pair<Long, Long>(
                                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                    MaterialDatePicker.todayInUtcMilliseconds()
                            )
                    )
                    .build()
    val im = ImportDataFromExcel()
    @SuppressLint("ResourceAsColor")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_show_chart, container, false)
        val expenseTypeChipGroup=view.findViewById<View>(R.id.expenseTypeChipGroup) as ChipGroup
        setHasOptionsMenu(true) // Turn on option menu
        siftLayout=view.findViewById<View>(R.id.siftLayout) as RelativeLayout

        //pie chart
        val pieChart = view.findViewById<View>(R.id.pieChart) as PieChart
        //准备数据
        val entries: MutableList<PieEntry> = ArrayList()
        entries.add(PieEntry(18.5f, "Green"))
        entries.add(PieEntry(26.7f, "Yellow"))
        entries.add(PieEntry(24.0f, "Red"))
        entries.add(PieEntry(30.8f, "Blue"))
        val set = PieDataSet(entries, "Election Results")
        set.setColors(
                intArrayOf(
                        R.color.purple_200,
                        R.color.purple_500,
                        R.color.purple_700,
                        R.color.teal_200
                ), context
        )
        val data = PieData(set)
        pieChart.data = data
        pieChart.centerText = "This is a center text!"
        pieChart.centerTextRadiusPercent = 0.8f
        pieChart.invalidate() // refresh
        val lineChart = view.findViewById<View>(R.id.lineChart) as LineChart
        val valsComp1: MutableList<Entry> =
                ArrayList()
        val valsComp2: MutableList<Entry> =
                ArrayList()
        val c1e1 =
                Entry(0f, 100000f)
        valsComp1.add(c1e1)
        val c1e2 =
                Entry(1f, 140000f) // 1 == quarter 2 ...
        valsComp1.add(c1e2)
        val c1e3 =
                Entry(2f, 120000f) // 1 == quarter 2 ...
        valsComp1.add(c1e3)
        val c1e4 =
                Entry(3f, 180000f) // 1 == quarter 2 ...
        valsComp1.add(c1e4)
        val c2e1 =
                Entry(0f, 130000f) // 0 == quarter 1
        valsComp2.add(c2e1)
        val c2e2 =
                Entry(1f, 115000f) // 1 == quarter 2 ...
        valsComp2.add(c2e2)
        val c2e3 =
                Entry(2f, 60000f) // 0 == quarter 1
        valsComp2.add(c2e3)
        val c2e4 =
                Entry(3f, 170000f) // 1 == quarter 2 ...
        valsComp2.add(c2e4)
        val setComp1 = LineDataSet(valsComp1, "Company 1")
        setComp1.axisDependency = YAxis.AxisDependency.LEFT
        setComp1.color = R.color.teal_700
        val setComp2 = LineDataSet(valsComp2, "Company 2")
        setComp2.axisDependency = YAxis.AxisDependency.RIGHT
        setComp2.color = R.color.purple_500
        val lineDataSets: MutableList<ILineDataSet> =
                ArrayList()
        lineDataSets.add(setComp1)
        lineDataSets.add(setComp2)
        val lineData = LineData(lineDataSets)
        lineChart.data = lineData
        val quarters = arrayOf("Q1", "Q2", "Q3", "Q4")
        val valueFormatter: ValueFormatter =
                object : ValueFormatter() {
                    override fun getAxisLabel(
                            value: Float,
                            axis: AxisBase
                    ): String {
                        return quarters[value.toInt()]
                    }
                }
        val xAxis = lineChart.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = valueFormatter
        val lineChartDescription =
                Description()
        lineChartDescription.text = "this is a lineChart description!"
        lineChartDescription.textAlign = Paint.Align.CENTER
        lineChartDescription.textColor = R.color.purple_700
        lineChart.description = lineChartDescription
        lineChart.setNoDataText("No data available!")
        lineChart.setDrawGridBackground(false)
        lineChart.setDrawBorders(true)
        lineChart.setBorderColor(R.color.design_default_color_error)
        lineChart.setBackgroundColor(R.color.design_default_color_on_primary) //?
        lineChart.setBorderWidth(5f)
        lineChart.invalidate()






        //app bar
        val topAppBar=view.findViewById<View>(R.id.topAppBar) as MaterialToolbar
        topAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sift -> {
                    siftLayout.animate()
                            .translationY(800f)
                            .setDuration(300)
                            .start()
                    pieChart.animate()
                            .translationY(800f)
                            .setDuration(300)
                            .start()
                    lineChart.animate()
                            .translationY(800f)
                            .setDuration(300)
                            .start()
                    true
                }
                R.id.reset -> {
                    siftLayout.animate()
                            .translationY(0f)
                            .setDuration(300)
                            .start()
                    pieChart.animate()
                            .translationY(0f)
                            .setDuration(300)
                            .start()
                    lineChart.animate()
                            .translationY(0f)
                            .setDuration(300)
                            .start()
                    true
                }
                R.id.more -> {
                    // Handle more item (inside overflow menu) press
                    true
                }
                else -> false
            }
        }


        //add date picker


//        dateRangePicker.addOnPositiveButtonClickListener{
//
//        }
        //设置按钮回调函数
        val setDateRangeBtn= view.findViewById<View>(R.id.setDateRangeButton) as Button
        setDateRangeBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                activity?.supportFragmentManager?.let { dateRangePicker.show(it, "DATE_RANGE_PICKER") }
            }
        })

        val clearAllBtn=view.findViewById<View>(R.id.clearAllBtn) as Button
        clearAllBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                expenseTypeChipGroup.clearCheck()
                Toast.makeText(context,"clear all tags",Toast.LENGTH_SHORT).show()
            }
        })

        val siftBtn=view.findViewById<View>(R.id.siftBtn) as Button
        siftBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                siftItems()
            }
        })


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onResume() {
        super.onResume()
        Toast.makeText(context, "Hello World", Toast.LENGTH_SHORT)
    }

    fun siftItems(){
        val dateRange=dateRangePicker.selection
        val dateStart= dateRange?.first?.let { UTC2Str(it) }
        val dateEnd=dateRange?.second?.let { UTC2Str(it) }
        if (dateStart != null&&dateEnd!=null) {
            Toast.makeText(context,dateStart+"   "+dateEnd,Toast.LENGTH_SHORT).show()
        }


//        im.find_date_from()
//        val startDate=
//        im.find_date_from()
    }


    fun UTC2Str(utc:Long): String {
        val sdf=SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date=Date(utc)
        val str=sdf.format(date)
        return str
    }






}