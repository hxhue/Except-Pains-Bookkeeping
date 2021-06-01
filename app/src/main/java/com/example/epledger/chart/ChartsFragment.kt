package com.example.epledger.chart

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.util.Pair
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.example.epledger.R
import com.example.epledger.db.AppDatabase
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.example.epledger.db.ImportDataFromExcel.bill
import com.example.epledger.db.SqliteDatabase
import com.example.epledger.model.Record
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlin.collections.HashMap

class ChartsFragment: Fragment() {
    private val ONEDAY=24*60*60*1000
    private var dateRangeIsSet=false
    private lateinit var siftLayout:RelativeLayout
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    private lateinit var accountIdList:List<Int>
    private lateinit var expenseTypeIdList:List<Int>
    val expenseTypeChipList=ArrayList<Chip>()
    private lateinit var accountChipGroup:ChipGroup
    private lateinit var expenseTypeChipGroup: ChipGroup
    private lateinit var billList:List<Record>
    private lateinit var siftBtn:Button
    private lateinit var pieSwitch:SwitchMaterial
    private lateinit var pieTextView:TextView
    private lateinit var catNames:List<String>
    private lateinit var srcNames:List<String>

    var dateRangePicker=
            MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .setSelection(
                            Pair<Long, Long>(
                                    MaterialDatePicker.todayInUtcMilliseconds()-14*ONEDAY,
                                    MaterialDatePicker.todayInUtcMilliseconds()
                            )
                    )
                    .build()

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_show_chart, container, false)

        siftBtn=view.findViewById(R.id.siftBtn)
        pieChart=view.findViewById(R.id.pieChart)
        lineChart=view.findViewById(R.id.lineChart)
        barChart=view.findViewById(R.id.barChart)
        pieSwitch=view.findViewById(R.id.pieSwitch)
        pieTextView=view.findViewById(R.id.pieTextView)
        dateRangePicker.addOnPositiveButtonClickListener {
            siftBtn.isEnabled=true
            dateRangeIsSet=true
        }
        accountChipGroup=view.findViewById(R.id.accountChipGroup)
        expenseTypeChipGroup=view.findViewById(R.id.expenseTypeChipGroup)

        accountIdList=accountChipGroup.checkedChipIds
        expenseTypeIdList=expenseTypeChipGroup.checkedChipIds
        for(id in expenseTypeIdList){
            expenseTypeChipList.add(view.findViewById<View>(id) as Chip)
        }
        billList=ArrayList<Record>()
        setHasOptionsMenu(true) // Turn on option menu
        siftLayout=view.findViewById<View>(R.id.siftLayout) as RelativeLayout

        createChips()

        //默认筛选14天的账单
        billList=im.siftRecords(Date(Date().time-14*24*60*60*1000),Date(),im.getAllSourceNames() as ArrayList<String>,im.getAllCategoryNames() as ArrayList<String>)
        drawChart(view,true)

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
                    val len=expenseTypeChipGroup.size
                    for(i in 0 until len){
                        val chip=expenseTypeChipGroup[i] as Chip
                        chip.isChecked=true
                    }
                }
            }
        })

        val siftBtn=view.findViewById<View>(R.id.siftBtn) as Button
        siftBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                billList=getSiftedBills(view)
                Toast.makeText(context,billList.size.toString()+" records found",Toast.LENGTH_SHORT).show()
                drawChart(view,true)
            }
        })

        pieSwitch.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                drawPieChart(view, pieSwitch.isChecked)
                if (pieSwitch.isChecked)
                    pieTextView.setText(R.string.cost)
                else
                    pieTextView.setText(R.string.income)
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

    fun getSiftedBills(view: View): MutableList<Record>{
        //get date range
        val dateRange=dateRangePicker.selection
        val dateBegin= dateRange?.let { Date(it.first) }
        val dateEnd= dateRange?.let { Date(it.second) }

        val srcList=ArrayList<String>()
        for(id in accountChipGroup.checkedChipIds) {
            val chip = view.findViewById<View>(id) as Chip
            srcList.add(chip.text.toString())
        }
        val catList=ArrayList<String>()
        for(id in expenseTypeChipGroup.checkedChipIds) {
            val chip = view.findViewById<View>(id) as Chip
            catList.add(chip.text.toString())
        }

        var bills: MutableList<Record> = ArrayList<Record>()
        if(dateBegin!=null&&dateEnd!=null){
            System.out.println(dateBegin)
            System.out.println(dateEnd)
            for (src in srcList){
                System.out.println(src)
            }
            for(cat in catList){
                System.out.println(cat)
            }

            bills = AppDatabase.siftRecords(dateBegin,dateEnd,srcList,catList)
        }
        return bills
    }

    fun drawPieChart(view: View, costOrIncome:Boolean){
        //pie chart
        //准备数据
        val typeSumMap=HashMap<String,Double>()
        if(costOrIncome){
            for(bill in billList){
                if(bill.moneyAmount<0) bill.category?.let { typeSumMap.put(it,typeSumMap.getOrDefault(bill.category!!, 0.0)?.plus(bill.moneyAmount*(-1.0))) }
            }
        }else{
            for(bill in billList){
                    if(bill.moneyAmount>0) bill.category?.let { typeSumMap.put(it,typeSumMap.getOrDefault(bill.category!!, 0.0)?.plus(bill.moneyAmount)) }
            }
        }


        val entries: MutableList<PieEntry> = ArrayList()
        for(entry in typeSumMap){
            entries.add(PieEntry(entry.value.toFloat(),entry.key))
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
        pieChart.centerText = "It's center text!"
        pieChart.centerTextRadiusPercent = 0.8f
        pieChart.invalidate() // refresh
        System.out.println(entries.size)
    }

    fun drawLineChart(view:View){
        //按天可视化支出信息
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
//        lineChart.setBorderColor(R.color.design_default_color_error)
//        lineChart.setBackgroundColor(R.color.design_default_color_on_primary) //?
        lineChart.setBorderWidth(5f)
        lineChart.invalidate()
    }

    fun createChips(){
        catNames= AppDatabase.getAllCategoryNames()
        srcNames= AppDatabase.getAllSourceNames()
//        val chipLP=ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)

        for(catStr in catNames){
            val chip=layoutInflater.inflate(R.layout.layout_chip_choice,expenseTypeChipGroup,false) as Chip
            chip.text=catStr
            expenseTypeChipGroup.addView(chip)
        }
        for(srcStr in srcNames){
            val chip=layoutInflater.inflate(R.layout.layout_chip_choice,accountChipGroup,false) as Chip
            chip.text=srcStr
            accountChipGroup.addView(chip)
        }
    }

    fun drawChart(view:View,pieBool:Boolean){
        drawLineChart(view)
        drawPieChart(view,pieBool)
        drawBarChart(view)
    }

    fun drawBarChart(view:View){
        //先根据dateRange指定日期范围，创建并初始化map<日期，支出金额>的每一个条目，然后将支出条目的金额加到对应日期上，最后生成barChart。
        //创建一个开始date（0点），然后用每个支出条目的date减该date并除以一天的时间，就得到对应天数
        val baseDate=Date((Date().time-ONEDAY*13))
        val endDate=Date((Date().time+ONEDAY))//第二天的零点
        if(dateRangeIsSet){
            baseDate.time= dateRangePicker.selection?.first ?: baseDate.time
            endDate.time= dateRangePicker.selection?.second?.plus(ONEDAY) ?: endDate.time
        }
        baseDate.time=baseDate.time-baseDate.time%ONEDAY-8*60*60*1000 //时间调整为第一天0点
        endDate.time=endDate.time-endDate.time%ONEDAY-8*60*60*1000
        val dayNum=(endDate.time-baseDate.time)/ONEDAY
        println("dayNum=$dayNum")
        val mp=HashMap<Int,Double>()
        for(i in 0 until dayNum){
            mp[i.toInt()] = 0.0
        }

        for(record in billList){
            if(record.moneyAmount<0){
                val idx=(record.mDate.time-baseDate.time)/ONEDAY
                mp[idx.toInt()]?.minus(record.moneyAmount)?.let { mp.put(idx.toInt(), it) }
                println("map item idx="+idx.toString()+"  new value="+ mp[idx.toInt()])
            }
        }
        val entries=ArrayList<BarEntry>()
        for(item in mp){
            entries.add(BarEntry(item.key.toFloat(),item.value.toFloat()))
        }
        val setCost=BarDataSet(entries,"bar chart")
        //By calling setAxisDependency(...), the axis the DataSet should be plotted against is specified
        setCost.axisDependency=YAxis.AxisDependency.LEFT
        val dataSets=ArrayList<IBarDataSet>();
        dataSets.add(setCost)
        val data=BarData(dataSets)
        barChart.data=data

        /**
         *set x labels
         */
        val xlabels=ArrayList<String>()
        val baseCalendar = Calendar.getInstance()
        baseCalendar.time=baseDate
        while(baseCalendar.time<endDate){
            xlabels.add(baseCalendar.get(Calendar.DAY_OF_MONTH).toString())
            baseCalendar.add(Calendar.DAY_OF_MONTH,1)
        }
        class MyXAXisFormatter:ValueFormatter(){
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return xlabels[value.toInt()]
            }
        }
        barChart.xAxis.valueFormatter=MyXAXisFormatter()

        /**
         * 个性化设置
         */
        barChart.xAxis.granularity=1f
        data.barWidth=0.9f //柱宽
        barChart.setFitBars(true)  //有什么用？
        setCost.setColors(intArrayOf(
                R.color.barColor1,
                R.color.barColor2,
                R.color.barColor3,
                R.color.barColor4
        ),context)

        barChart.invalidate()

    }


}


//    fun UTC2Str(utc:Long): String {
//        val sdf=SimpleDateFormat("yyyy/MM/dd")
//        val date=Date(utc)
//        val str=sdf.format(date)
//        return str
//    }