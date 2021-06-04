package com.example.epledger.chart

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.util.Pair
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.epledger.R
import com.example.epledger.db.AppDatabase
import com.example.epledger.db.DatabaseModel
import com.example.epledger.model.Record
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
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
    private val allCatNames=ArrayList<String>()
    private val allSrcNames=ArrayList<String>()
    private lateinit var mainView:View
    private lateinit var dateRangeTitle:TextView
    private var submittedTypeChipIds=ArrayList<Int>()
    private var submittedAccountChipIds=ArrayList<Int>()
    private lateinit var dateRangeDisplay: TextView
    private lateinit var autoComplete:AutoCompleteTextView
    private lateinit var menuTextTmp:String
    private lateinit var tableData1:TextView
    private lateinit var tableData2:TextView
    private lateinit var tableData3:TextView
    private lateinit var tableData4:TextView
    private val barYAxisValueFormatter=BarYAxisValueFormatter()
    private lateinit var percentFormatter:PercentFormatter

    // This is a database model used for accessing running data.
    // But do not use it without knowing what it's truly for.
    private val dbModel: DatabaseModel by activityViewModels()
    private var dateRangePicker=
            MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .setSelection(
                            Pair(
                                    MaterialDatePicker.todayInUtcMilliseconds() - 13 * ONEDAY,
                                    MaterialDatePicker.todayInUtcMilliseconds()
                            )
                    )
                    .build()

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /**
         * 变量初始化
         */
        val view = inflater.inflate(R.layout.activity_show_chart, container, false)
        mainView=view
        siftDialogView= inflater.inflate(R.layout.dialog_sift_records, container, false)
        siftDialogView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        menuTextTmp=getString(R.string.recent_14_days)
        accountChipGroup=siftDialogView.findViewById(R.id.accountChipGroup)
        expenseTypeChipGroup=siftDialogView.findViewById(R.id.expenseTypeChipGroup)
        pieChart=view.findViewById(R.id.pieChart)
//        lineChart=view.findViewById(R.id.lineChart)
        barChart=view.findViewById(R.id.barChart)
        pieSwitch=view.findViewById(R.id.pieSwitch)
        pieTextView=view.findViewById(R.id.pieTextView)
        dateRangeDisplay=siftDialogView.findViewById(R.id.dateRangeDisplay)
        dateRangeTitle=view.findViewById(R.id.dateRangeTitle)
        tableData1=view.findViewById(R.id.tableData1)
        tableData2=view.findViewById(R.id.tableData2)
        tableData3=view.findViewById(R.id.tableData3)
        tableData4=view.findViewById(R.id.tableData4)
        percentFormatter=PercentFormatter(pieChart)
        billList=ArrayList()
        //创建exposed dropdown menu
        val items=listOf(getString(R.string.recent_14_days), getString(R.string.recent_7_days), getString(R.string.sift_by_yourself))
        val adapter=ArrayAdapter(requireContext(), R.layout.list_item, items)
        val textField=view.findViewById<TextInputLayout>(R.id.menu)
        (textField.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        autoComplete=view.findViewById<AutoCompleteTextView>(R.id.textField)
        autoComplete.setText(getString(R.string.recent_14_days), false)
        autoComplete.setOnItemClickListener{ _: AdapterView<*>, view: View, position: Int, _: Long ->
            when(position){
                0 -> {
                    getSiftedBills(view, 14)
                    drawChart(view, pieSwitch.isChecked, 14, true)
                    menuTextTmp = getString(R.string.recent_14_days)
                    dateRangeTitle.text = ""
                    updateTable(14)
                    dateRangeTitle.text = getRecentDateRangeStr(14)
                }
                1 -> {
                    getSiftedBills(view, 7)
                    drawChart(view, pieSwitch.isChecked, 7, true)
                    menuTextTmp = getString(R.string.recent_7_days)
                    dateRangeTitle.text = ""
                    updateTable(7)
                    dateRangeTitle.text = getRecentDateRangeStr(7)
                }
                2 -> {
                    dialog.show()
                }
            }
        }
        //创建筛选dialog
        dialog= context?.let {
            MaterialAlertDialogBuilder(it)
                    .setView(siftDialogView)
                    .setNegativeButton(R.string.cancel){ _, _->
                        autoComplete.setText(menuTextTmp, false)
                    }
                    .setPositiveButton(R.string.sift){ _, _->
                        submittedTypeChipIds= expenseTypeChipGroup.checkedChipIds as ArrayList<Int>
                        submittedAccountChipIds=accountChipGroup.checkedChipIds as ArrayList<Int>
                        refresh()
                        menuTextTmp=getString(R.string.sift_by_yourself)
                        dateRangePicker.selection?.let{ it1->
                            dateRangeTitle.text=getString(R.string.show_date_range, utc2str(it1.first), utc2str(it1.second)
                            )
                        }
                        updateTable(getDayNum())
                        Toast.makeText(context, billList.size.toString() + " records found.", Toast.LENGTH_SHORT).show()
                    }
                    .setTitle(R.string.sift_conditions)
                    .create()
        }!!
        dialog.setOnShowListener {
            if(!dateRangeIsSet){
                dateRangeDisplay.text=getString(R.string.show_date_range, utc2str(Date().time - ONEDAY * 13), utc2str(Date().time))
            }else{
                dateRangeDisplay.text=getString(R.string.show_date_range, dateRangePicker.selection?.let { it1 -> utc2str(it1.first) },
                        dateRangePicker.selection?.let { it1 -> utc2str(it1.second) })
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=dateRangeIsSet
        }
        //设置barChart的X轴ValueFormatter，初始化日期范围为1000天，可动态扩容
        barXLabels=ArrayList()
        for(i in 0 until 1000) barXLabels.add("null")
        class MyXAXisFormatter:ValueFormatter(){
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return barXLabels[value.toInt()]
            }
        }
        barChart.xAxis.valueFormatter=MyXAXisFormatter()

        /**
         * 其他初始化
         */
        dateRangeTitle.text=getRecentDateRangeStr(14)
        setHasOptionsMenu(true) // Turn on option menu
        initChips()
        submittedAccountChipIds= accountChipGroup.checkedChipIds as ArrayList<Int>
        submittedTypeChipIds= expenseTypeChipGroup.checkedChipIds as ArrayList<Int>
        for(id in submittedAccountChipIds){
            allSrcNames.add(siftDialogView.findViewById<Chip>(id).text as String)
        }
        for(id in submittedTypeChipIds){
            allCatNames.add(siftDialogView.findViewById<Chip>(id).text as String)
        }

        /**
         * 图表个性化设置
         */
        pieChart.apply{
            setUsePercentValues(true)
            centerTextRadiusPercent = 1f
            holeRadius=55f
            transparentCircleRadius=58f
            setTransparentCircleAlpha(120)
            description.isEnabled=false
            legend.isEnabled=false
            setNoDataText("No records available!")
        }
        barChart.apply{
            xAxis.granularity=1f
            setFitBars(true)  //有什么用？
            setScaleEnabled(false)
            setPinchZoom(false)
            isDragEnabled=true
            isDoubleTapToZoomEnabled=false
            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum=0F
                valueFormatter=barYAxisValueFormatter
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
            setNoDataText("No data available!")
        }

        /**
         * 默认筛选最近14天的账单,并生成图表
         */
        billList= AppDatabase.siftRecords(Date(Date().time - 14 * ONEDAY), Date(), AppDatabase.getAllSourceNames() as ArrayList<String>, AppDatabase.getAllCategoryNames() as ArrayList<String>)
        drawChart(view, pieSwitch.isChecked, 14, true)
        updateTable(14)

        /**
         * 设置各回调函数
         */
        pieSwitch.setOnCheckedChangeListener { _, _ ->
            pieChart.highlightValues(null)
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
    private fun drawChart(view: View, pieBool: Boolean, dayNum: Int, dayNumEnabled: Boolean){
//        drawLineChart(view)
        drawPieChart(view, pieBool)
        drawBarChart(view, dayNum, dayNumEnabled)
    }

    /**
     * 根据billList生成饼图
     */
    private val typeSumMap=HashMap<String, Double>()
    private val entries: MutableList<PieEntry> = ArrayList()
    private fun drawPieChart(view: View, costOrIncome: Boolean){
        GlobalScope.launch(Dispatchers.IO){
            /**
             * 设置数据
             */
            typeSumMap.clear()
            if(costOrIncome){
                for(bill in billList){
                    if(bill.money<0) {
                        val cat= bill.categoryID?.let { dbModel.findCategory(it) }
                        if (cat != null) {
                            typeSumMap[cat.name] = typeSumMap.getOrDefault(cat.name, 0.0).plus(bill.money * (-1.0))
                        }
                    }
                }
            }else{
                for(bill in billList){
                    if(bill.money>0) {
                        val cat= bill.categoryID?.let { dbModel.findCategory(it) }
                        if (cat != null) {
                            typeSumMap[cat.name] = typeSumMap.getOrDefault(cat.name, 0.0).plus(bill.money)
                        }
                    }
                }
            }

            entries.clear()
            for(entry in typeSumMap){
                entries.add(PieEntry(entry.value.toFloat(), entry.key))
            }
            val set = PieDataSet(entries, "type proportion")
            set.sliceSpace= 2F
            set.setColors(
                    intArrayOf(
                            R.color.pieColor1, R.color.pieColor2, R.color.pieColor3, R.color.pieColor4, R.color.pieColor5, R.color.pieColor6, R.color.pieColor7
                    ), context
            )
            val data = PieData(set)
            data.setValueFormatter(percentFormatter)
            data.setValueTextSize(14F)
            pieChart.data = data

            /**
             * 设置中心环文字和MarkerView
             */
//            pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//                override fun onNothingSelected() {
//                    pieChart.centerText = ""
//                }
//
//                override fun onValueSelected(p0: Entry?, p1: Highlight?) {
////                if (p0 is PieEntry) {
////                    pieChart.centerText = (p0.label + "\n" + p0.value + " 元")
////                }
//                    pieChart.centerText = ""
//                }
//            })
            val markerView=MyMarkerView(context, R.layout.marker)
            markerView.getViewHW(pieChart)
            pieChart.marker=markerView

            withContext(Dispatchers.Main){
                pieChart.animateXY(500, 500)
            }
        }

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
    private val baseCalendar: Calendar = Calendar.getInstance()
    private val mp=HashMap<Int, Double>()
    private val barEntries=ArrayList<BarEntry>()
    private fun drawBarChart(view: View, dayNum: Int, dayNumEnabled: Boolean){
        //先根据dateRange指定日期范围，创建并初始化map<日期，支出金额>的每一个条目，然后将支出条目的金额加到对应日期上，最后生成barChart。
        //创建一个开始date（0点），然后用每个支出条目的date减该date并除以一天的时间，就得到对应天数
        GlobalScope.launch(Dispatchers.IO){
            /**
             * 设置数据
             */
            val baseDate=Date((Date().time - ONEDAY * 13))
            val endDate=Date((Date().time + ONEDAY))//第二天的零点

            if(dayNumEnabled){
                baseDate.time=Date().time-ONEDAY*(dayNum-1)
            }else{
                if(dateRangeIsSet&&autoComplete.text.toString()==getString(R.string.sift_by_yourself)){
                    baseDate.time= dateRangePicker.selection?.first ?: baseDate.time
                    endDate.time= dateRangePicker.selection?.second?.plus(ONEDAY) ?: endDate.time
                }
            }
//        println("drawBarChart:baseDate=$baseDate")
//        println("drawBarChart:endDate=$endDate")
            if(baseDate.time%ONEDAY>=16*ONEDAY/24){
                //加一天
                baseDate.time+=ONEDAY
                endDate.time+=ONEDAY
            }
            baseDate.time=baseDate.time-baseDate.time%ONEDAY-8*60*60*1000 //时间调整为第一天0点
            endDate.time=endDate.time-endDate.time%ONEDAY-8*60*60*1000

//        println("drawBarChart: base date=$baseDate")
//        println("drawBarChart: end date=$endDate")

            val dayNum=(endDate.time-baseDate.time)/ONEDAY
//        println("drawBarChart: dayNum=$dayNum")

            //set x labels  更新labels要在set data之前完成！
            //动态扩容barXLabels
            if(dayNum>barXLabels.size-100){
                val addNum=dayNum+100-barXLabels.size
                for(i in 0 until addNum) barXLabels.add("null")
            }

            baseCalendar.time=baseDate
//            barXLabels.fill("null")

            var idx=0
            while(baseCalendar.time.time<endDate.time){
//            barXLabels.add(baseCalendar.get(Calendar.DAY_OF_MONTH).toString())
                barXLabels[idx++]=String.format("%02d/%02d"
                        ,baseCalendar.get(Calendar.MONTH)+1
                        ,baseCalendar.get(Calendar.DAY_OF_MONTH)
                )
//            println("Label add:"+baseCalendar.get(Calendar.DAY_OF_MONTH).toString())
                baseCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            mp.clear()
            for(i in 0 until dayNum){
                mp[i.toInt()] = 0.0
            }

            for(record in billList){
                if(record.money<0){
                    val idx=(record.date.time-baseDate.time)/ONEDAY
                    mp[idx.toInt()]?.minus(record.money)?.let { mp.put(idx.toInt(), it) }
                }
            }
            barEntries.clear()
            for(item in mp){
                barEntries.add(BarEntry(item.key.toFloat(), item.value.toFloat()))
            }
            val setCost=BarDataSet(barEntries, "bar chart label")
            setCost.axisDependency=YAxis.AxisDependency.LEFT
            setCost.setColors(intArrayOf(
                    R.color.barColor1,
                    R.color.barColor2,
                    R.color.barColor3,
                    R.color.barColor4
            ), context)
            val barDataSets=ArrayList<IBarDataSet>();
            barDataSets.add(setCost)
            val data=BarData(barDataSets)
            barChart.data=data
            //数据设置完成

            /**
             * 个性化设置
             */
            data.barWidth=0.9f //柱宽
            withContext(Dispatchers.Main){
                barChart.animateXY(300, 300)
            }
        }
    }

    /**
     * 根据当前menu的选择项更新数据并重新生成所有图表
     */
    private fun refresh(){
        GlobalScope.launch(Dispatchers.IO){
            when(autoComplete.text.toString()){
                getString(R.string.recent_14_days) -> {
                    getSiftedBills(mainView, 14)
                    withContext(Dispatchers.Main) {
                        drawChart(mainView, pieSwitch.isChecked, 14, true)
                    }
                }
                getString(R.string.recent_7_days) -> {
                    getSiftedBills(mainView, 7)
                    withContext(Dispatchers.Main) {
                        drawChart(mainView, pieSwitch.isChecked, 7, true)
                    }
                }
                getString(R.string.sift_by_yourself) -> {
                    getSiftedBills(mainView)
                    withContext(Dispatchers.Main) {
                        drawChart(mainView, pieSwitch.isChecked, -1, false)
                    }
                }
            }
        }
    }


    /**
     * 实现top app bar功能，已舍弃
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.sift -> {
//                dialog.show()

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
//            }
//            R.id.reset -> {
//                if(!dateRangeIsSet) autoComplete.setText(getString(R.string.recent_14_days),false)
//                refresh()
//                Toast.makeText(context,"Charts refreshed!",Toast.LENGTH_SHORT).show()
//            }
            R.id.more -> {
                // Handle more item (inside overflow menu) press
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 利用数据库操作筛选出默认记录或满足用户筛选条件的记录
     * 在drawChart()前要先调用此函数
     * refresh()已包含此函数
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

        billList= AppDatabase.siftRecords(dateBegin, dateEnd, srcList, catList)
        return
    }

    /**
     * 重载，获取最近dayNum（7/14）天的所有记录
     */
    private fun getSiftedBills(view: View, dayNum: Int){
        if(dayNum<1){
            billList=ArrayList()
            return
        }
        val dateBegin=Date((Date().time - ONEDAY * (dayNum - 1)))
        val dateEnd=Date((Date().time + ONEDAY))

        println("getSiftedBills: dateBegin=$dateBegin")
        println("getSiftedBills: dateBegin=$dateEnd")

        billList=AppDatabase.siftRecords(dateBegin, dateEnd, allSrcNames, allCatNames)
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
    public fun addCategory(catName: String){
        val chip=layoutInflater.inflate(R.layout.layout_chip_choice, expenseTypeChipGroup, false) as Chip
        chip.text=catName
        expenseTypeChipGroup.addView(chip)
        allCatNames.add(catName)
    }

    public fun deleteCategory(catName: String){
        for(chip in expenseTypeChipGroup){
            if(chip is Chip&&chip.text==catName){
                expenseTypeChipGroup.removeView(chip)
            }
        }
        allCatNames.remove(catName)
    }

    public fun addSource(srcName: String){
        val chip=layoutInflater.inflate(R.layout.layout_chip_choice, accountChipGroup, false) as Chip
        chip.text=srcName
        accountChipGroup.addView(chip)
        allSrcNames.add(srcName)
    }

    public fun deleteSource(srcName: String){
        for(chip in accountChipGroup){
            if(chip is Chip&&chip.text==srcName){
                accountChipGroup.removeView(chip)
            }
        }
        allSrcNames.remove(srcName)
    }

    /**
     * 更新统计数据表
     */
    @SuppressLint("SetTextI18n")
    private fun updateTable(dayNum: Int){
        GlobalScope.launch(Dispatchers.IO){
            val income=getIncome()
            val expenditure=getExpenditure()
            withContext(Dispatchers.Main){
                tableData1.text=income.toString()+"元"
                tableData2.text=expenditure.toString()+"元"
                tableData3.text=String.format("%.2f", expenditure / dayNum)+"元"  //日均支出
                tableData4.text=(income-expenditure).toString()+"元"  //结余
            }
        }
    }

    private fun getIncome():Double{
        var res=0.0
        billList.forEach {
            if(it.money>0){
                res+=it.money
            }
        }
        return res
    }

    private fun getExpenditure():Double{
        var res=0.0
        billList.forEach{
            if(it.money<0){
                res-=it.money
            }
        }
        return res
    }

    /**
     * 获取dateRangePicker当前指定的天数，dateRangePicker未被设置时返回-1
     */
    private fun getDayNum():Int{
        if(!dateRangeIsSet) return -1
        val baseDate=Date()
        val endDate=Date()
        dateRangePicker.selection?.let{
            baseDate.time= dateRangePicker.selection?.first ?: baseDate.time
            endDate.time= dateRangePicker.selection?.second?.plus(ONEDAY) ?: endDate.time
            baseDate.time=baseDate.time-baseDate.time%ONEDAY-8*60*60*1000 //时间调整为第一天0点
            endDate.time=endDate.time-endDate.time%ONEDAY-8*60*60*1000
            return ((endDate.time-baseDate.time)/ONEDAY).toInt()
        }
        return -1
    }

    /**
     * 生成最近(7/14)天的日期范围字符串
     */
    private fun getRecentDateRangeStr(dayNum: Int): String {
        return getString(R.string.show_date_range, utc2str(Date().time - ONEDAY * (dayNum - 1)), utc2str(Date().time))
    }

}

/**
 * Date类型的time属性转日期字符串
 */
fun utc2str(utc: Long): String {
    val sdf= SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val date=Date(utc)
    return sdf.format(date)
}

/**
 * PieChart的MarkerView，将高亮的消费类型的具体金额显示在PieChart中心
 */
class MyMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById<View>(R.id.tvContent) as TextView
    private var viewWidth:Int=0
    private var viewHeight:Int=0

    fun getViewHW(view: View){
        viewWidth=view.width
        viewHeight=view.height
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
//        println("refreshContent:")
        if(e is PieEntry){
            tvContent.text = String.format("%.2f¥", e.value)// set the entry-value as the display text
        }
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        val mOffset2=MPPointF.getInstance()
        val offset = this.offset
        mOffset2.x = offset.x
        mOffset2.y = offset.y

        mOffset2.x=mOffset2.x-posX+viewWidth/2-width/2
        mOffset2.y=mOffset2.y-posY+viewHeight/2-height/2

        return mOffset2
    }
}

/**
 * barChart Y轴的ValueFormatter，显示金额，以"¥"结尾
 */
class BarYAxisValueFormatter : ValueFormatter() {
    private val format = DecimalFormat("###,###,###,##0.0")

    // override this for BarChart
    override fun getBarLabel(barEntry: BarEntry?): String {
        return format.format(barEntry?.y)+"¥"
    }

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return format.format(value)+"¥"
    }

}