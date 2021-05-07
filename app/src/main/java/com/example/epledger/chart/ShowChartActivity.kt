package com.example.epledger.chart

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.epledger.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*

class ShowChartActivity : AppCompatActivity() {
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_chart)

        //pie chart
        val pieChart = findViewById<View>(R.id.pieChart) as PieChart
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
            ), this.applicationContext
        )
        val data = PieData(set)
        pieChart.data = data
        pieChart.centerText = "This is a center text!"
        pieChart.centerTextRadiusPercent = 0.8f
        pieChart.invalidate() // refresh
        val lineChart = findViewById<View>(R.id.lineChart) as LineChart
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


        //add date picker
        val dateRangePicker=
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates")
                .setSelection(
                    androidx.core.util.Pair<Long,Long>(
                        MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                    )
                )
                .build()



        //app bar
        val topAppBar=findViewById<View>(R.id.topAppBar) as MaterialToolbar
        topAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sift -> {
                    // Handle favorite icon press
                    true
                }
                R.id.reset -> {
                    // Handle search icon press
                    true
                }
                R.id.more -> {
                    // Handle more item (inside overflow menu) press
                    true
                }
                else -> false
            }
        }



    }
}