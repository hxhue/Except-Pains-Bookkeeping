package com.example.epledger.chart

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.util.Pair
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.example.epledger.R
import com.example.epledger.db.AppDatabase
import com.example.epledger.model.Record
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChartsFragment: Fragment() {
    private val ONEDAY=24*60*60*1000
    private var dateRangeIsSet=false
    private lateinit var pieChart: PieChart
//    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    lateinit var billList:List<Record>
    private lateinit var pieSwitch:SwitchMaterial
    private lateinit var pieTextView:TextView
    private lateinit var barXLabels:ArrayList<String>
    private lateinit var siftDialogView:View
    private lateinit var dialog:androidx.appcompat.app.AlertDialog
    private lateinit var accountChipGroup: ChipGroup
    private lateinit var expenseTypeChipGroup: ChipGroup
    private lateinit var catNames:List<String>
    private lateinit var srcNames:List<String>
    private lateinit var mainView:View
    private var submittedTypeChipIds=ArrayList<Int>()
    private var submittedAccountChipIds=ArrayList<Int>()
    private var dateRangePicker=
            MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .setSelection(
                            Pair(
                                MaterialDatePicker.todayInUtcMilliseconds() - 14 * ONEDAY,
                                MaterialDatePicker.todayInUtcMilliseconds()
                            )
                    )
                    .build()

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /**
         * 完成各组件的初始化工作
         */
        val view = inflater.inflate(R.layout.activity_show_chart, container, false)
        mainView=view
        siftDialogView= inflater.inflate(R.layout.dialog_sift_records,container,false)
        siftDialogView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        accountChipGroup=siftDialogView.findViewById(R.id.accountChipGroup)
        expenseTypeChipGroup=siftDialogView.findViewById(R.id.expenseTypeChipGroup)
        pieChart=view.findViewById(R.id.pieChart)
//        lineChart=view.findViewById(R.id.lineChart)
        barChart=view.findViewById(R.id.barChart)
        pieSwitch=view.findViewById(R.id.pieSwitch)
        pieTextView=view.findViewById(R.id.pieTextView)
        billList=ArrayList()
        dialog= context?.let {
            MaterialAlertDialogBuilder(it)
                    .setView(siftDialogView)
                    .setNegativeButton(R.string.cancel){_,_-> /**/}
                    .setPositiveButton(R.string.sift){_,_->
                        submittedTypeChipIds= expenseTypeChipGroup.checkedChipIds as ArrayList<Int>
                        submittedAccountChipIds=accountChipGroup.checkedChipIds as ArrayList<Int>
                        refresh()
                        Toast.makeText(context, billList.size.toString() + " records found.", Toast.LENGTH_SHORT).show()
                    }
                    .setTitle(R.string.sift_conditions)
                    .create()
        }!!
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=dateRangeIsSet
        }
        barXLabels=ArrayList()
        for(i in 0 until 1000) barXLabels.add("null")  //初始化日期范围为1000天，可动态扩容
        class MyXAXisFormatter:ValueFormatter(){
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return barXLabels[value.toInt()]
            }
        }
        barChart.xAxis.valueFormatter=MyXAXisFormatter()
        setHasOptionsMenu(true) // Turn on option menu
        initChips()
        submittedAccountChipIds= accountChipGroup.checkedChipIds as ArrayList<Int>
        submittedTypeChipIds= expenseTypeChipGroup.checkedChipIds as ArrayList<Int>

        /**
         * 默认筛选最近14天的账单,并生成图表
         */
        billList= AppDatabase.siftRecords(Date(Date().time - 14 * 24 * 60 * 60 * 1000), Date()
                , AppDatabase.getAllSourceNames() as ArrayList<String>, AppDatabase.getAllCategoryNames() as ArrayList<String>)
        drawChart(view, true)

        /**
         * 设置各回调函数
         */
        pieSwitch.setOnCheckedChangeListener { _, _ ->
            drawPieChart(view, pieSwitch.isChecked)
            if (pieSwitch.isChecked)
                pieTextView.setText(R.string.cost)
            else
                pieTextView.setText(R.string.income)
        }

        dateRangePicker.addOnPositiveButtonClickListener{
            dateRangeIsSet=true
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).isEnabled=true
        }

        val setDateRangeBtn= siftDialogView.findViewById<View>(R.id.setDateRangeButton) as Button
        setDateRangeBtn.setOnClickListener {
            activity?.supportFragmentManager?.let { dateRangePicker.show(it, "DATE_RANGE_PICKER") }
        }

        val selectAllCategoriesBtn=siftDialogView.findViewById<View>(R.id.selectAllCategoriesBtn) as Button
        selectAllCategoriesBtn.setOnClickListener {
            if (expenseTypeChipGroup.checkedChipIds.size != 0) {
                expenseTypeChipGroup.clearCheck()
            } else {
                val len = expenseTypeChipGroup.size
                for (i in 0 until len) {
                    val chip = expenseTypeChipGroup[i] as Chip
                    chip.isChecked = true
                }
            }
        }

        val selectAllAccountsBtn=siftDialogView.findViewById<View>(R.id.selectAllAccountsBtn) as Button
        selectAllAccountsBtn.setOnClickListener {
            if (accountChipGroup.checkedChipIds.size != 0) {
                accountChipGroup.clearCheck()
            } else {
                val len = accountChipGroup.size
                for (i in 0 until len) {
                    val chip = accountChipGroup[i] as Chip
                    chip.isChecked = true
                }
            }
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    /**
     * 绘制所有图表，不包括更新数据
     */
    private fun drawChart(view: View, pieBool: Boolean){
//        drawLineChart(view)
        drawPieChart(view, pieBool)
        drawBarChart(view)
    }

    /**
     * 根据billList生成饼图
     */
    private fun drawPieChart(view: View, costOrIncome: Boolean){
        val typeSumMap=HashMap<String, Double>()
        if(costOrIncome){
            for(bill in billList){
                if(bill.moneyAmount<0) bill.category?.let { typeSumMap.put(it, typeSumMap.getOrDefault(bill.category!!, 0.0)?.plus(bill.moneyAmount * (-1.0))) }
            }
        }else{
            for(bill in billList){
                    if(bill.moneyAmount>0) bill.category?.let { typeSumMap.put(it, typeSumMap.getOrDefault(bill.category!!, 0.0)?.plus(bill.moneyAmount)) }
            }
        }


        val entries: MutableList<PieEntry> = ArrayList()
        for(entry in typeSumMap){
            entries.add(PieEntry(entry.value.toFloat(), entry.key))
        }
        val set = PieDataSet(entries, "type proportion")
        set.sliceSpace= 5F
        set.setColors(
                intArrayOf(
                        R.color.pieColor1,R.color.pieColor2,R.color.pieColor3,R.color.pieColor4
                        ,R.color.pieColor5,R.color.pieColor6,R.color.pieColor7
                ), context
        )
        val data = PieData(set)
        pieChart.data = data

        /**
         * 个性化设置
         */
        val percentFormatter=PercentFormatter(pieChart)
        data.setValueFormatter(percentFormatter)
        pieChart.apply{
            setUsePercentValues(true)
            centerTextRadiusPercent = 1f
            holeRadius=55f
            transparentCircleRadius=58f
            setTransparentCircleAlpha(120)
            description.isEnabled=false
            legend.isEnabled=false
        }

        /**
         * 设置中心环文字
         */
        pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
                pieChart.centerText = ""
            }

            override fun onValueSelected(p0: Entry?, p1: Highlight?) {
                if (p0 is PieEntry) {
                    pieChart.centerText = (p0.label + "\n" + p0.value + " 元")
                }
            }
        })
//        pieChart.invalidate() // refresh
        pieChart.animateXY(500,500)
    }
/*
    fun drawLineChart(view: View){
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
        lineChart.setNoDataText("No data available!")
        lineChart.setDrawGridBackground(false)
        lineChart.setDrawBorders(true)
//        lineChart.setBorderColor(R.color.design_default_color_error)
//        lineChart.setBackgroundColor(R.color.design_default_color_on_primary) //?
        lineChart.setBorderWidth(5f)
//        lineChart.invalidate()
        lineChart.animateXY(300,300, EaseInOutBack)
    }

 */

    /**
     * 根据billList生成柱状图
     */
    private fun drawBarChart(view: View){
        //先根据dateRange指定日期范围，创建并初始化map<日期，支出金额>的每一个条目，然后将支出条目的金额加到对应日期上，最后生成barChart。
        //创建一个开始date（0点），然后用每个支出条目的date减该date并除以一天的时间，就得到对应天数
        val baseDate=Date((Date().time - ONEDAY * 13))
        val endDate=Date((Date().time + ONEDAY))//第二天的零点

        // FIXME: 2021/6/2 修正time设定
        if(dateRangeIsSet){
            baseDate.time= dateRangePicker.selection?.first ?: baseDate.time
            endDate.time= dateRangePicker.selection?.second?.plus(ONEDAY) ?: endDate.time
        }

        baseDate.time=baseDate.time-baseDate.time%ONEDAY-8*60*60*1000 //时间调整为第一天0点
        endDate.time=endDate.time-endDate.time%ONEDAY-8*60*60*1000
        println("drawBarChart: base date=$baseDate")
        println("drawBarChart: end date=$endDate")
        val dayNum=(endDate.time-baseDate.time)/ONEDAY
        println("drawBarChart: dayNum=$dayNum")

        /**
         *set x labels  更新labels要在set data之前完成！
         */
        //动态扩容barXLabels
        if(dayNum>barXLabels.size-100){
            val addNum=dayNum+100-barXLabels.size
            for(i in 0 until addNum) barXLabels.add("null")
        }

        val baseCalendar = Calendar.getInstance()
        baseCalendar.time=baseDate
        barXLabels.fill("null")

        var idx=0
        while(baseCalendar.time.time<endDate.time){
//            barXLabels.add(baseCalendar.get(Calendar.DAY_OF_MONTH).toString())
            barXLabels[idx++]=baseCalendar.get(Calendar.DAY_OF_MONTH).toString()
//            println("Label add:"+baseCalendar.get(Calendar.DAY_OF_MONTH).toString())
            baseCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val mp=HashMap<Int, Double>()
        for(i in 0 until dayNum){
            mp[i.toInt()] = 0.0
        }

        for(record in billList){
            if(record.moneyAmount<0){
                val idx=(record.mDate.time-baseDate.time)/ONEDAY
                mp[idx.toInt()]?.minus(record.moneyAmount)?.let { mp.put(idx.toInt(), it) }
            }
        }
        val entries=ArrayList<BarEntry>()
        for(item in mp){
            entries.add(BarEntry(item.key.toFloat(), item.value.toFloat()))
        }
        val setCost=BarDataSet(entries, "bar chart label")
        setCost.axisDependency=YAxis.AxisDependency.LEFT
        val dataSets=ArrayList<IBarDataSet>();
        dataSets.add(setCost)
        val data=BarData(dataSets)
        barChart.data=data

        /**
         * 个性化设置
         */
        data.barWidth=0.9f //柱宽
        barChart.apply{
            xAxis.granularity=1f
            setFitBars(true)  //有什么用？
            setCost.setColors(intArrayOf(
                    R.color.barColor1,
                    R.color.barColor2,
                    R.color.barColor3,
                    R.color.barColor4
            ), context)
            setScaleEnabled(false)
            setPinchZoom(false)
            isDragEnabled=true
            isDoubleTapToZoomEnabled=false
            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum=0F
            }
            axisRight.apply {
                isEnabled=false
            }
            xAxis.apply {
                position=XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }
            setDrawValueAboveBar(true)
            legend.isEnabled=false
            setVisibleXRangeMaximum(14F)
//            setVisibleXRangeMinimum(4F)
            description.isEnabled=false
        }

//        barChart.invalidate()
        barChart.animateXY(300,300)
    }

    /**
     * 更新数据并重新生成所有图表
     */
    private fun refresh(){
        getSiftedBills(mainView)
        drawChart(mainView,true)
    }

    /**
     * 实现top app bar功能
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sift -> {
                dialog.show()

//                animation
//                if(!dragged){
//                    siftLayout.animate().translationY(800f).setDuration(300).start()
//                    pieChart.animate().translationY(800f).setDuration(300).start()
////                    lineChart.animate().translationY(800f).setDuration(300).start()
//                    barChart.animate().translationY(800f).setDuration(300).start()
//                    dragged=true
//                }else{
//                    siftLayout.animate().translationY(0f).setDuration(300).start()
//                    pieChart.animate().translationY(0f).setDuration(300).start()
////                    lineChart.animate().translationY(0f).setDuration(300).start()
//                    barChart.animate().translationY(0f).setDuration(300).start()
//                    dragged=false
//                }
            }
            R.id.reset -> {
                refresh()
                Toast.makeText(context,"Charts refreshed!",Toast.LENGTH_SHORT).show()
            }
            R.id.more -> {
                // Handle more item (inside overflow menu) press
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 利用数据库操作筛选出默认记录或满足用户筛选条件的记录
     * 在drawChart()前要先调用此函数
     * 调用refresh()前无需调用此函数
     */
    private fun getSiftedBills(view: View){
        var dateBegin=Date((Date().time - ONEDAY * 13))
        var dateEnd=Date((Date().time + ONEDAY))

        if(dateRangeIsSet){
            val dateRange=dateRangePicker.selection
            dateBegin= dateRange?.let { Date(it.first) }!!
            dateEnd= Date(dateRange.second)
        }

        println("getSiftedBills: dateBegin=$dateBegin")
        println("getSiftedBills: dateBegin=$dateEnd")

        val srcList=ArrayList<String>()
        for(id in submittedAccountChipIds) {
            val chip = siftDialogView.findViewById<View>(id) as Chip
            srcList.add(chip.text.toString())
        }
        val catList=ArrayList<String>()
        for(id in submittedTypeChipIds) {
            val chip = siftDialogView.findViewById<View>(id) as Chip
            catList.add(chip.text.toString())
        }

        var bills: MutableList<Record> = ArrayList()
        bills = AppDatabase.siftRecords(dateBegin, dateEnd, srcList, catList)

        billList=bills
        return
    }

    /**
     * 初始化筛选对话框的chips
     */
    private fun initChips(){
        catNames= AppDatabase.getAllCategoryNames()
        srcNames= AppDatabase.getAllSourceNames()

        for(catStr in catNames){
            val chip=layoutInflater.inflate(R.layout.layout_chip_choice, expenseTypeChipGroup, false) as Chip
            chip.text=catStr
            expenseTypeChipGroup.addView(chip)
        }
        for(srcStr in srcNames){
            val chip=layoutInflater.inflate(R.layout.layout_chip_choice, accountChipGroup, false) as Chip
            chip.text=srcStr
            accountChipGroup.addView(chip)
        }
    }

    /**
     * 在添加或删除种类、来源时调用addCategory，deleteCategory，addSource，deleteSource中相应的函数
     * 用于更新图表筛选对话框的chips
     */
    public fun addCategory(catName:String){
        val chip=layoutInflater.inflate(R.layout.layout_chip_choice, expenseTypeChipGroup, false) as Chip
        chip.text=catName
        expenseTypeChipGroup.addView(chip)
    }

    public fun deleteCategory(catName: String){
        for(chip in expenseTypeChipGroup){
            if(chip is Chip&&chip.text==catName){
                expenseTypeChipGroup.removeView(chip)
            }
        }
    }

    public fun addSource(srcName:String){
        val chip=layoutInflater.inflate(R.layout.layout_chip_choice, accountChipGroup, false) as Chip
        chip.text=srcName
        accountChipGroup.addView(chip)
    }

    public fun deleteSource(srcName: String){
        for(chip in accountChipGroup){
            if(chip is Chip&&chip.text==srcName){
                accountChipGroup.removeView(chip)
            }
        }
    }

}


//    fun UTC2Str(utc:Long): String {
//        val sdf=SimpleDateFormat("yyyy/MM/dd")
//        val date=Date(utc)
//        val str=sdf.format(date)
//        return str
//    }