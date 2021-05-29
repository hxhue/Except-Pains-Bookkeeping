package com.example.epledger.chart

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.util.Pair
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.play.core.internal.i
import kotlinx.android.synthetic.main.activity_show_chart.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import com.example.epledger.db.ImportDataFromExcel.bill
import com.example.epledger.db.MemoryDatabase
import kotlin.collections.HashMap

class ChartsFragment: Fragment() {
    private lateinit var siftLayout:RelativeLayout
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var accountChip1:Chip
    private lateinit var accountChip2:Chip
    private lateinit var accountChip3:Chip
    private lateinit var accountIdList:List<Int>
    private lateinit var expenseTypeIdList:List<Int>
    val expenseTypeChipList=ArrayList<Chip>()
    private lateinit var accountChipGroup:ChipGroup
    private lateinit var billList:List<bill>
    private lateinit var siftBtn:Button
    private lateinit var catNames:List<String>
    private lateinit var srcNames:List<String>
    val im = MemoryDatabase()
    var dateRangePicker=
            MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .setSelection(
                            Pair<Long, Long>(
                                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                    MaterialDatePicker.todayInUtcMilliseconds()
                            )
                    )
                    .build()



    @SuppressLint("ResourceAsColor")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_show_chart, container, false)
        siftBtn=view.findViewById(R.id.siftBtn)
        dateRangePicker.addOnPositiveButtonClickListener {
            siftBtn.isEnabled=true
        }
        val expenseTypeChipGroup=view.findViewById<View>(R.id.expenseTypeChipGroup) as ChipGroup
        accountChipGroup=view.findViewById(R.id.accountChipGroup)

        accountChip1=view.findViewById<Chip>(R.id.wechatAccountChip)
        accountChip2=view.findViewById<Chip>(R.id.alipayAccountChip)
        accountChip3=view.findViewById<Chip>(R.id.campusCardAccountChip)
        System.out.println(accountChip1.text.toString())
//        accountChipList.add(accountChip1)
//        accountChipList.add(accountChip2)
//        accountChipList.add(accountChip3)
        accountIdList=accountChipGroup.checkedChipIds
        expenseTypeIdList=expenseTypeChipGroup.checkedChipIds
        pieChart=view.findViewById(R.id.pieChart)
        lineChart=view.findViewById(R.id.lineChart)
        for(id in expenseTypeIdList){
            expenseTypeChipList.add(view.findViewById<View>(id) as Chip)
        }
        billList=ArrayList<bill>()
        setHasOptionsMenu(true) // Turn on option menu
        siftLayout=view.findViewById<View>(R.id.siftLayout) as RelativeLayout

        catNames=im.getAllCategoryNames()
        srcNames=im.getAllSourceNames()

        drawPieChart(view)

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

        val selectAllBtn=view.findViewById<View>(R.id.selectAllBtn) as Button
        selectAllBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(expenseTypeChipGroup.checkedChipIds.size!=0) {
                    expenseTypeChipGroup.clearCheck()
                }
                else {
                    for (chip in expenseTypeChipList){
                        chip.setChecked(true)
                    }
                }
            }
        })

        val siftBtn=view.findViewById<View>(R.id.siftBtn) as Button
        siftBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                billList=getSiftedBills()
                drawLineChart(view)
                drawPieChart(view)

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

    fun getSiftedBills():List<bill>{
        //get date range
        val dateRange=dateRangePicker.selection
        val checkedAccountIds=accountChipGroup.checkedChipIds as ArrayList<Int>
        val checkedExpenseTypeIds=expenseTypeChipGroup.checkedChipIds as ArrayList<Int>
//        val bills= im.siftRecords(Date(dateRange.first),Date(dateRange.second),checkedAccountIds,checkedExpenseTypeIds)
//        return bills
        return ArrayList<bill>()
    }

    fun UTC2Str(utc:Long): String {
        val sdf=SimpleDateFormat("yyyy/MM/dd")
        val date=Date(utc)
        val str=sdf.format(date)
        return str
    }

    fun drawPieChart(view: View){
        //pie chart
        //准备数据
        Toast.makeText(context,billList.size.toString(),Toast.LENGTH_SHORT).show()
        val typeSumMap=HashMap<String,Float>()
        for(bill in billList){
            typeSumMap.put(bill.type1,typeSumMap.getOrDefault(bill.type1, 0F)?.plus(bill.account))
        }

        val entries: MutableList<PieEntry> = ArrayList()
        for(entry in typeSumMap){
            entries.add(PieEntry(entry.value,entry.key))
        }
        val set = PieDataSet(entries, "type proportion")
//        set.setColors(
//                intArrayOf(
//                        R.color.purple_200,
//                        R.color.purple_500,
//                        R.color.purple_700,
//                        R.color.teal_200
//                ), context
//        )
        val data = PieData(set)
        pieChart.data = data
        pieChart.centerText = "This is a center text!"
        pieChart.centerTextRadiusPercent = 0.8f
        pieChart.invalidate() // refresh
    }

    fun drawLineChart(view:View){

    }

}