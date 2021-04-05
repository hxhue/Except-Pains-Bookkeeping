package com.example.epledger.chart;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.content.Context;

import com.example.epledger.R;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.transition.MaterialSharedAxis;

import java.util.ArrayList;
import java.util.List;

public class ShowChartActivity extends AppCompatActivity {


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chart);

        //pie chart
        PieChart pieChart = (PieChart)findViewById(R.id.pieChart);
        //准备数据
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(18.5f, "Green"));
        entries.add(new PieEntry(26.7f, "Yellow"));
        entries.add(new PieEntry(24.0f, "Red"));
        entries.add(new PieEntry(30.8f, "Blue"));
        PieDataSet set = new PieDataSet(entries, "Election Results");
        set.setColors(new int[] { R.color.purple_200, R.color.purple_500, R.color.purple_700, R.color.teal_200 }, this.getApplicationContext());
        PieData data = new PieData(set);
        pieChart.setData(data);

        pieChart.setCenterText("This is a center text!");
        pieChart.setCenterTextRadiusPercent(0.8f);
        pieChart.invalidate(); // refresh

        LineChart lineChart=(LineChart)findViewById(R.id.lineChart);
        List<Entry> valsComp1 = new ArrayList<Entry>();
        List<Entry> valsComp2 = new ArrayList<Entry>();
        Entry c1e1=new Entry(0f,100000f);
        valsComp1.add(c1e1);
        Entry c1e2 = new Entry(1f, 140000f); // 1 == quarter 2 ...
        valsComp1.add(c1e2);
        Entry c1e3 = new Entry(2f, 120000f); // 1 == quarter 2 ...
        valsComp1.add(c1e3);
        Entry c1e4 = new Entry(3f, 180000f); // 1 == quarter 2 ...
        valsComp1.add(c1e4);
        Entry c2e1 = new Entry(0f, 130000f); // 0 == quarter 1
        valsComp2.add(c2e1);
        Entry c2e2 = new Entry(1f, 115000f); // 1 == quarter 2 ...
        valsComp2.add(c2e2);
        Entry c2e3 = new Entry(2f, 60000f); // 0 == quarter 1
        valsComp2.add(c2e3);
        Entry c2e4 = new Entry(3f, 170000f); // 1 == quarter 2 ...
        valsComp2.add(c2e4);

        LineDataSet setComp1=new LineDataSet(valsComp1,"Company 1");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(R.color.teal_700);
        LineDataSet setComp2=new LineDataSet(valsComp2,"Company 2");
        setComp2.setAxisDependency(YAxis.AxisDependency.RIGHT);
        setComp2.setColor(R.color.purple_500);


        List<ILineDataSet> lineDataSets=new ArrayList<ILineDataSet>();
        lineDataSets.add(setComp1);
        lineDataSets.add(setComp2);

        LineData lineData=new LineData(lineDataSets);
        lineChart.setData(lineData);

        final String[] quarters = new String[] { "Q1", "Q2", "Q3", "Q4" };
        ValueFormatter valueFormatter=new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis){
                return quarters[(int) value];
            }
        };
        XAxis xAxis=lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(valueFormatter);

        Description lineChartDescription=new Description();
        lineChartDescription.setText("this is a lineChart description!");
        lineChartDescription.setTextAlign(Paint.Align.CENTER);
        lineChartDescription.setTextColor(R.color.purple_700);

        lineChart.setDescription(lineChartDescription);
        lineChart.setNoDataText("No data available!");
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(true);
        lineChart.setBorderColor(R.color.design_default_color_error);
        lineChart.setBackgroundColor(R.color.design_default_color_on_primary); //?
        lineChart.setBorderWidth(5f);
        lineChart.invalidate();



    }
}